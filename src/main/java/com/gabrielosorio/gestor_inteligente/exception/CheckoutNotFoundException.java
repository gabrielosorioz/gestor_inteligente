package com.gabrielosorio.gestor_inteligente.exception;

public class CheckoutNotFoundException extends RuntimeException {
    public CheckoutNotFoundException(long checkoutId) {
        super("Checkout not found for ID: " + checkoutId);
    }
}
