package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.List;

public class FindByCategoryName implements Specification<Category> {

    private final QueryLoader qLoader;
    private final String description;

    public FindByCategoryName(String description) {
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.description = description;
    }


    @Override
    public String toSql() {
        return qLoader.getQuery("findCategoryByName");
    }

    @Override
    public List<Object> getParameters() {
        return List.of(description);
    }

    @Override
    public boolean isSatisfiedBy(Category category) {
        return description.equalsIgnoreCase(category.getDescription());
    }

}
