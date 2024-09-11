package com.gabrielosorio.gestor_inteligente.repository;

import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;

import java.util.HashMap;
import java.util.List;

public interface Repository <T> {
    void add(T t);
    List<T> getAll();
    List<T> findBySpecification(Specification<T> specification);
    void update(T oldT, T newT);
    void remove(long id);
}
