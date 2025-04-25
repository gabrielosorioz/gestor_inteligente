package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.repository.base.CategoryRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public class PSQLCategoryRepository extends Repository<Category> implements CategoryRepository {
    public PSQLCategoryRepository(RepositoryStrategy<Category> strategy) {
        init(strategy);
    }
}
