package com.gabrielosorio.gestor_inteligente.repository.specification.base;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import java.util.List;

public abstract class AbstractSpecification<T> implements Specification<T> {

    private final QueryLoader qLoader = new QueryLoader(DBScheme.POSTGRESQL);

    public String getQuery(String queryKey){
        return qLoader.getQuery(queryKey);
    }

    @Override
    public abstract String toSql();

    @Override
    public abstract List<Object> getParameters();

    @Override
    public abstract boolean isSatisfiedBy(T item);
}

