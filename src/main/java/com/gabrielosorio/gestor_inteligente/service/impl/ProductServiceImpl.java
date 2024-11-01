package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.exception.ProductException;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.service.ProductService;

import java.sql.Timestamp;
import java.time.Instant;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository pRepository;

    public ProductServiceImpl(ProductRepository pRepository) {
        this.pRepository = pRepository;
    }

    @Override
    public void save(Product product) throws ProductException {
        long pCode;

        if(product.getProductCode() == 0){
            pCode = pRepository.genPCode();
        } else {
            pCode = product.getProductCode();

            if(pRepository.existPCode(product.getProductCode())){
                throw new ProductException("The product code already exists. ");
            }

            if(pRepository.existsBarCode(product.getBarCode().orElse(""))){
                throw new ProductException("The product bar code already exists. ");
            }
        }

        product.setProductCode(pCode);
        product.setDateUpdate(Timestamp.from(Instant.now()));
        pRepository.add(product);
    }

    @Override
    public void update(Product product) {
        product.setDateUpdate(Timestamp.from(Instant.now()));
        pRepository.update(product);
    }




}
