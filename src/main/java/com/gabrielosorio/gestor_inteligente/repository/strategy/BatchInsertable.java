package com.gabrielosorio.gestor_inteligente.repository.strategy;

import java.util.List;

public interface BatchInsertable<T> {
    List<T> addAll(List<T> entities);
}
