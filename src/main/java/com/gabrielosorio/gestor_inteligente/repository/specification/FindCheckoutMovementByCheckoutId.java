package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.List;

public class FindCheckoutMovementByCheckoutId implements Specification<CheckoutMovement> {
    private final long checkoutId;
    private final QueryLoader qLoader;

    public FindCheckoutMovementByCheckoutId(long checkoutId) {
        this.checkoutId = checkoutId;
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
    }
    @Override
    public String toSql() {
        return qLoader.getQuery("findCheckoutMovementByCheckoutId");
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
