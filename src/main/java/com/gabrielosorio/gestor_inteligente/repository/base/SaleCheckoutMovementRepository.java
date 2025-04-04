package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import java.util.List;

public interface SaleCheckoutMovementRepository extends RepositoryStrategy<SaleCheckoutMovement>, BatchInsertable<SaleCheckoutMovement> {
    List<SaleCheckoutMovement> findSalesInCheckoutMovements(List<CheckoutMovement> checkoutMovements);
}
