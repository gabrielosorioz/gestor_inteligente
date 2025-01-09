package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.Product;

import java.util.List;

public interface ProductService extends TransactionalService {
    void save(Product product);
    void update(Product product);
    List<Product> findAllProducts();
    void increaseQuantity(long id, long quantity);
    void decreaseQuantity(long id, long quantity);

}
