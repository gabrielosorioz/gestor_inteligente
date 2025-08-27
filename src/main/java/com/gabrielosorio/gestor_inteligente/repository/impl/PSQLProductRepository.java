package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.base.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.strategy.psql.PSQLProductStrategy;

public class PSQLProductRepository extends Repository<Product> implements ProductRepository {

    public PSQLProductRepository(RepositoryStrategy<Product> strategy) {
        super.init(strategy);
    }

    @Override
    public boolean existsPCode(long pCode) {
        if(strategy instanceof PSQLProductStrategy psqlProductStrategy) {
            return psqlProductStrategy.existsPCode(pCode);
        }
        return false;
    }

    @Override
    public boolean existsBarCode(String barCode) {
        if(strategy instanceof PSQLProductStrategy psqlProductStrategy) {
            return psqlProductStrategy.existsBarCode(barCode);
        }
        return false;
    }

    @Override
    public long genPCode() {
        if(strategy instanceof PSQLProductStrategy psqlProductStrategy) {
            return psqlProductStrategy.genPCode();
        }
        return 0;
    }
}
