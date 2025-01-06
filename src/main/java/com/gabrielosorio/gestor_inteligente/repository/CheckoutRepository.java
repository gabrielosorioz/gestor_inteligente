package com.gabrielosorio.gestor_inteligente.repository;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindOpenCheckoutForToday;

import java.util.Optional;

public class CheckoutRepository extends Repository<Checkout>{

    /**
     * Finds the open checkout for today, if any.
     *
     * @return An {@code Optional<Checkout>} containing the open checkout for today,
     *         or an empty Optional if none exists.
     */
    public Optional<Checkout> findOpenCheckoutForToday() {
        return findBySpecification(new FindOpenCheckoutForToday()).stream().findFirst();
    }

}
