package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.base.SaleCheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLSaleCheckoutMovementRepository extends Repository<SaleCheckoutMovement> implements SaleCheckoutMovementRepository {
    public PSQLSaleCheckoutMovementRepository(RepositoryStrategy<SaleCheckoutMovement> strategy){
        init(strategy);
    }
}
