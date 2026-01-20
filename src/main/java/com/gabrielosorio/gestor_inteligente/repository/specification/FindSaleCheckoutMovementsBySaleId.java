package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.List;

public class FindSaleCheckoutMovementsBySaleId extends AbstractSpecification<SaleCheckoutMovement> {

    private final Long saleId;

    public FindSaleCheckoutMovementsBySaleId(Long saleId) {
        this.saleId = saleId;
    }

    @Override
    public String toSql() {
        return getQuery("findSaleCheckoutMovementsBySaleId");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(saleId);
    }

    @Override
    public boolean isSatisfiedBy(SaleCheckoutMovement saleCheckoutMovement) {
        return saleCheckoutMovement != null
                && saleCheckoutMovement.getSale() != null
                && saleId.equals(saleCheckoutMovement.getSale().getId());
    }
}
