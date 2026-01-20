package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchDeletable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchUpdatable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.util.List;

public interface SalePaymentRepository extends RepositoryStrategy<SalePayment,Long>, BatchInsertable<SalePayment>, BatchDeletable<Long>,
        BatchUpdatable<SalePayment> {
    List<SalePayment> findBySaleId(Long saleId);
}
