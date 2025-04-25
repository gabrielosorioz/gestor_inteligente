package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutMovementTypeRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLCheckoutMovementTypeRepository extends Repository<CheckoutMovementType> implements CheckoutMovementTypeRepository {
    public PSQLCheckoutMovementTypeRepository(RepositoryStrategy<CheckoutMovementType> strategy) {
        init(strategy);
    }
}
