package com.gabrielosorio.gestor_inteligente.service.navigation;

import com.gabrielosorio.gestor_inteligente.service.impl.ServiceFactory;
import com.gabrielosorio.gestor_inteligente.view.checkout.CheckoutMovementController;
import com.gabrielosorio.gestor_inteligente.view.checkout.CheckoutTabPaneController;
import com.gabrielosorio.gestor_inteligente.view.product.ProductManagerController;
import com.gabrielosorio.gestor_inteligente.view.shared.base.ScreenManager;

/**
 * Serviço responsável por gerenciar a navegação entre telas da aplicação
 */
public class NavigationServiceImpl implements NavigationService {

    private final ScreenManager screenManager;
    private final ServiceFactory serviceFactory;
    private final Runnable sidebarCollapseCallback;

    public NavigationServiceImpl(ScreenManager screenManager, ServiceFactory serviceFactory, Runnable sidebarCollapseCallback) {
        this.screenManager = screenManager;
        this.serviceFactory = serviceFactory;
        this.sidebarCollapseCallback = sidebarCollapseCallback;
    }

    /**
     * Abre a tela inicial/home
     */
    public void openHome() {
        screenManager.loadScreen("fxml/Home.fxml");
        collapseSidebarIfOpen();
    }

    /**
     * Abre o ponto de venda (PDV)
     */
    public void openPointOfSale() {
        var productService = serviceFactory.getProductService();
        var controller = new CheckoutTabPaneController(productService);

        screenManager.loadScreen("fxml/sale/CheckoutTabPane.fxml", controller);
        collapseSidebarIfOpen();
    }

    /**
     * Abre o gerenciador de produtos
     */
    public void openProductManager() {
        var productService = serviceFactory.getProductService();
        var controller = new ProductManagerController(productService);

        screenManager.loadScreen("fxml/product-manager/ProductManager.fxml", controller);
        collapseSidebarIfOpen();
    }

    /**
     * Abre o relatório de vendas
     */
    public void openSalesReport() {
        screenManager.loadScreen("fxml/reports/SalesReport.fxml");
        collapseSidebarIfOpen();
    }

    /**
     * Abre a movimentação do caixa
     */
    public void openCheckoutMovement() {
        var checkoutService = serviceFactory.getCheckoutService();
        var saleCheckoutMovementService = serviceFactory.getSaleCheckoutMovementService();
        var saleService = serviceFactory.getSaleService();
        var checkoutMovementService = serviceFactory.getCheckoutMovementService();

        var controller = new CheckoutMovementController(
                checkoutService,
                saleCheckoutMovementService,
                saleService,
                checkoutMovementService
        );

        screenManager.loadScreen("fxml/sale/CheckoutMovement.fxml", controller);
        collapseSidebarIfOpen();
    }

    /**
     * Executa o callback para colapsar a sidebar se necessário
     */
    private void collapseSidebarIfOpen() {
        if (sidebarCollapseCallback != null) {
            sidebarCollapseCallback.run();
        }
    }
}