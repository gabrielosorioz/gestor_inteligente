package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.time.LocalDate;
import java.util.List;

public class FindOpenCheckoutForToday extends AbstractSpecification<Checkout> {

    private final LocalDate today;

    public FindOpenCheckoutForToday() {
        this.today = LocalDate.now();
    }

    @Override
    public String toSql() {
        return getQuery("findOpenCheckoutForTodayWithUsers");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(CheckoutStatus.OPEN.name(), today);
    }

    @Override
    public boolean isSatisfiedBy(Checkout checkout) {
        return checkout.getStatus() == CheckoutStatus.OPEN &&
                checkout.getOpenedAt().toLocalDate().isEqual(today) &&
                checkout.getClosedAt() == null;
    }
}
