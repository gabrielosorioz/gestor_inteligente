package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.List;

public class SaleProductBySaleIdSpec implements Specification<SaleProduct> {

    private final QueryLoader qLoader;
    private final long saleId;

    public SaleProductBySaleIdSpec(long saleId) {
        this.saleId = saleId;
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
    }

    @Override
    public String toSql() {
        return "findSaleProductBySaleId";
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
