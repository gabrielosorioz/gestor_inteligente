package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.base.SalePaymentRepository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLSalePaymentRepository extends Repository<SalePayment,Long> implements SalePaymentRepository {
    public PSQLSalePaymentRepository(RepositoryStrategy<SalePayment,Long> strategy) {
        init(strategy);
    }
}
