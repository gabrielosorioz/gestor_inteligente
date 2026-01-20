package com.gabrielosorio.gestor_inteligente.repository.strategy.base;
import java.util.List;

public interface BatchDeletable<ID> {
    int deleteAll(List<ID> ids);
}

