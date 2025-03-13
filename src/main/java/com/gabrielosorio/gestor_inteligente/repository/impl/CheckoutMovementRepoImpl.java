package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementByCheckoutId;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementBySaleId;

import java.util.List;

public class CheckoutMovementRepoImpl extends Repository<CheckoutMovement> implements CheckoutMovementRepository {

    @Override
    public List<CheckoutMovement> findCheckoutMovementBySaleId(long saleId) {
        return findBySpecification(new FindCheckoutMovementBySaleId(saleId));
    }

    @Override
    public List<CheckoutMovement> findCheckoutMovementByCheckoutId(long checkoutId) {
        return findBySpecification(new FindCheckoutMovementByCheckoutId(checkoutId));
    }

}
