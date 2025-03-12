package com.gabrielosorio.gestor_inteligente.repository.specification;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;
import java.util.List;

public class FindByCategoryName extends AbstractSpecification<Category> {

    private final String description;

    public FindByCategoryName(String description) {
        this.description = description;
    }


    @Override
    public String toSql() {
        return getQuery("findCategoryByName");
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
