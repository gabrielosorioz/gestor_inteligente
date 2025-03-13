package com.gabrielosorio.gestor_inteligente.service.base;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.model.Payment;

import java.util.List;

/**
 * Service interface for managing {@link CheckoutMovement} entities.
 * Provides methods to add, retrieve, and build checkout movements.
 */
public interface CheckoutMovementService extends TransactionalService {

    /**
     * Adds a new checkout movement to the system.
     *
     * @param movement the checkout movement to be added
     * @return the saved checkout movement
     */
    CheckoutMovement addMovement(CheckoutMovement movement);

    /**
     * Saves a list of checkout movements in bulk.
     *
     * @param checkoutMovements the list of checkout movements to be saved
     * @return the list of saved checkout movements
     */
    List<CheckoutMovement> saveAll(List<CheckoutMovement> checkoutMovements);

    /**
     * Builds a new {@link CheckoutMovement} instance based on the provided parameters.
     *
     * @param checkout the checkout associated with the movement
     * @param payment the payment details for the movement
     * @param obs the observation or note about the movement
     * @param checkoutMovementType the type of movement (e.g., ENTRY, EXIT)
     * @return a new {@link CheckoutMovement} instance
     */
    CheckoutMovement buildCheckoutMovement(Checkout checkout, Payment payment, String obs, CheckoutMovementType checkoutMovementType);

    /**
     * Retrieves a list of all checkout movements associated with a specific checkout by its ID.
     *
     * @param checkoutId the ID of the checkout
     * @return a list of {@link CheckoutMovement} objects associated with the checkout.
     *         If no movements are found, an empty list is returned.
     */
    List<CheckoutMovement> findByCheckoutId(long checkoutId);

    List<CheckoutMovement> findBySaleId(long id);
}