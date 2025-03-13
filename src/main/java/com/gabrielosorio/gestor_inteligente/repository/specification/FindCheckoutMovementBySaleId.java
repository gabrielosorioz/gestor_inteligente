package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.List;

public class FindCheckoutMovementBySaleId extends AbstractSpecification<CheckoutMovement> {

    private final long saleId;

    public FindCheckoutMovementBySaleId(long saleId) {
        this.saleId = saleId;
    }

    @Override
    public String toSql() {
        return getQuery("findCheckoutMovementBySaleId");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(saleId);
    }

    @Override
    public boolean isSatisfiedBy(CheckoutMovement cm) {
        return false;
    }
}
