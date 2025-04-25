package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.List;

public class FindProductByProductCode extends AbstractSpecification<Product> {

    private final long pCode;

    public FindProductByProductCode(long productCode){
        this.pCode = productCode;
    }

    @Override
    public String toSql() {
        return getQuery("productByProductCode");
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
