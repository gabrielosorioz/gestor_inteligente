package com.gabrielosorio.gestor_inteligente.service.base;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.model.Payment;

import java.util.List;


public interface CheckoutMovementService extends TransactionalService {

    CheckoutMovement addMovement(CheckoutMovement movement);
    List<CheckoutMovement> saveAll(List<CheckoutMovement> checkoutMovements);
    CheckoutMovement buildCheckoutMovement(Checkout checkout, Payment payment, String obs, CheckoutMovementType checkoutMovementType);
}
