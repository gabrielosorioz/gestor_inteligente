package com.gabrielosorio.gestor_inteligente.repository.base;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.time.LocalDateTime;
import java.util.List;

public interface CheckoutMovementRepository extends RepositoryStrategy<CheckoutMovement,Long>, BatchInsertable<CheckoutMovement> {
    List<CheckoutMovement> findCheckoutMovementByCheckoutId(long checkoutId);
    List<CheckoutMovement> findCheckoutMovementByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
