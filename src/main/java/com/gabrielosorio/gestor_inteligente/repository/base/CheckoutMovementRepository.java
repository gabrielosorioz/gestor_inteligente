package com.gabrielosorio.gestor_inteligente.repository.base;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import java.util.List;

public interface CheckoutMovementRepository extends RepositoryStrategy<CheckoutMovement>, BatchInsertable<CheckoutMovement> {
    List<CheckoutMovement> findCheckoutMovementByCheckoutId(long checkoutId);
}
