package com.gabrielosorio.gestor_inteligente.repository.base;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.util.List;
import java.util.Optional;

public abstract class Repository <T,ID> {

    protected RepositoryStrategy<T,ID> strategy;

    public void init(RepositoryStrategy<T,ID> strategy){
        this.strategy = strategy;
    }

    public T add(T entity){
        return strategy.add(entity);
    }

    public Optional<T> find(ID id){
        return strategy.find(id);
    }

    public List<T> findAll(){
        return strategy.findAll();
    }

    public List<T> findBySpecification(Specification<T> specification){
        return strategy.findBySpecification(specification);
    }

    public T update(T newT){
        return strategy.update(newT);
    }

    public boolean remove(ID id){
        return strategy.remove(id);
    }

    public List<T> addAll(List<T> entities) {
        if (strategy instanceof BatchInsertable) {
            return ((BatchInsertable<T>) strategy).addAll(entities);
        } else {
            throw new UnsupportedOperationException("Batch insert not supported for this entity.");
        }
    }


}
