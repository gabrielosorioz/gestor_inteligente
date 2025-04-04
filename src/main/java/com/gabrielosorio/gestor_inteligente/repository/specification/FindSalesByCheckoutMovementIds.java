package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FindSalesByCheckoutMovementIds extends AbstractSpecification<SaleCheckoutMovement> {
    private final List<Long> checkoutMovementIds;

    public FindSalesByCheckoutMovementIds(List<Long> checkoutMovementIds) {
        this.checkoutMovementIds = checkoutMovementIds;
    }

    @Override
    public String toSql() {
        String placeholders = checkoutMovementIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        return getQuery("findSalesByCheckoutMovementIds")
                .replace("{placeholders}", placeholders);
    }

    @Override
    public List<Object> getParameters() {
        return new ArrayList<>(checkoutMovementIds);
    }

    @Override
    public boolean isSatisfiedBy(SaleCheckoutMovement saleCheckoutMovement) {
        return checkoutMovementIds.contains(saleCheckoutMovement.getCheckoutMovement().getId());
    }
}
