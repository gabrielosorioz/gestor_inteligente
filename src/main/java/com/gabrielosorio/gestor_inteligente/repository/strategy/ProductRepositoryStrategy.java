package com.gabrielosorio.gestor_inteligente.repository.strategy;

import com.gabrielosorio.gestor_inteligente.model.Product;

public interface ProductRepositoryStrategy extends RepositoryStrategy<Product> {
    boolean existsPCode(long pCode);
    boolean existsBarCode(String barCode);
    long genPCode();
}
