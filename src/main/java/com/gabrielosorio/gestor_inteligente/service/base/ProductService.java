package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product save(Product product);
    void update(Product product);
    List<Product> findAllProducts();
    Optional<Product> findByBarCodeOrCode(String code);
    void increaseQuantity(long id, long quantity);
    void decreaseQuantity(long id, long quantity);

}
