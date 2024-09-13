package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.Product;

public class FindByProductCode implements Specification<Product> {

    private final long productCode;

    public FindByProductCode(long productCode){
        this.productCode = productCode;
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
        return product.getProductCode() == productCode;
    }

}
