package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Product;

import java.util.List;

public class FindProductByProductCode implements Specification<Product> {

    private final long pCode;
    private final QueryLoader qLoader;

    public FindProductByProductCode(long productCode){
        this.pCode = productCode;
        qLoader = new QueryLoader(DBScheme.POSTGRESQL);
    }

    @Override
    public String toSql() {
        return qLoader.getQuery("productByProductCode");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(pCode);
    }

    @Override
    public boolean isSatisfiedBy(Product p) {
        return p.getProductCode() == this.pCode;
    }
}
