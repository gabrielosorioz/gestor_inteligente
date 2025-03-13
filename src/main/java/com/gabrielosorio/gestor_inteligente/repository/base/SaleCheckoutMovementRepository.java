package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public interface SaleCheckoutMovementRepository extends RepositoryStrategy<SaleCheckoutMovement>, BatchInsertable<SaleCheckoutMovement> {
}
