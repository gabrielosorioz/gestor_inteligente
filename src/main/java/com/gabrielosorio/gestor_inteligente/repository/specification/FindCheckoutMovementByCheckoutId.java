package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.List;

public class FindCheckoutMovementByCheckoutId extends AbstractSpecification<CheckoutMovement> {
    private final long checkoutId;

    public FindCheckoutMovementByCheckoutId(long checkoutId) {
        this.checkoutId = checkoutId;
    }
    @Override
    public String toSql() {
        return getQuery("findCheckoutMovementByCheckoutId");
    }


    @Override
    public List<Object> getParameters() {
        return List.of(checkoutId);
    }

    @Override
    public boolean isSatisfiedBy(CheckoutMovement checkoutMovement) {
        return checkoutMovement.getCheckout()
                .getId() == checkoutId;
    }
}
