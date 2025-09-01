package com.gabrielosorio.gestor_inteligente.repository.factory;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.repository.base.*;
import com.gabrielosorio.gestor_inteligente.repository.impl.*;
import com.gabrielosorio.gestor_inteligente.repository.strategy.psql.*;

public class PSQLRepositoryFactory implements RepositoryFactory {

    private final ConnectionFactory connectionFactory;

    public PSQLRepositoryFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public CategoryRepository getCategoryRepository() {
        var strategy = new PSQLCategoryStrategy(connectionFactory);
        return new PSQLCategoryRepository(strategy);
    }

    @Override
    public CheckoutMovementRepository getCheckoutMovementRepository() {
              var strategy = new PSQLCheckoutMovementStrategy(connectionFactory);
        return new PSQLCheckoutMovementRepository(strategy);
    }

    @Override
    public CheckoutMovementTypeRepository getCheckoutMovementTypeRepository() {
        var strategy = new PSQLCheckoutMovementTypeStrategy(connectionFactory);
        return new PSQLCheckoutMovementTypeRepository(strategy);
    }

    @Override
    public CheckoutRepository getCheckoutRepository() {
        var strategy = new PSQLCheckoutStrategy(connectionFactory);
        return new PSQLCheckoutRepository(strategy);
    }

    @Override
    public PaymentRepository getPaymentRepository() {
        var strategy = new PSQLPaymentStrategy(connectionFactory);
        return new PSQLPaymentRepository(strategy);
    }

    @Override
    public ProductRepository getProductRepository() {
        var strategy = new PSQLProductStrategy(connectionFactory);
        return new PSQLProductRepository(strategy);
    }

    @Override
    public SaleCheckoutMovementRepository getSaleCheckoutMovementRepository() {
        var strategy = new PSQLSaleCheckoutMovementStrategy(connectionFactory);
        return new PSQLSaleCheckoutMovementRepository(strategy);
    }

    @Override
    public SalePaymentRepository getSalePaymentRepository() {
        var strategy = new PSQLSalePaymentStrategy(connectionFactory);
        return new PSQLSalePaymentRepository(strategy);
    }

    @Override
    public SaleProductRepository getSaleProductRepository() {
        var strategy = new PSQLSaleProductStrategy(connectionFactory);
        return new PSQLSaleProductRepository(strategy);
    }

    @Override
    public SaleRepository getSaleRepository() {
        var strategy = new PSQLSaleStrategy(connectionFactory);
        return new PSQLSaleRepository(strategy);
    }

    @Override
    public UserRepository getUserRepository() {
        var strategy = new PSQLUserStrategy(connectionFactory);
        return new PSQLUserRepository(strategy);
    }
}