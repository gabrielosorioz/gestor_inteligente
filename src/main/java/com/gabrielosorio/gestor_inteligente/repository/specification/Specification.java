package com.gabrielosorio.gestor_inteligente.repository.specification;

public interface Specification <T> {
    boolean isSatisfiedBy(T t);
}
