package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.base.SaleRepository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLSaleRepository extends Repository<Sale> implements SaleRepository {
    public PSQLSaleRepository(RepositoryStrategy<Sale> strategy) {
        init(strategy);
    }
}
