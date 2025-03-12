package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.math.BigDecimal;
import java.util.List;

public class ProductByPriceRangeSpecification extends AbstractSpecification<Product> {

    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;

    public ProductByPriceRangeSpecification(BigDecimal minPrice, BigDecimal maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    @Override
    public String toSql() {
        return getQuery("productByPriceRange");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(minPrice,maxPrice);
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
        var price = product.getSellingPrice();
        return price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
    }
}
