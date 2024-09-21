package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.storage.PSQLProductStrategy;

import java.sql.SQLException;

public class ProductService {

    private final ProductRepository pRepository;

    public ProductService(ProductRepository pRepository) {
        this.pRepository = pRepository;
    }

    public void save(Product product) throws SQLException {

        long pCode;

        if(product.getProductCode() == 0){
            pCode = pRepository.genPCode();
        } else {
            pCode = product.getProductCode();

            if(pRepository.existPCode(product.getProductCode())){
                throw new SQLException("The product code already exists. ");
            }
        }

        product.setProductCode(pCode);
        pRepository.add(product);
    }




}
