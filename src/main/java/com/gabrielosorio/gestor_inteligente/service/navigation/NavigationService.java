package com.gabrielosorio.gestor_inteligente.service.navigation;

/**
 * Contrato para o serviço responsável por gerenciar a navegação entre telas da aplicação.
 */
public interface NavigationService {

    /**
     * Abre a tela inicial/home.
     */
    void openHome();

    /**
     * Abre o ponto de venda (PDV).
     */
    void openPointOfSale();

    /**
     * Abre o gerenciador de produtos.
     */
    void openProductManager();

    /**
     * Abre o relatório de vendas.
     */
    void openSalesReport();

    /**
     * Abre a movimentação do caixa.
     */
    void openCheckoutMovement();
}
