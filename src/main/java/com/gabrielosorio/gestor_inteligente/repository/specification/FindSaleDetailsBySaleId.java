package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.List;

public class FindSaleDetailsBySaleId extends AbstractSpecification<SaleCheckoutMovement> {

    private final Long saleId;

    public FindSaleDetailsBySaleId(Long saleId) {
        this.saleId = saleId;
    }

    @Override
    public String toSql() {
        return getQuery("findSaleDetailsBySaleId");
    }

    @Override
    public List getParameters() {
        return List.of(saleId);
    }

    @Override
    public boolean isSatisfiedBy(SaleCheckoutMovement item) {
        if (item == null || item.getSale() == null) return false;
        return item.getSale().getId() == saleId;
    }

}
