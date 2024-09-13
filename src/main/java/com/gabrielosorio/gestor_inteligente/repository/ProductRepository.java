package com.gabrielosorio.gestor_inteligente.repository;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;

import java.util.List;
import java.util.Optional;

public class ProductRepository extends Repository<Product> {


    @Override
    public void init(RepositoryStrategy<Product> strategy) {
        super.init(strategy);
    }

    @Override
    public Product add(Product entity) {
        return super.add(entity);
    }

    @Override
    public Optional<Product> find(long id) {
        return super.find(id);
    }

    @Override
    public List<Product> findAll() {
        return super.findAll();
    }

    @Override
    public List<Product> findBySpecification(Specification<Product> specification) {
        return super.findBySpecification(specification);
    }

    @Override
    public Product update(Product newT) {
        return super.update(newT);
    }

    @Override
    public boolean remove(long id) {
        return super.remove(id);
    }

}
