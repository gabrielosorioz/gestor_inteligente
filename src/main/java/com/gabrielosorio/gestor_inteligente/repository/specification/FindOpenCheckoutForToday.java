package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.time.LocalDate;
import java.util.List;

public class FindOpenCheckoutForToday implements Specification<Checkout> {

    private final LocalDate today;
    private final QueryLoader qLoader;

    public FindOpenCheckoutForToday() {
        this.today = LocalDate.now();
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
    }

    @Override
    public String toSql() {
        return qLoader.getQuery("findOpenCheckoutForToday");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(CheckoutStatus.OPEN.getName(), today);
    }

    @Override
    public boolean isSatisfiedBy(Checkout checkout) {
        return checkout.getStatus() == CheckoutStatus.OPEN &&
                checkout.getOpenedAt().toLocalDate().isEqual(today) &&
                checkout.getClosedAt() == null;
    }
}
