package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalStrategy;

import java.util.List;
import java.util.Optional;

public abstract class Repository <T> {

    protected RepositoryStrategy<T> strategy;

    public void init(RepositoryStrategy<T> strategy){
        this.strategy = strategy;
    }

    public T add(T entity){
        return strategy.add(entity);
    }

    public Optional<T> find(long id){
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

    public boolean remove(long id){
        return strategy.remove(id);
    }

    public List<T> addAll(List<T> entities) {
        if (strategy instanceof BatchInsertable) {
            return ((BatchInsertable<T>) strategy).addAll(entities);
        } else {
            throw new UnsupportedOperationException("Batch insert not supported for this entity.");
        }
    }

    public TransactionalStrategy<T> getTransactionalStrategy(){
        if (strategy instanceof TransactionalStrategy<?>) {
            return ((TransactionalStrategy<T>) strategy);
        } else {
            throw new UnsupportedOperationException("Transactional strategy not supported for this entity.");
        }
    }



}
