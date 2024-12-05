package com.gabrielosorio.gestor_inteligente.repository;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.ProductRepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.strategy.RepositoryStrategy;

import java.util.List;
import java.util.Optional;

public class ProductRepository extends Repository<Product> {

    private final ProductRepositoryStrategy pStrategy;

    public ProductRepository(ProductRepositoryStrategy pStrategy) {
        this.pStrategy = pStrategy;
        super.init(pStrategy);
    }

    public boolean existPCode(long pCode){
        return pStrategy.existsPCode(pCode);
    }

    public boolean existsBarCode(String barCode){
        return pStrategy.existsBarCode(barCode);
    }

    public long genPCode(){
        return pStrategy.genPCode();
    }

}
