package com.gabrielosorio.gestor_inteligente.repository;

import com.gabrielosorio.gestor_inteligente.model.Product;

public interface ProductRepositoryStrategy extends RepositoryStrategy<Product> {
    boolean existsPCode(long pCode);
    long genPCode();
}
