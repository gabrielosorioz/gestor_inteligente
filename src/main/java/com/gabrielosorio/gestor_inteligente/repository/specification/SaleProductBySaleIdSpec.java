package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.List;

public class SaleProductBySaleIdSpec extends AbstractSpecification<SaleProduct> {

    private final long saleId;

    public SaleProductBySaleIdSpec(long saleId) {
        this.saleId = saleId;
    }

    @Override
    public String toSql() {
        return getQuery("findSaleProductBySaleId");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(saleId);
    }

    @Override
    public boolean isSatisfiedBy(SaleProduct saleProduct) {
        return this.saleId == saleProduct.getSaleId();
    }
}
