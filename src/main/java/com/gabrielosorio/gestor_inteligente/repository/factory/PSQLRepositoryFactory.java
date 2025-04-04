package com.gabrielosorio.gestor_inteligente.repository.factory;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.repository.base.*;
import com.gabrielosorio.gestor_inteligente.repository.impl.*;
import com.gabrielosorio.gestor_inteligente.repository.strategy.psql.*;

public class PSQLRepositoryFactory implements RepositoryFactory {

    @Override
    public CategoryRepository getCategoryRepository() {
        var strategy = new PSQLCategoryStrategy(ConnectionFactory.getInstance());
        return new PSQLCategoryRepository(strategy);
    }

    @Override
    public CheckoutMovementRepository getCheckoutMovementRepository() {
        var strategy = new PSQLCheckoutMovementStrategy(ConnectionFactory.getInstance());
        return new PSQLCheckoutMovementRepository(strategy);
    }

    @Override
    public CheckoutMovementTypeRepository getCheckoutMovementTypeRepository() {
        var strategy = new PSQLCheckoutMovementTypeStrategy(ConnectionFactory.getInstance());
        return new PSQLCheckoutMovementTypeRepository(strategy);
    }

    @Override
    public CheckoutRepository getCheckoutRepository() {
        var strategy = new PSQLCheckoutStrategy(ConnectionFactory.getInstance());
        return new PSQLCheckoutRepository(strategy);
    }

    @Override
    public PaymentRepository getPaymentRepository() {
        var strategy = new PSQLPaymentStrategy(ConnectionFactory.getInstance());
        return new PSQLPaymentRepository(strategy);
    }

    @Override
    public ProductRepository getProductRepository() {
        var strategy = new PSQLProductStrategy(ConnectionFactory.getInstance());
        return new PSQLProductRepository(strategy);
    }

    @Override
    public SaleCheckoutMovementRepository getSaleCheckoutMovementRepository() {
        var strategy = new PSQLSaleCheckoutMovementStrategy();
        return new PSQLSaleCheckoutMovementRepository(strategy);
    }

    @Override
    public SalePaymentRepository getSalePaymentRepository() {
        var strategy = new PSQLSalePaymentStrategy(ConnectionFactory.getInstance());
        return new PSQLSalePaymentRepository(strategy);
    }

    @Override
    public SaleProductRepository getSaleProductRepository() {
        var strategy = new PSQLSaleProductStrategy(ConnectionFactory.getInstance());
        return new PSQLSaleProductRepository(strategy);
    }

    @Override
    public SaleRepository getSaleRepository() {
        var strategy = new PSQLSaleStrategy(ConnectionFactory.getInstance());
        return new PSQLSaleRepository(strategy);
    }
}
