package com.gabrielosorio.gestor_inteligente.repository.base;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchDeletable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchUpdatable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

public interface SaleProductRepository extends RepositoryStrategy<SaleProduct,Long>, BatchInsertable<SaleProduct>,
        BatchDeletable<Long>, BatchUpdatable<SaleProduct> {

}
