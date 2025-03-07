package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.repository.CheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.repository.Repository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementByCheckoutId;
import com.gabrielosorio.gestor_inteligente.service.AbstractTransactionalService;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutMovementService;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutMovementServiceImpl extends AbstractTransactionalService<CheckoutMovement> implements CheckoutMovementService {

    private final Repository<CheckoutMovement> REPOSITORY;

    public CheckoutMovementServiceImpl(CheckoutMovementRepository checkoutMovementRepository) {
       super(checkoutMovementRepository);
       REPOSITORY = getRepository();
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
        return REPOSITORY.findBySpecification(new FindCheckoutMovementByCheckoutId(checkoutId));
    }

}





