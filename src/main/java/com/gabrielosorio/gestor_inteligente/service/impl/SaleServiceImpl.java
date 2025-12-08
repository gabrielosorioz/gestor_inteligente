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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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

            List<CheckoutMovement> checkoutMovements = savedSale.getPaymentMethods().stream()
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
                    .toList();

            // 5. Registrar troco (se necessário)
            registerChangeIfNeeded(checkout, savedSale, saleObservationBase);

            // 6. Salvar todos os movimentos
            var checkoutMovementsWithGenKeys = checkoutMovementService.saveAll(checkoutMovements);

            // 7. Relacionar movimentos com a venda
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
     * Registra automaticamente uma saída de troco quando:
     * - Há troco (valor recebido > valor da venda)
     * - Pagamento inclui DINHEIRO
     */
    private void registerChangeIfNeeded(Checkout checkout, Sale sale, String saleObservationBase) {
        BigDecimal totalChange = sale.getTotalChange();

        // Se não há troco, não há o que registrar
        if (totalChange.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Verificar se há pagamento em DINHEIRO
        boolean hasCashPayment = sale.getPaymentMethods().stream()
                .anyMatch(payment -> payment.getPaymentMethod() == PaymentMethod.DINHEIRO);

        if (hasCashPayment) {
            // Criar pagamento representando o troco em dinheiro
            Payment changePayment = new Payment(PaymentMethod.DINHEIRO);
            changePayment.setValue(totalChange);

            // Criar movimento de SAÍDA para o troco
            var changeMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.SAIDA);
            String changeObservation = saleObservationBase + " - Troco";

            CheckoutMovement changeMovement = checkoutMovementService.buildCheckoutMovement(
                    checkout,
                    changePayment,
                    changeObservation,
                    changeMovementType
            );

            // Salvar o movimento de troco
            checkoutMovementService.saveAll(List.of(changeMovement));
        }
    }

    public Sale save(Sale sale) throws SalePaymentException {
        SaleValidator.validate(sale);

        if(saleRepository.find(sale.getId()).isPresent()){
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
    public BigDecimal calculateTotalProfit(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalProfit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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
                    // Calculate profit for this product = (selling price - cost price) * quantity
                    BigDecimal unitProfit = saleProduct.getUnitPrice().subtract(product.getCostPrice());
                    BigDecimal productProfit = unitProfit.multiply(BigDecimal.valueOf(saleProduct.getQuantity()));

                    // Apply any discounts at the product level
                    productProfit = productProfit.subtract(saleProduct.getDiscount());

                    // Ensure profit doesn't go below zero for this product
                    productProfit = productProfit.max(BigDecimal.ZERO);

                    totalProfit = totalProfit.add(productProfit);
                }
            }
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
            System.out.println("Preço final: R$" + sale.getTotalPrice());

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
