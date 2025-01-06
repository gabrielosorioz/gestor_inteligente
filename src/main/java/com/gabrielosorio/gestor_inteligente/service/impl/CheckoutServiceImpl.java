package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.exception.CheckoutNotFoundException;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.TypeCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.CheckoutRepository;
import com.gabrielosorio.gestor_inteligente.service.CheckoutMovementService;
import com.gabrielosorio.gestor_inteligente.service.CheckoutService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

        CheckoutMovement checkoutMovement = checkoutMovementService.buildCheckoutMovement(checkout, payment, obs, TypeCheckoutMovement.FUNDO_DE_CAIXA);
        checkoutMovementService.addMovement(checkoutMovement);

        updateInitialCash(checkout, payment.getValue());
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
