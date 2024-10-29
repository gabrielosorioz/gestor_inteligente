package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.SaleProductRepository;

import java.math.BigDecimal;

public class SaleProductService {

    private final SaleProductRepository saleProductRep;

    public SaleProductService(SaleProductRepository saleRepository){
        this.saleProductRep = saleRepository;
    }

    public void save(SaleProduct saleProduct){
        validate(saleProduct);
        saleProductRep.add(saleProduct);
    }

    private void validate(SaleProduct saleProduct){
        BigDecimal unitPrice = saleProduct.getUnitPrice();

        if(saleProduct.getProduct() == null){
            throw new IllegalArgumentException("Product cannot be null.");
        }

        if(saleProduct.getQuantity() <= 0){
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be null or negative");
        }

        if(saleProduct.getSale() == null){
            throw new IllegalArgumentException("The sale item must be associated with a sale.");
        }


    }


}
