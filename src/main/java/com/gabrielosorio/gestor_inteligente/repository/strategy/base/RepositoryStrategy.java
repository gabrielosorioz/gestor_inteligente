package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.List;
import java.util.Optional;

public interface RepositoryStrategy <T> {
    T add(T t);
    Optional<T> find(long id);
    List<T> findAll();
    List<T> findBySpecification(Specification<T> specification);
    T update(T newT);
    boolean remove(long id);
}

