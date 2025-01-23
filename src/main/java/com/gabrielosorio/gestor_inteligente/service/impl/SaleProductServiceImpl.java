package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.Repository;

import com.gabrielosorio.gestor_inteligente.service.AbstractTransactionalService;
import com.gabrielosorio.gestor_inteligente.service.base.SaleProductService;
import java.math.BigDecimal;
import java.util.List;

public class SaleProductServiceImpl extends AbstractTransactionalService<SaleProduct> implements SaleProductService {

    private final Repository<SaleProduct> REPOSITORY;

    public SaleProductServiceImpl(Repository<SaleProduct> saleProductRepository) {
        super(saleProductRepository);
        REPOSITORY = getRepository();
    }

    public SaleProduct save(SaleProduct saleProduct){
        validate(saleProduct);
        return REPOSITORY.add(saleProduct);
    }

    public List<SaleProduct> saveAll(List<SaleProduct> saleProducts){
        saleProducts.forEach(this::validate);
        return REPOSITORY.addAll(saleProducts);
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
