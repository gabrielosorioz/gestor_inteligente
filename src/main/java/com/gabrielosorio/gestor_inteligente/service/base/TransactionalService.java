package com.gabrielosorio.gestor_inteligente.service.base;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalStrategy;

public interface TransactionalService {
    TransactionalStrategy<?> getTransactionalStrategy();
}
