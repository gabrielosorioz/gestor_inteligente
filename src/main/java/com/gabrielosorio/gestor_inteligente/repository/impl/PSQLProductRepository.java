package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.base.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.ProductRepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLProductRepository extends Repository<Product> implements ProductRepository {

    public PSQLProductRepository(RepositoryStrategy<Product> strategy) {
        super.init(strategy);
    }


    @Override
    public boolean existsPCode(long pCode) {
        return false;
    }

    @Override
    public boolean existsBarCode(String barCode) {
        return false;
    }

    @Override
    public long genPCode() {
        return 0;
    }
}
