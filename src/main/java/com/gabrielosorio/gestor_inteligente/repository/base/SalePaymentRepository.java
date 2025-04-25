package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public interface SalePaymentRepository extends RepositoryStrategy<SalePayment>, BatchInsertable<SalePayment> {
}
