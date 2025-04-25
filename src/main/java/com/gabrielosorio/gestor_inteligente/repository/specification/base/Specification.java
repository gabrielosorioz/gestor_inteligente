package com.gabrielosorio.gestor_inteligente.repository.specification.base;

import java.util.List;

public interface Specification <T> {
    String toSql();
    List<Object> getParameters();
    boolean isSatisfiedBy(T item);
}
