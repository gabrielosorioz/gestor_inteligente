package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.List;

public class FindSalePaymentsBySaleId extends AbstractSpecification<SalePayment> {

    private final Long saleId;

    public FindSalePaymentsBySaleId(Long saleId) {
        this.saleId = saleId;
    }

    @Override
    public String toSql() {
        return getQuery("findSalePaymentsBySaleId");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(saleId);
    }

    @Override
    public boolean isSatisfiedBy(SalePayment salePayment) {
        if (salePayment == null) return false;
        return salePayment.getSaleId() == saleId;
    }
}
