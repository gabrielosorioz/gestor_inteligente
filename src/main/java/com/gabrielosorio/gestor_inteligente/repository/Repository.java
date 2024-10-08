package com.gabrielosorio.gestor_inteligente.repository;

import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import java.util.List;
import java.util.Optional;

public abstract class Repository <T> {

    private RepositoryStrategy<T> strategy;

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
}
