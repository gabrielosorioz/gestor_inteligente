package com.gabrielosorio.gestor_inteligente.repository;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.ProductRepositoryStrategy;

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
