package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementByCheckoutId;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementByDateRange;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementBySaleId;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.time.LocalDateTime;
import java.util.List;

public class PSQLCheckoutMovementRepository extends Repository<CheckoutMovement> implements CheckoutMovementRepository {

    public PSQLCheckoutMovementRepository(RepositoryStrategy<CheckoutMovement> strategy) {
        init(strategy);
    }

    @Override
    public List<CheckoutMovement> findCheckoutMovementByCheckoutId(long checkoutId) {
        return findBySpecification(new FindCheckoutMovementByCheckoutId(checkoutId));
    }

    @Override
    public List<CheckoutMovement> findCheckoutMovementByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return findBySpecification(new FindCheckoutMovementByDateRange(startDate, endDate));
    }

}
