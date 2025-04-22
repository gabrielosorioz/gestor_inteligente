package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Specification to find checkout movements within a date range.
 */
public class FindCheckoutMovementByDateRange extends AbstractSpecification<CheckoutMovement> {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public FindCheckoutMovementByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String toSql() {
        return getQuery("findCheckoutMovementByDateRange");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(startDate, endDate);
    }

    @Override
    public boolean isSatisfiedBy(CheckoutMovement checkoutMovement) {
        LocalDateTime movementDate = checkoutMovement.getDateTime();
        return (movementDate.isEqual(startDate) || movementDate.isAfter(startDate)) &&
                (movementDate.isEqual(endDate) || movementDate.isBefore(endDate));
    }
}