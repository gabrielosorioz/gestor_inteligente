package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public interface ProductRepository extends RepositoryStrategy<Product> {
    boolean existsPCode(long pCode);
    boolean existsBarCode(String barCode);
    long genPCode();
}
