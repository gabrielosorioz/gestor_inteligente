package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindOpenCheckoutForToday;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.util.Optional;

public class PSQLCheckoutRepository extends Repository<Checkout,Long> implements CheckoutRepository {

    public PSQLCheckoutRepository(RepositoryStrategy<Checkout,Long> strategy){
        init(strategy);
    }
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
