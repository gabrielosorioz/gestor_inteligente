package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.repository.base.RepositoryFactory;
import com.gabrielosorio.gestor_inteligente.service.base.*;
import com.gabrielosorio.gestor_inteligente.service.view.ScreenLoaderServiceImpl;

import java.util.Objects;

/**
 * Factory responsible for creating and managing instance of application services.
 * It centralizes the creation of all services and manages their dependencies
 */
public class ServiceFactory {

    private final RepositoryFactory repositoryFactory;
    private SaleService saleService;
    private SaleProductService saleProductService;
    private SalePaymentService salePaymentService;
    private ProductService productService;
    private CheckoutMovementService checkoutMovementService;
    private CheckoutService checkoutService;
    private SaleCheckoutMovementService saleCheckoutMovementService;
    private AuthenticationService authenticationService;
    private ScreenLoaderService screenLoaderService;


    /**
     * Constructor that receives a repository factory necessary to create the services.
     * @param repositoryFactory factory que fornece acesso aos repositórios
     */
    public ServiceFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public ProductService getProductService() {
        if (productService == null) {
            productService = new ProductServiceImpl(repositoryFactory.getProductRepository());
        }
        return productService;
    }

    public SaleProductService getSaleProductService() {
        if (saleProductService == null) {
            saleProductService = new SaleProductServiceImpl(repositoryFactory.getSaleProductRepository());
        }
        return saleProductService;
    }

    public SalePaymentService getSalePaymentService() {
        if (salePaymentService == null) {
            salePaymentService = new SalePaymentServiceImpl(repositoryFactory.getSalePaymentRepository());
        }
        return salePaymentService;
    }

    public CheckoutMovementService getCheckoutMovementService() {
        if (checkoutMovementService == null) {
            checkoutMovementService = new CheckoutMovementServiceImpl(
                    repositoryFactory.getCheckoutMovementRepository());
        }
        return checkoutMovementService;
    }

    public CheckoutService getCheckoutService() {
        if (checkoutService == null) {
            checkoutService = new CheckoutServiceImpl(
                    repositoryFactory.getCheckoutRepository(),
                    getCheckoutMovementService());
        }
        return checkoutService;
    }

    public SaleCheckoutMovementService getSaleCheckoutMovementService() {
        if (saleCheckoutMovementService == null) {
            saleCheckoutMovementService = new SaleCheckoutMovementServiceImpl(
                    repositoryFactory.getSaleCheckoutMovementRepository());
        }
        return saleCheckoutMovementService;
    }

    public SaleService getSaleService() {
        if (saleService == null) {
            saleService = new SaleServiceImpl(
                    repositoryFactory.getSaleRepository(),
                    getSaleProductService(),
                    getSalePaymentService(),
                    getCheckoutMovementService(),
                    getCheckoutService(),
                    getProductService(),
                    getSaleCheckoutMovementService()
            );
        }
        return saleService;
    }

    public AuthenticationService getAuthenticationService(){
        return Objects.requireNonNullElseGet(authenticationService,
                () -> new AuthenticationServiceImpl(
                repositoryFactory.getUserRepository()
        ));
    }

    public ScreenLoaderService getScreenLoaderService() {
        if (screenLoaderService == null) {
            screenLoaderService = new ScreenLoaderServiceImpl(this);
        }
        return screenLoaderService;
    }
}
