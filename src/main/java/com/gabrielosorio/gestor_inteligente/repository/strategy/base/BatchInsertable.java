package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import java.util.List;

public interface BatchInsertable<T> {
    List<T> addAll(List<T> entities);
}
