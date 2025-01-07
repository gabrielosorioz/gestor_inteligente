package com.gabrielosorio.gestor_inteligente.service;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.model.enums.TypeCheckoutMovement;

import java.util.List;


public interface CheckoutMovementService {

    CheckoutMovement addMovement(CheckoutMovement movement);
    List<CheckoutMovement> saveAll(List<CheckoutMovement> checkoutMovements);
    CheckoutMovement buildCheckoutMovement(Checkout checkout, Payment payment, String obs, TypeCheckoutMovement type);
}
