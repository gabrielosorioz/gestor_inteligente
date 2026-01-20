package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.exception.CheckoutNotFoundException;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutRepository;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutMovementService;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CheckoutServiceImpl implements CheckoutService {

    private final CheckoutRepository checkoutRepository;
    private final CheckoutMovementService checkoutMovementService;

    public CheckoutServiceImpl(CheckoutRepository checkoutRepository, CheckoutMovementService checkoutMovementService) {
        this.checkoutRepository = checkoutRepository;
        this.checkoutMovementService = checkoutMovementService;
    }

    @Override
    public Checkout openCheckout(User user) {
        return checkoutRepository.findOpenCheckoutForToday()
                .orElseGet(() -> createNewCheckout(user));
    }

    @Override
    public void setInitialCash(long checkoutId, Payment payment, String obs) {
        Checkout checkout = validateCheckoutExists(checkoutId);

        BigDecimal previousInitialCash = checkout.getInitialCash();
        BigDecimal newInitialCash = payment.getValue();

        BigDecimal difference = newInitialCash.subtract(previousInitialCash);

        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        CheckoutMovementTypeEnum movementTypeEnum;
        if (previousInitialCash.compareTo(BigDecimal.ZERO) == 0) {
            movementTypeEnum = CheckoutMovementTypeEnum.FUNDO_DE_CAIXA;
        } else if (difference.compareTo(BigDecimal.ZERO) > 0) {
            movementTypeEnum = CheckoutMovementTypeEnum.AJUSTE_POSITIVO;
        } else {
            movementTypeEnum = CheckoutMovementTypeEnum.AJUSTE_NEGATIVO;
            difference = difference.abs(); // Valor positivo para registro
        }

        var checkoutMovementType = new CheckoutMovementType(movementTypeEnum);
        Payment adjustmentPayment = new Payment(payment.getPaymentMethod(), difference);

        String finalObs = obs;
        if (previousInitialCash.compareTo(BigDecimal.ZERO) > 0) {
            finalObs = String.format("%s (Alteração: %s → %s)",
                    obs,
                    previousInitialCash.toPlainString(),
                    newInitialCash.toPlainString()
            );
        }

        CheckoutMovement checkoutMovement = checkoutMovementService.buildCheckoutMovement(
                checkout,
                adjustmentPayment,
                finalObs,
                checkoutMovementType
        );

        checkoutMovementService.addMovement(checkoutMovement);
        updateInitialCash(checkout, newInitialCash);
    }

    @Override
    public void addCashInflow(long checkoutId, Payment payment, String obs) {
        Checkout checkout = validateCheckoutExists(checkoutId);
        if (payment.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da entrada deve ser maior que zero");
        }

        var checkoutMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.ENTRADA);

        CheckoutMovement checkoutMovement = checkoutMovementService.buildCheckoutMovement(
                checkout, payment, obs,
                checkoutMovementType);

        checkoutMovementService.addMovement(checkoutMovement);
        updateTotalEntry(checkout, payment.getValue());
    }

    @Override
    public void addCashOutflow(long checkoutId, Payment payment, String obs) {
        Checkout checkout = validateCheckoutExists(checkoutId);

        if (payment.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da saída deve ser maior que zero");
        }

        BigDecimal currentCashTotal = calculateCurrentCashTotal(checkout);
        if (currentCashTotal.compareTo(payment.getValue()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Saldo insuficiente. Disponível: %s, Solicitado: %s",
                            currentCashTotal, payment.getValue()));
        }

        var checkoutMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.SAIDA);
        CheckoutMovement checkoutMovement = checkoutMovementService.buildCheckoutMovement(
                checkout, payment, obs, checkoutMovementType);

        checkoutMovementService.addMovement(checkoutMovement);

        updateTotalExit(checkout, payment.getValue());
    }

    private BigDecimal calculateCurrentCashTotal(Checkout checkout) {
        return checkout.getInitialCash()
                .add(checkout.getTotalEntry())
                .subtract(checkout.getTotalExit());
    }

    private void updateTotalExit(Checkout checkout, BigDecimal exitAmount) {
        if (exitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de saída deve ser positivo");
        }

        BigDecimal currentTotal = checkout.getTotalExit();
        BigDecimal newTotal = currentTotal.add(exitAmount);

        checkout.setTotalExit(newTotal);
        checkout.setUpdatedAt(LocalDateTime.now());

        checkoutRepository.update(checkout);
    }

    public void validateCheckoutConsistency(long checkoutId) {
        Checkout checkout = validateCheckoutExists(checkoutId);

        List<CheckoutMovement> movements = checkoutMovementService.findByCheckoutId(checkoutId);

        BigDecimal calculatedEntry = movements.stream()
                .filter(m -> m.getMovementType().equals(CheckoutMovementTypeEnum.ENTRADA))
                .map(m -> m.getPayment().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal calculatedExit = movements.stream()
                .filter(m -> m.getMovementType().equals(CheckoutMovementTypeEnum.SAIDA))
                .map(m -> m.getPayment().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!calculatedEntry.equals(checkout.getTotalEntry()) ||
                !calculatedExit.equals(checkout.getTotalExit())) {
            throw new IllegalStateException("Inconsistência detectada nos totais do checkout");
        }
    }

    @Override
    public List<CheckoutMovement> findCheckoutMovementsById(long id) {
        return checkoutMovementService.findByCheckoutId(id);
    }

    @Override
    public Optional<Checkout> findById(Long id) {
        return checkoutRepository.find(id);
    }

    private Checkout validateCheckoutExists(long checkoutId) {
        return checkoutRepository.find(checkoutId)
                .orElseThrow(() -> new CheckoutNotFoundException(checkoutId));
    }

    private void updateInitialCash(Checkout checkout, BigDecimal initialCash) {
        checkout.setInitialCash(initialCash);
        checkoutRepository.update(checkout);
    }

    private void updateTotalEntry(Checkout checkout, BigDecimal entryAmount) {
        BigDecimal currentTotal = checkout.getTotalEntry();
        BigDecimal newTotal = currentTotal.add(entryAmount);
        checkout.setTotalEntry(newTotal);
        checkoutRepository.update(checkout);
    }

    private Checkout createNewCheckout(User user) {
        Checkout checkout = new Checkout();
        LocalDateTime now = LocalDateTime.now();

        checkout.setOpenedAt(now);
        checkout.setCreatedAt(now);
        checkout.setUpdatedAt(now);
        checkout.setStatus(CheckoutStatus.OPEN);
        checkout.setTotalExit(BigDecimal.ZERO);
        checkout.setClosingBalance(BigDecimal.ZERO);
        checkout.setOpenedBy(user);

        return checkoutRepository.add(checkout);
    }

}
