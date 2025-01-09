package com.gabrielosorio.gestor_inteligente.service;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.strategy.TransactionalStrategy;

public interface TransactionalService {
    TransactionalStrategy<?> getTransactionalStrategy();
}
