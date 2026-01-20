package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.List;

public class FindSaleDetailsByCheckoutMovementId extends AbstractSpecification<SaleCheckoutMovement> {

    private final Long checkoutMovementId;

    public FindSaleDetailsByCheckoutMovementId(Long checkoutMovementId) {
        this.checkoutMovementId = checkoutMovementId;
    }

    @Override
    public String toSql() {
        return getQuery("findSaleDetailsByCheckoutMovementId");
    }

    @Override
    public List getParameters() {
        return List.of(checkoutMovementId);
    }

    @Override
    public boolean isSatisfiedBy(SaleCheckoutMovement saleCheckoutMovement) {
        if (saleCheckoutMovement == null
                || saleCheckoutMovement.getCheckoutMovement() == null) {
            return false;
        }
        return saleCheckoutMovement.getCheckoutMovement().getId() == checkoutMovementId;
    }
}
