package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.base.SaleProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLSaleProductRepository extends Repository<SaleProduct,Long> implements SaleProductRepository {
    public PSQLSaleProductRepository(RepositoryStrategy<SaleProduct,Long> strategy) {
        init(strategy);
    }
}
