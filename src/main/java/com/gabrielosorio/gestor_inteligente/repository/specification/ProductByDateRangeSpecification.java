package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.sql.Timestamp;
import java.util.List;

public class ProductByDateRangeSpecification extends AbstractSpecification<Product> {

    private final Timestamp startDate;
    private final Timestamp endDate;

    public ProductByDateRangeSpecification(Timestamp startDate, Timestamp endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String toSql() {
        return getQuery("productByDateRange");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(startDate,endDate);
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
        Timestamp created = product.getDateCreate();
        Timestamp updated = product.getDateUpdate();
        return (isWithinRange(created) || isWithinRange(updated));
    }

    private boolean isWithinRange(Timestamp date){
        return date != null && !date.before(startDate) && !date.after(endDate);
    }
}
