package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.exception.SalePaymentException;
import com.gabrielosorio.gestor_inteligente.exception.SaleProcessingException;
import com.gabrielosorio.gestor_inteligente.exception.SaleValidationException;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.model.enums.SaleStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;
import com.gabrielosorio.gestor_inteligente.repository.base.SaleRepository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionManagerV2;
import com.gabrielosorio.gestor_inteligente.service.base.*;
import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.validation.SaleValidator;
import com.gabrielosorio.gestor_inteligente.view.shared.ToastNotification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleProductService saleProductService;
    private final SalePaymentService salePaymentService;
    private final CheckoutMovementService checkoutMovementService;
    private final CheckoutService checkoutService;
    private final ProductService productService;
    private final SaleCheckoutMovementService saleCheckoutMovementService;
    private final ConnectionFactory connectionFactory;
    private final Logger log = Logger.getLogger(SaleServiceImpl.class.getName());


    public SaleServiceImpl(SaleRepository saleRepository, SaleProductService saleProductService,
                           SalePaymentService salePaymentService, CheckoutMovementService checkoutMovementService,
                           CheckoutService checkoutService, ProductService productService,
                           SaleCheckoutMovementService saleCheckoutMovementService, ConnectionFactory connectionFactory) {
        this.saleRepository = saleRepository;
        this.saleProductService = saleProductService;
        this.salePaymentService = salePaymentService;
        this.checkoutMovementService = checkoutMovementService;
        this.checkoutService = checkoutService;
        this.productService = productService;
        this.saleCheckoutMovementService = saleCheckoutMovementService;
        this.connectionFactory = connectionFactory;
    }


    @Override
    public Sale processSale(User user, Sale sale) throws SaleProcessingException {
        try {
            TransactionManagerV2.beginTransaction(connectionFactory);

            // 1. Validar política de troco ANTES de qualquer operação
            validateChangePolicy(sale);

            // 2. Abrir/obter checkout
            var checkout = checkoutService.openCheckout(user);

            // 3. Salvar venda
            Sale savedSale = save(sale);

            // 4. Processar movimentos de recebimento (vendas)
            var checkoutMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.VENDA);
            String saleObservationBase = "Venda #" + savedSale.getId();
            boolean hasMultiplePayments = savedSale.getPaymentMethods().size() > 1;

            // ALTERAÇÃO: Usamos ArrayList para permitir adição posterior do troco
            List<CheckoutMovement> checkoutMovements = new ArrayList<>(savedSale.getPaymentMethods().stream()
                    .map(payment -> {
                        String saleObservation = saleObservationBase;

                        if (hasMultiplePayments || payment.getPaymentMethod() == PaymentMethod.CREDIT0) {
                            if (payment.getPaymentMethod() == PaymentMethod.CREDIT0) {
                                saleObservation += (payment.getInstallments() > 1)
                                        ? " - Crédito " + payment.getInstallments() + "x"
                                        : " - Crédito à vista";
                            } else {
                                saleObservation += " - " + TextFieldUtils.toTitleCase(payment.getDescription());
                            }
                        }

                        return checkoutMovementService.buildCheckoutMovement(
                                checkout, payment, saleObservation, checkoutMovementType);
                    })
                    .toList());

            // 5. Registrar troco (se necessário) -> ALTERAÇÃO: Adiciona à lista
            registerChangeIfNeeded(checkout, savedSale, saleObservationBase)
                    .ifPresent(checkoutMovements::add);

            // 6. Salvar todos os movimentos (Vendas + Troco)
            var checkoutMovementsWithGenKeys = checkoutMovementService.saveAll(checkoutMovements);

            // 7. Relacionar movimentos com a venda (Vendas + Troco)
            List<SaleCheckoutMovement> saleCheckoutMovements = checkoutMovementsWithGenKeys.stream()
                    .map(checkoutMovement -> saleCheckoutMovementService
                            .buildSaleCheckoutMovement(checkoutMovement, savedSale))
                    .toList();

            saleCheckoutMovementService.saveAll(saleCheckoutMovements);

            TransactionManagerV2.commit();

            return savedSale;

        } catch (SaleProcessingException | SaleValidationException | SalePaymentException e) {
            try {
                TransactionManagerV2.rollback();
            } catch (SQLException rollbackEx) {
                log.severe("Failed to perform rollback after SaleProcessingException.");
            }
            throw e;
        } catch (Exception e) {
            try {
                TransactionManagerV2.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            log.severe(e.getMessage());
            throw new SaleProcessingException("Erro inesperado ao processar a venda.", e);
        }
    }

    /**
     * Valida a política de troco:
     * - Pagamentos eletrônicos nunca podem exceder o valor da venda
     * - O valor em dinheiro deve ser suficiente para cobrir o troco
     */
    private void validateChangePolicy(Sale sale) throws SaleProcessingException {
        BigDecimal totalChange = sale.getTotalChange();

        if (totalChange.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal totalCash = sale.getPaymentMethods().stream()
                .filter(payment -> payment.getPaymentMethod() == PaymentMethod.DINHEIRO)
                .map(Payment::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalElectronic = sale.getPaymentMethods().stream()
                .filter(payment -> payment.getPaymentMethod() != PaymentMethod.DINHEIRO)
                .map(Payment::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalElectronic.compareTo(sale.getTotalPrice()) > 0) {
            throw new SaleProcessingException(
                    "Crédito/débito/Pix não podem ultrapassar o valor da venda."
            );
        }

        BigDecimal minCashNeeded = sale.getTotalPrice().subtract(totalElectronic);
        BigDecimal requiredCash = minCashNeeded.add(totalChange);

        if (totalCash.compareTo(requiredCash) < 0) {
            throw new SaleProcessingException(
                    "Dinheiro insuficiente para o troco. Precisa de R$ "
                            + requiredCash + ", tem R$ " + totalCash
            );
        }
    }

    /**
     * Constrói o movimento de troco (se necessário) e o retorna.
     * NÃO persiste o movimento diretamente.
     */
    private Optional<CheckoutMovement> registerChangeIfNeeded(Checkout checkout, Sale sale, String saleObservationBase) {
        BigDecimal totalChange = sale.getTotalChange();

        if (totalChange == null || totalChange.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        boolean hasCashPayment = sale.getPaymentMethods().stream()
                .anyMatch(payment -> payment.getPaymentMethod() == PaymentMethod.DINHEIRO);

        if (hasCashPayment) {
            Payment changePayment = new Payment(PaymentMethod.DINHEIRO);
            changePayment.setValue(totalChange);

            var changeMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.TROCO);
            String changeObservation = saleObservationBase + " - Troco";

            CheckoutMovement changeMovement = checkoutMovementService.buildCheckoutMovement(
                    checkout,
                    changePayment,
                    changeObservation,
                    changeMovementType
            );

            return Optional.of(changeMovement);
        }

        return Optional.empty();
    }

    public Sale save(Sale sale) throws SalePaymentException {
        SaleValidator.validate(sale);

        if(sale.getId() != null && saleRepository.find(sale.getId()).isPresent()){
            throw new IllegalArgumentException("Sale already exists.");
        }

        validateTotalPayment(sale);
        sale.setStatus(SaleStatus.APPROVED);
        var savedSale = saleRepository.add(sale);
        saveSaleProduct(savedSale);
        saveSalePayment(savedSale);
        return savedSale;
    }

    @Override
    public Sale updateSale(User user, Sale sale, Checkout checkout) throws SaleProcessingException {
        try {
            TransactionManagerV2.beginTransaction(connectionFactory);

            // 1) Validações Iniciais
            validateChangePolicy(sale);
            SaleValidator.validate(sale);
            validateTotalPayment(sale);

            // 2) Carregar estado atual do banco (Venda Antiga)
            var oldSaleOpt = saleCheckoutMovementService.findSaleDetailsBySaleId(sale.getId());
            Sale oldSale = oldSaleOpt.orElseThrow(() ->
                    new SaleProcessingException("Venda não encontrada para edição. ID=" + sale.getId())
            );

            // --- NOVO FLUXO DE ESTORNO ---
            // 3) Realizar o estorno dos pagamentos antigos no caixa ATUAL
            // Isso anula financeiramente a operação anterior sem apagar histórico.
            performPaymentReversal(oldSale.getId(), checkout, oldSale);

            // Preparar objeto editado
            var newSale = editSale(sale, oldSale);

            // 4) Atualizar Sale (UPDATE no banco)
            saleRepository.update(newSale);

            // 5) Reconciliar Estoque (Lógica já existente mantida)
            updateSaleProducts(newSale, oldSale);

            // 6) Atualizar Pagamentos (Salepayment)
            // Nota: Aqui mantemos a lógica de substituir os registros de SalePayment
            // para refletir o estado "atual" da venda, pois SalePayment não é ledger.
            updateSalePayments(newSale, oldSale);

            // 7) Registrar Novos Movimentos de Caixa (Como se fosse uma nova venda)
            // Isso cria as entradas "corrigidas" no caixa atual.
            // Reutilizamos a lógica de processamento de novos movimentos.
            processNewSaleMovements(checkout, newSale);

            TransactionManagerV2.commit();
            return newSale;

        } catch (SaleProcessingException | SaleValidationException | SalePaymentException e) {
            try { TransactionManagerV2.rollback(); } catch (SQLException ignored) {}
            throw e;
        } catch (Exception e) {
            try { TransactionManagerV2.rollback(); } catch (SQLException ignored) {}
            log.severe("Erro crítico ao editar venda: " + e.getMessage());
            throw new SaleProcessingException("Erro inesperado ao editar a venda.", e);
        }
    }

    //TODO TESTE UNITÁRIO
    private void updateSaleProducts(Sale newSale, Sale oldSale) {
        var oldItems = oldSale.getSaleProducts();
        var newItems = newSale.getSaleProducts();

        // Mapa para localizar itens novos rapidamente por product_id
        Map<Long, SaleProduct> newItemsMap = newItems.stream()
                .collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(),
                        sp -> sp,
                        (existing, replacement) -> replacement // se duplicado, pega o último
                ));

        Map<Long, SaleProduct> oldItemsMap = oldItems.stream()
                .collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(),
                        sp -> sp,
                        (existing, replacement) -> existing
                ));

        // 1) Processar itens antigos: repor estoque e marcar pra exclusão se não estiver no novo
        List<Long> idsToDelete = new ArrayList<>();
        for (SaleProduct oldItem : oldItems) {
            long productId = oldItem.getProduct().getId();
            long oldQty = oldItem.getQuantity();

            // Repor estoque do item antigo (porque ele foi "desfeito")
            productService.increaseQuantity(productId, oldQty);

            // Se não existe no novo conjunto, marcar para exclusão
            if (!newItemsMap.containsKey(productId)) {
                idsToDelete.add(oldItem.getId());
            }
        }

        // 2) Processar itens novos: baixar estoque e salvar/update
        List<SaleProduct> itemsToSave = new ArrayList<>();
        for (SaleProduct newItem : newItems) {
            long productId = newItem.getProduct().getId();
            long newQty = newItem.getQuantity();

            // Baixar estoque do item novo
            productService.decreaseQuantity(productId, newQty);

            // Se já existia, atualizar; senão, inserir
            SaleProduct oldItem = oldItemsMap.get(productId);
            if (oldItem != null) {
                newItem.setId(oldItem.getId()); // preserva ID para UPDATE
            }
            newItem.setSale(newSale);
            itemsToSave.add(newItem);
        }

        // 3) Persistir alterações
        if (!idsToDelete.isEmpty()) {
            saleProductService.deleteByIds(idsToDelete);
        }

        List<SaleProduct> itemsToInsert = new ArrayList<>();
        List<SaleProduct> itemsToUpdate = new ArrayList<>();

        for (SaleProduct item : itemsToSave) {
            if (item.getId() == null || item.getId() <= 0) {
                itemsToInsert.add(item);
            } else {
                itemsToUpdate.add(item);
            }
        }

        if (!itemsToInsert.isEmpty()) {
            saleProductService.saveAll(itemsToInsert);
        }

        if (!itemsToUpdate.isEmpty()) {
            saleProductService.updateAll(itemsToUpdate);
        }


    }

    private void updateSalePayments(Sale newSale, Sale oldSale){
        //Extrair pagamentos do objeto Sale e mapear por payment_id (PaymentMethod.id)
        Map<Long, Payment> oldPayMap = oldSale.getPaymentMethods().stream()
                .map(p -> (Payment) p)
                .collect(Collectors.toMap(
                        Payment::getId,
                        p -> p,
                        (existing, replacement) -> replacement
                ));

        Map<Long, Payment> newPayMap = newSale.getPaymentMethods().stream()
                .map(p -> (Payment) p)
                .collect(Collectors.toMap(
                        Payment::getId,
                        p -> p,
                        (existing, replacement) -> replacement
                ));

        List<Long> idsToDelete = new ArrayList<>();
        List<SalePayment> toInsert = new ArrayList<>();
        List<SalePayment> toUpdate = new ArrayList<>();

        // 2) Regras: comparar old vs new e preencher idsToDelete / toInsert / toUpdate

        List<SalePayment> oldLinks = salePaymentService.findBySaleId(oldSale.getId()); // precisa existir
        Map<Long, SalePayment> oldLinksMap = oldLinks.stream()
                .collect(Collectors.toMap(
                        SalePayment::getPaymentId,
                        sp -> sp,
                        (existing, replacement) -> existing
                ));

        Set<Long> paymentIds = new HashSet<>();
        paymentIds.addAll(oldPayMap.keySet());
        paymentIds.addAll(newPayMap.keySet());
        paymentIds.addAll(oldLinksMap.keySet());

        for (Long paymentId : paymentIds) {
            Payment oldPay = oldPayMap.get(paymentId);
            Payment newPay = newPayMap.get(paymentId);

            SalePayment oldLink = oldLinksMap.get(paymentId); // registro em salepayment (pode ser null)

            BigDecimal oldAmount = (oldPay != null && oldPay.getValue() != null) ? oldPay.getValue() : BigDecimal.ZERO;
            BigDecimal newAmount = (newPay != null && newPay.getValue() != null) ? newPay.getValue() : BigDecimal.ZERO;

            boolean oldPositive = oldAmount.compareTo(BigDecimal.ZERO) > 0;
            boolean newPositive = newAmount.compareTo(BigDecimal.ZERO) > 0;

            // A) old > 0 e new <= 0 => DELETE
            if (oldPositive && !newPositive) {
                if (oldLink != null) {
                    idsToDelete.add(oldLink.getId());
                }
                continue;
            }

            // B) old <= 0 e new > 0 => INSERT
            if (!oldPositive && newPositive) {
                // cria novo vínculo sale-payment
                SalePayment sp = new SalePayment(newPay, newSale);
                toInsert.add(sp);
                continue;
            }

            // C) old > 0 e new > 0 => UPDATE se mudou (amount ou installments no crédito)
            if (oldPositive && newPositive) {
                if (oldLink == null) {
                    // Se por algum motivo não existe vínculo no banco, cria (modo "auto-repair")
                    SalePayment sp = new SalePayment(newPay, newSale);
                    toInsert.add(sp);
                    continue;
                }

                boolean amountChanged = oldLink.getAmount() == null || oldLink.getAmount().compareTo(newAmount) != 0;

                boolean installmentsChanged = false;
                if (newPay.getPaymentMethod() == PaymentMethod.CREDIT0) {
                    installmentsChanged = oldLink.getInstallments() != newPay.getInstallments();
                }

                if (amountChanged || installmentsChanged) {
                    oldLink.setSale(newSale);
                    oldLink.setSaleId(newSale.getId());

                    oldLink.setPayment(newPay);
                    oldLink.setPaymentId(newPay.getId());

                    oldLink.setAmount(newAmount);

                    if (newPay.getPaymentMethod() == PaymentMethod.CREDIT0) {
                        oldLink.setInstallments(newPay.getInstallments());
                    } else {
                        oldLink.setInstallments(1);
                    }

                    toUpdate.add(oldLink);
                }
            }
        }

        // 3) Persistência (dentro da mesma transação do updateSale)
        if (!idsToDelete.isEmpty()) {
            salePaymentService.deleteByIds(idsToDelete);
        }

        if (!toUpdate.isEmpty()) {
            salePaymentService.updateAll(toUpdate);
        }

        if (!toInsert.isEmpty()) {
            salePaymentService.saveAll(toInsert);
        }

    }

    private void updateSaleCheckoutMovements(Checkout checkout, Sale savedSale) throws SaleProcessingException {
        if (checkout == null) throw new IllegalArgumentException("checkout não pode ser null.");
        if (savedSale == null) throw new IllegalArgumentException("savedSale não pode ser null.");
        if (savedSale.getId() == null) throw new IllegalArgumentException("savedSale.getId() não pode ser null.");

        // Regra: valida troco antes
        validateChangePolicy(savedSale);

        // 1) Identificar movimentos antigos (via vínculo)
        List<SaleCheckoutMovement> oldLinks = saleCheckoutMovementService.findBySaleId(savedSale.getId());
        if (oldLinks == null) oldLinks = List.of();

        List<Long> oldMovementIdsToRemove = oldLinks.stream()
                .map(SaleCheckoutMovement::getCheckoutMovement)
                .filter(cm -> cm != null && cm.getId() != null)
                .filter(cm -> shouldRemoveCheckoutMovementFromSale(cm, savedSale.getId()))
                .map(CheckoutMovement::getId)
                .distinct()
                .toList();

        // 2) Remover vínculos primeiro (FK safety)
        saleCheckoutMovementService.removeAllBySaleId(savedSale.getId());

        // 3) Remover movimentos antigos
        checkoutMovementService.removeAllByIds(oldMovementIdsToRemove);

        // 4) Criar movimentos novos (VENDA + TROCO, se houver)
        String saleObservationBase = "Venda " + savedSale.getId();
        boolean hasMultiplePayments = savedSale.getPaymentMethods() != null && savedSale.getPaymentMethods().size() > 1;

        var checkoutMovementTypeSale = new CheckoutMovementType(CheckoutMovementTypeEnum.VENDA);

        List<CheckoutMovement> newMovements = (savedSale.getPaymentMethods() == null ? List.<Payment>of() : savedSale.getPaymentMethods())
                .stream()
                .map(payment -> {
                    String saleObservation = saleObservationBase;

                    if (hasMultiplePayments && payment != null) {
                        if (payment.getPaymentMethod() == PaymentMethod.CREDIT0) {
                            saleObservation += payment.getInstallments() > 1
                                    ? " - Crédito (" + payment.getInstallments() + "x)"
                                    : " - Crédito (à vista)";
                        } else {
                            saleObservation += " - " + TextFieldUtils.toTitleCase(payment.getDescription());
                        }
                    }

                    return checkoutMovementService.buildCheckoutMovement(
                            checkout,
                            payment,
                            saleObservation,
                            checkoutMovementTypeSale
                    );
                })
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        buildChangeMovementIfNeeded(checkout, savedSale, saleObservationBase)
                .ifPresent(newMovements::add);

        // 5) Persistir movimentos
        List<CheckoutMovement> newMovementsWithIds = checkoutMovementService.saveAll(newMovements);

        // 6) Recriar vínculos sale <-> checkout_movement
        List<SaleCheckoutMovement> newLinks = newMovementsWithIds.stream()
                .map(cm -> saleCheckoutMovementService.buildSaleCheckoutMovement(cm, savedSale))
                .toList();

        saleCheckoutMovementService.saveAll(newLinks);
    }

    private Optional<CheckoutMovement> buildChangeMovementIfNeeded(Checkout checkout, Sale sale, String saleObservationBase) {
        if (checkout == null || sale == null) return java.util.Optional.empty();

        BigDecimal totalChange = sale.getTotalChange();
        if (totalChange == null || totalChange.compareTo(BigDecimal.ZERO) <= 0) return java.util.Optional.empty();

        boolean hasCashPayment = sale.getPaymentMethods() != null
                && sale.getPaymentMethods().stream()
                .anyMatch(p -> p != null && p.getPaymentMethod() == PaymentMethod.DINHEIRO);

        if (!hasCashPayment) return java.util.Optional.empty();

        Payment changePayment = new Payment(PaymentMethod.DINHEIRO);
        changePayment.setValue(totalChange);

        var changeMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.SAIDA);
        String changeObservation = saleObservationBase + " - Troco";

        CheckoutMovement changeMovement = checkoutMovementService.buildCheckoutMovement(
                checkout,
                changePayment,
                changeObservation,
                changeMovementType
        );

        return java.util.Optional.of(changeMovement);
    }

    private Sale editSale(Sale editedSale, Sale oldSale) {
        if (editedSale == null) throw new IllegalArgumentException("editedSale is null.");
        if (oldSale == null) throw new IllegalArgumentException("oldSale is null.");

        editedSale.setId(oldSale.getId());
        editedSale.setDateSale(oldSale.getDateSale());
        editedSale.setStatus(oldSale.getStatus());
        editedSale.setDataCancel(oldSale.getDataCancel());

        return editedSale;
    }

    private boolean shouldRemoveCheckoutMovementFromSale(CheckoutMovement cm, Long saleId) {
        if (cm == null || saleId == null) return false;

        // Segurança: só removemos movimentos que estejam claramente associados à venda pelo texto padrão
        String obs = cm.getObs() == null ? "" : cm.getObs();

        boolean looksLikeThisSale = obs.contains("Venda " + saleId);
        if (!looksLikeThisSale) return false;

        long typeId = cm.getMovementType() != null ? cm.getMovementType().getId() : -1L;

        boolean isVenda = typeId == CheckoutMovementTypeEnum.VENDA.getId();
        boolean isTroco = typeId == CheckoutMovementTypeEnum.SAIDA.getId()
                && obs.toLowerCase().contains("troco");

        return isVenda || isTroco;
    }

    /**
     * Executa o fluxo completo de estorno: recupera, fabrica, salva e vincula.
     */
    private void performPaymentReversal(Long saleId, Checkout currentCheckout, Sale saleReference) {
        log.info("Iniciando processo de estorno para Venda #" + saleId); // LOG NOVO

        // 1, 2 e 3: Recuperação, Filtro e Fabricação (Lógica anterior)
        List<CheckoutMovement> reversalMovements = reversePayments(saleId, currentCheckout);

        log.info("Total de movimentos de estorno gerados (pré-persisência): " + reversalMovements.size()); // LOG NOVO

        if (reversalMovements.isEmpty()) {
            log.warning("Nenhum movimento de estorno foi gerado. Verifique se a venda possui movimentos vinculados."); // LOG NOVO
            return;
        }

        // Passo 4: Persistência (Save)
        // O service retorna os objetos salvos com ID gerado
        List<CheckoutMovement> savedReversals = checkoutMovementService.saveAll(reversalMovements);

        log.info("Estornos persistidos com sucesso. Quantidade: " + savedReversals.size()); // LOG NOVO

        // Passo 5: Vinculação de Rastreabilidade (Link)
        // Criar o vínculo na tabela intermediária (sale_checkoutmovement)
        List<SaleCheckoutMovement> reversalLinks = savedReversals.stream()
                .map(movement -> saleCheckoutMovementService.buildSaleCheckoutMovement(movement, saleReference))
                .toList();

        saleCheckoutMovementService.saveAll(reversalLinks);
    }

    /**
     * Estorna (reverte) os movimentos de caixa originais de uma venda.
     * Gera movimentos compensatórios (ESTORNO) no caixa ATUAL.
     *
     * @param saleId ID da venda original
     * @param currentCheckout Caixa aberto do usuário que está editando
     * @return Lista de novos CheckoutMovement (tipo ESTORNO) prontos para persistência
     */
    private List<CheckoutMovement> reversePayments(Long saleId, Checkout currentCheckout) {
        if (currentCheckout == null) {
            throw new IllegalArgumentException("Caixa atual não pode ser nulo para realizar estorno.");
        }

        List<SaleCheckoutMovement> saleCheckoutMovements =
                saleCheckoutMovementService.findBySaleId(saleId);

        if (saleCheckoutMovements == null || saleCheckoutMovements.isEmpty()) {
            log.warning("Nenhum vínculo (SaleCheckoutMovement) encontrado no banco para a Venda #" + saleId); // LOG NOVO
            return List.of();
        }

        log.info("Encontrados " + saleCheckoutMovements.size() + " vínculos de movimentos originais."); // LOG NOVO

        String reversalObsBase = "Estorno - Edição Venda #" + saleId;

        return saleCheckoutMovements.stream()
                .map(SaleCheckoutMovement::getCheckoutMovement)
                .filter(cm -> cm != null && cm.getMovementType() != null)
                .map(originalMovement -> {
                    long typeId = originalMovement.getMovementType().getId();
                    CheckoutMovementTypeEnum originalType = CheckoutMovementTypeEnum.getById(typeId);
                    CheckoutMovementTypeEnum targetTypeEnum = null;

                    log.info(String.format("Analisando Movimento Original ID: %d | Tipo: %s (%d) | Valor: %s",
                            originalMovement.getId(), originalType.getName(), typeId, originalMovement.getValue()));

                    // Lógica de Decisão Robusta
                    if (originalType == CheckoutMovementTypeEnum.VENDA) {
                        // Anular Entrada -> Saída (Estorno)
                        targetTypeEnum = CheckoutMovementTypeEnum.ESTORNO;

                    } else if (originalType == CheckoutMovementTypeEnum.TROCO) {
                        // Anular Saída (Troco Novo) -> Entrada (Estorno Troco)
                        targetTypeEnum = CheckoutMovementTypeEnum.ESTORNO_TROCO;

                    } else if (originalType == CheckoutMovementTypeEnum.SAIDA) {
                        // Anular Saída -> Entrada (Estorno Troco)
                        targetTypeEnum = CheckoutMovementTypeEnum.ESTORNO_TROCO;
                    }

                    // Se não for um tipo estornável, retorna null para filtrar
                    if (targetTypeEnum == null) {
                        log.info(">> Movimento ID " + originalMovement.getId() + " ignorado (Tipo não estornável)."); // LOG NOVO
                        return null;
                    }

                    log.info(">> Gerando Estorno do Tipo: " + targetTypeEnum.getName()); // LOG NOVO

                    var reversalType = new CheckoutMovementType(targetTypeEnum);

                    // Clonagem do pagamento (workaround para o bug do repositório, se necessário)
                    Payment paymentClone = new Payment(originalMovement.getPayment().getPaymentMethod());
                    paymentClone.setId(originalMovement.getPayment().getId());
                    paymentClone.setDescription(originalMovement.getPayment().getDescription());

                    CheckoutMovement reversalMovement = checkoutMovementService.buildCheckoutMovement(
                            currentCheckout,
                            paymentClone,
                            reversalObsBase,
                            reversalType
                    );

                    // Garante valor correto
                    reversalMovement.setValue(originalMovement.getValue());

                    return reversalMovement;
                })
                .filter(java.util.Objects::nonNull) // Remove os nulos
                .toList();
    }

    /**
     * Processa, salva e vincula os NOVOS movimentos financeiros de uma venda (Pagamentos + Troco).
     * Utilizado tanto na criação quanto na edição de vendas.
     *
     * @param checkout O caixa onde os movimentos serão registrados (contexto atual).
     * @param sale A venda contendo os pagamentos e valores atualizados.
     */
    private void processNewSaleMovements(Checkout checkout, Sale sale) {
        List<CheckoutMovement> movementsToSave = new ArrayList<>();

        // 1. Configuração Base para Movimentos de VENDA
        var saleType = new CheckoutMovementType(CheckoutMovementTypeEnum.VENDA);
        String saleObservationBase = "Venda #" + sale.getId();
        boolean hasMultiplePayments = sale.getPaymentMethods().size() > 1;

        // 2. Criar Movimentos de PAGAMENTO (Entradas)
        List<CheckoutMovement> paymentMovements = sale.getPaymentMethods().stream()
                .map(payment -> {
                    String observation = saleObservationBase;

                    if (hasMultiplePayments || payment.getPaymentMethod() == PaymentMethod.CREDIT0) {
                        if (payment.getPaymentMethod() == PaymentMethod.CREDIT0) {
                            observation += (payment.getInstallments() > 1)
                                    ? " - Crédito " + payment.getInstallments() + "x"
                                    : " - Crédito à vista";
                        } else {
                            observation += " - " + TextFieldUtils.toTitleCase(payment.getDescription());
                        }
                    }

                    return checkoutMovementService.buildCheckoutMovement(
                            checkout,
                            payment,
                            observation,
                            saleType
                    );
                })
                .toList();

        movementsToSave.addAll(paymentMovements);

        // 3. Criar Movimento de TROCO, se necessário
        BigDecimal totalChange = sale.getTotalChange();
        boolean hasCashPayment = sale.getPaymentMethods().stream()
                .anyMatch(p -> p.getPaymentMethod() == PaymentMethod.DINHEIRO);

        if (totalChange != null && totalChange.compareTo(BigDecimal.ZERO) > 0 && hasCashPayment) {

            Payment changePayment = new Payment(PaymentMethod.DINHEIRO);
            changePayment.setValue(totalChange);

            // CORREÇÃO: Usa o novo tipo específico TROCO (ID 8)
            var changeType = new CheckoutMovementType(CheckoutMovementTypeEnum.TROCO);
            String changeObservation = saleObservationBase + " - Troco";

            CheckoutMovement changeMovement = checkoutMovementService.buildCheckoutMovement(
                    checkout,
                    changePayment,
                    changeObservation,
                    changeType
            );

            movementsToSave.add(changeMovement);
        }

        // 4. Persistência em Lote
        if (movementsToSave.isEmpty()) return;

        List<CheckoutMovement> savedMovements = checkoutMovementService.saveAll(movementsToSave);

        // 5. Vinculação
        List<SaleCheckoutMovement> links = savedMovements.stream()
                .map(movement -> saleCheckoutMovementService.buildSaleCheckoutMovement(movement, sale))
                .toList();

        saleCheckoutMovementService.saveAll(links);
    }


    @Override
    public BigDecimal calculateTotalProfit(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalProfit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Set<Long> processedSaleIds = new HashSet<>();

        for (Sale sale : sales) {
            if (sale.getStatus() == SaleStatus.CANCELED ||
                    sale.getSaleProducts() == null ||
                    sale.getSaleProducts().isEmpty() ||
                    !processedSaleIds.add(sale.getId())) {
                continue;
            }

            BigDecimal totalCost = BigDecimal.ZERO;
            for (SaleProduct saleProduct : sale.getSaleProducts()) {
                Product product = saleProduct.getProduct();
                if (product != null) {
                    BigDecimal productCost = product.getCostPrice()
                            .multiply(BigDecimal.valueOf(saleProduct.getQuantity()));
                    totalCost = totalCost.add(productCost);
                }
            }

            BigDecimal saleFinalPrice = sale.getTotalPrice();
            BigDecimal saleProfit = saleFinalPrice.subtract(totalCost);

            totalProfit = totalProfit.add(saleProfit);
        }

        return totalProfit.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalCost(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalCost = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        // Add a Set to track processed sale IDs
        Set<Long> processedSaleIds = new HashSet<>();

        for (Sale sale : sales) {
            // Skip if sale is canceled, has no products, or has already been processed
            if (sale.getStatus() == SaleStatus.CANCELED ||
                    sale.getSaleProducts() == null ||
                    sale.getSaleProducts().isEmpty() ||
                    !processedSaleIds.add(sale.getId())) {
                continue;
            }

            for (SaleProduct saleProduct : sale.getSaleProducts()) {
                Product product = saleProduct.getProduct();
                if (product != null) {
                    // Calculate cost for this product = cost price * quantity
                    BigDecimal productCost = product.getCostPrice()
                            .multiply(BigDecimal.valueOf(saleProduct.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);

                    totalCost = totalCost.add(productCost);
                }
            }
        }

        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalSales(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // Uses Set to avoiding duplicate sales
        Set<Long> processedSaleIds = new HashSet<>();
        BigDecimal totalSales = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (Sale sale : sales) {
            // Skip if the sale is canceled or has already been processed
            if (sale.getStatus() == SaleStatus.CANCELED || !processedSaleIds.add(sale.getId())) {
                continue;
            }

            totalSales = totalSales.add(sale.getTotalPrice());
        }

        return totalSales.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateAverageSale(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long count = countSales(sales);
        if (count == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = calculateTotalSales(sales);
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    @Override
    public long countSales(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return 0;
        }

        Set<Long> uniqueSaleIds = new HashSet<>();

        for (Sale sale : sales) {
            if (sale.getStatus() != SaleStatus.CANCELED) {
                uniqueSaleIds.add(sale.getId());
            }
        }
        return uniqueSaleIds.size();
    }

    private void validateTotalPayment (Sale sale) throws SalePaymentException {
        BigDecimal totalAmount = BigDecimal.ZERO;

        var listPayment = new ArrayList<>(sale.getPaymentMethods());

        for (Payment payment : listPayment) {
            totalAmount = totalAmount.add(payment.getValue());
        }

        if (totalAmount.compareTo(sale.getTotalPrice()) < 0) {
            throw new SalePaymentException("Error processing payment: Total amount of payments is less than the sale total price. Total Payments: " + totalAmount + ", Sale Total: " + sale.getTotalPrice());
        }

    }

    private void saveSaleProduct(Sale sale){
        var saleProducts = sale.getSaleProducts();
        saleProducts.forEach(saleProduct -> {
            saleProduct.setSale(sale);
            var prod = saleProduct.getProduct();
            var qtd = saleProduct.getQuantity();
            decreaseStockQtd(prod,qtd);
        });
        saleProductService.saveAll(saleProducts);
    }

    private void saveSalePayment(Sale sale){
        var salePayments = sale.getPaymentMethods().stream()
                .map(payment -> new SalePayment(payment,sale))
                .toList();
        salePaymentService.saveAll(salePayments);
    }

    private void decreaseStockQtd(Product product, long quantity){
        productService.decreaseQuantity(product.getId(), quantity);
    }


}
