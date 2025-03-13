package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutMovementRepo;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementByCheckoutId;
import com.gabrielosorio.gestor_inteligente.service.base.AbstractTransactionalService;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutMovementService;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutMovementServiceImpl extends AbstractTransactionalService<CheckoutMovement> implements CheckoutMovementService {

    private final CheckoutMovementRepo REPOSITORY;

    public CheckoutMovementServiceImpl(CheckoutMovementRepo checkoutMovementRepository) {
        super(checkoutMovementRepository instanceof Repository ? (Repository<CheckoutMovement>) checkoutMovementRepository : null);
        if (checkoutMovementRepository == null) {
            throw new IllegalArgumentException("checkoutMovementRepository must be an instance of Repository<CheckoutMovement>");
        }
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
    public List<CheckoutMovement> findBySaleId(long saleId) {
        return REPOSITORY.findCheckoutMovementBySaleId(saleId);
    }

}





