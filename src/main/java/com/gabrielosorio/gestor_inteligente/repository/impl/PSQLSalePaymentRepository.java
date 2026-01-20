package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.base.SalePaymentRepository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindSalePaymentsBySaleId;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.util.List;

public class PSQLSalePaymentRepository extends Repository<SalePayment,Long> implements SalePaymentRepository {
    public PSQLSalePaymentRepository(RepositoryStrategy<SalePayment,Long> strategy) {
        init(strategy);
    }

    @Override
    public List<SalePayment> findBySaleId(Long saleId) {
        if (saleId == null || saleId <= 0) {
            throw new IllegalArgumentException("The sale ID is invalid.");
        }
        return strategy.findBySpecification(new FindSalePaymentsBySaleId(saleId));
    }

}
