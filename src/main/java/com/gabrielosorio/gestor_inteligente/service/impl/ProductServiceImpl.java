package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.datacontext.ProductDataContext;
import com.gabrielosorio.gestor_inteligente.exception.ProductException;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.service.AbstractTransactionalService;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl extends AbstractTransactionalService<Product> implements ProductService {

    private final ProductRepository REPOSITORY;
    private final ProductDataContext prodDataContext;

    public ProductServiceImpl(ProductRepository productRepository) {
        super(productRepository);
        REPOSITORY = productRepository;
        this.prodDataContext = ProductDataContext.getInstance(getRepository());
    }

    @Override
    public void save(Product product) throws ProductException {
        long pCode;

        if(product.getProductCode() == 0){
            pCode = REPOSITORY.genPCode();
        } else {
            pCode = product.getProductCode();

            if(REPOSITORY.existPCode(product.getProductCode())){
                throw new ProductException("The product code already exists. ");
            }

            if(REPOSITORY.existsBarCode(product.getBarCode().orElse(""))){
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
    public Optional<Product> findByBarCodeOrCode(String code){
        return prodDataContext.findByCode(code);
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
