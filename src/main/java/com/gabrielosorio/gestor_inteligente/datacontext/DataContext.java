package com.gabrielosorio.gestor_inteligente.datacontext;

import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;

import java.util.List;
import java.util.Optional;

public interface DataContext<T> {
    T add(T t);
    Optional<T> find(long id);
    List<T> findAll();
    List<T> findBySpecification(Specification<T> specification);
    T update(T newT);
    boolean remove(long id);
}
