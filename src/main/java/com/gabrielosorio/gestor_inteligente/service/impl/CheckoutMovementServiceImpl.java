package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutMovementService;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutMovementServiceImpl implements CheckoutMovementService {

    private final CheckoutMovementRepository REPOSITORY;

    public CheckoutMovementServiceImpl(CheckoutMovementRepository checkoutMovementRepository) {
        REPOSITORY = checkoutMovementRepository;
    }

    @Override
    public List<CheckoutMovement> saveAll(List<CheckoutMovement> checkoutMovements) {
        return REPOSITORY.addAll(checkoutMovements);
    }

    @Override
    public CheckoutMovement addMovement(CheckoutMovement movement) {
        movement.setDateTime(LocalDateTime.now());
        return REPOSITORY.add(movement);
    }

    @Override
    public CheckoutMovement buildCheckoutMovement(Checkout checkout, Payment payment, String obs, CheckoutMovementType checkoutMovementType) {
        CheckoutMovement checkoutMovement = new CheckoutMovement();
        checkoutMovement.setCheckout(checkout);
        checkoutMovement.setPayment(payment);
        checkoutMovement.setValue(payment.getValue());
        checkoutMovement.setObs(obs);
        checkoutMovement.setMovementType(checkoutMovementType);
        checkoutMovement.setDateTime(LocalDateTime.now());
        return checkoutMovement;
    }

    @Override
    public List<CheckoutMovement> findByCheckoutId(long checkoutId) {
        return REPOSITORY.findCheckoutMovementByCheckoutId(checkoutId);
    }

    @Override
    public List<CheckoutMovement> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("A data de início e a data de fim não podem ser nulas");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de fim");
        }

        return REPOSITORY.findCheckoutMovementByDateRange(startDate, endDate);
    }

}





