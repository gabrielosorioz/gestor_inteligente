package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.repository.base.PaymentRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLPaymentRepository extends Repository<Payment,Long> implements PaymentRepository {
    public PSQLPaymentRepository(RepositoryStrategy<Payment,Long> strategy) {
        init(strategy);
    }
}

