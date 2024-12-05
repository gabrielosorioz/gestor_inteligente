package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.Inject;
import com.gabrielosorio.gestor_inteligente.datacontext.ProductDataContext;
import com.gabrielosorio.gestor_inteligente.exception.ProductException;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.service.ProductService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository pRepository;
    private final ProductDataContext prodDataContext;

    public ProductServiceImpl(ProductRepository pRepository) {
        this.pRepository = pRepository;
        this.prodDataContext = ProductDataContext.getInstance(pRepository);
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
        prodDataContext.add(product);
    }

    @Override
    public List<Product> findAllProducts(){
        return prodDataContext.findAll();
    }

    @Override
    public void update(Product product) {
        product.setDateUpdate(Timestamp.from(Instant.now()));
        prodDataContext.update(product);
    }

    public void increaseQuantity(long id, long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("The quantity must be greater than zero.");
        }
        var product = prodDataContext.find(id);

        product.ifPresentOrElse(
                prod -> {
                    long currentQuantity = prod.getQuantity();
                    prod.setQuantity(currentQuantity + quantity);
                    prodDataContext.update(prod);
                },
                () -> {
                    throw new IllegalArgumentException("Product with ID " + id + " not found.");
                }
        );
    }

    public void decreaseQuantity(long id, long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("The quantity must be greater than zero.");
        }
        var product = prodDataContext.find(id);

        product.ifPresentOrElse(
                prod -> {
                    long currentQuantity = prod.getQuantity();
                    if (currentQuantity < quantity) {
                        throw new IllegalArgumentException(
                                "Insufficient stock for product with ID " + id + ". Current stock: " + currentQuantity);
                    }

                    prod.setQuantity(currentQuantity - quantity);
                    prodDataContext.update(prod);
                },
                () -> {
                    throw new IllegalArgumentException("Product with ID" + id + " not found.");
                }
        );
    }
}
