package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.util.Optional;

public interface CheckoutRepository extends RepositoryStrategy<Checkout> {
    /**
     * Finds the open checkout for today, if any.
     *
     * @return An {@code Optional<Checkout>} containing the open checkout for today,
     *         or an empty Optional if none exists.
     */

    Optional<Checkout> findOpenCheckoutForToday();
}
