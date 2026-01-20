package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import java.util.List;

public interface BatchUpdatable<T> {
    List<T> updateAll(List<T> entities);
}
