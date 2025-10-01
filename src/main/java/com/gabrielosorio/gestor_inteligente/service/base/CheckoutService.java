package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.User;

import java.util.List;
import java.util.Optional;

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

    void addCashInflow(long checkoutId, Payment payment, String obs);

    void addCashOutflow(long checkoutId, Payment payment, String obs);

    /**
     * Retrieves a list of all cash movements associated with a specific checkout by its ID.
     * This method is useful for auditing, tracking, or displaying the history of cash movements
     * (e.g., entries, exits, or adjustments) for a given checkout session.
     *
     * @param id the ID of the checkout for which the movements are to be retrieved
     * @return a list of {@link CheckoutMovement} objects representing the cash movements
     *         associated with the checkout. If no movements are found, an empty list is returned.
     *
     * @throws IllegalArgumentException if the provided ID is invalid (e.g., less than or equal to 0)
     */
    List<CheckoutMovement> findCheckoutMovementsById(long id);

    Optional<Checkout> findById(Long id);



}
