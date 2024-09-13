package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.Product;

public class FindByBarcode implements Specification<Product> {

    private final String barcode;

    public FindByBarcode(String barcode){
        this.barcode = barcode;
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
       return product.getBarCode()
               .map(productBarCode -> productBarCode.equalsIgnoreCase(barcode))
               .orElse(false);
    }
}
