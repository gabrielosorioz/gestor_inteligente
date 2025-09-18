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
        var checkoutMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.FUNDO_DE_CAIXA);
        CheckoutMovement checkoutMovement = checkoutMovementService.buildCheckoutMovement(checkout, payment, obs, checkoutMovementType);
        checkoutMovementService.addMovement(checkoutMovement);
        updateInitialCash(checkout, payment.getValue());
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


    private void updateInitialCash(Checkout checkout, BigDecimal initialCash) {
        checkout.setInitialCash(initialCash);
        checkoutRepository.update(checkout);
    }
}
