package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.model.enums.TypeCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.CheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.service.CheckoutMovementService;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutMovementServiceImpl implements CheckoutMovementService {

    private final CheckoutMovementRepository checkoutMovementRepository;

    public CheckoutMovementServiceImpl(CheckoutMovementRepository checkoutMovementRepository) {
        this.checkoutMovementRepository = checkoutMovementRepository;
    }

    @Override
    public List<CheckoutMovement> saveAll(List<CheckoutMovement> checkoutMovements) {
        return checkoutMovementRepository.addAll(checkoutMovements);
    }

    @Override
    public CheckoutMovement addMovement(CheckoutMovement movement) {
        movement.setDateTime(LocalDateTime.now());
        return checkoutMovementRepository.add(movement);
    }


    @Override
    public CheckoutMovement buildCheckoutMovement(Checkout checkout, Payment payment, String obs, TypeCheckoutMovement type) {
        CheckoutMovement checkoutMovement = new CheckoutMovement();
        checkoutMovement.setCheckout(checkout);
        checkoutMovement.setPayment(payment);
        checkoutMovement.setValue(payment.getValue());
        checkoutMovement.setObs(obs);
        checkoutMovement.setType(type);
        checkoutMovement.setDateTime(LocalDateTime.now());
        return checkoutMovement;
    }

}





