package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.User;

public interface CheckoutService {

    /**
     * Opens a new checkout for a user. If there is already an open checkout for today, it returns it.
     *
     * @param user the user who is opening the checkout
     * @return the opened checkout, either an existing one or a new one
     */
    Checkout openCheckout(User user);

    /**
     * Sets the initial cash for a checkout and records a fund movement.
     *
     * @param checkoutId the ID of the checkout
     * @param payment the payment details for the initial cash
     * @param obs the observation or note about the cash movement
     */
    void setInitialCash(long checkoutId, Payment payment, String obs);
}
