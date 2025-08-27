package com.gabrielosorio.gestor_inteligente.repository.base;

public interface RepositoryFactory {
    CategoryRepository getCategoryRepository();
    CheckoutMovementRepository getCheckoutMovementRepository();
    CheckoutMovementTypeRepository getCheckoutMovementTypeRepository();
    CheckoutRepository getCheckoutRepository();
    PaymentRepository getPaymentRepository();
    ProductRepository getProductRepository();
    SaleCheckoutMovementRepository getSaleCheckoutMovementRepository();
    SalePaymentRepository getSalePaymentRepository();
    SaleProductRepository getSaleProductRepository();
    SaleRepository getSaleRepository();
    UserRepository getUserRepository();
}
