package com.gabrielosorio.gestor_inteligente.view.shared.base;


/**
 * Contrato para gerenciamento de telas FXML na aplicação.
 */
public interface ScreenManager {

    /**
     * Carrega uma tela usando o controlador automático do FXML.
     *
     * @param fxmlPath Caminho do arquivo FXML.
     */
    void loadScreen(String fxmlPath);

    /**
     * Carrega uma tela com um controlador específico (injeção manual).
     *
     * @param fxmlPath   Caminho do arquivo FXML.
     * @param controller Instância do controlador a ser usada (opcional).
     */
    void loadScreen(String fxmlPath, Object controller);

    /**
     * Limpa o cache de todas as telas.
     */
    void clearCache();

    /**
     * Remove uma tela específica do cache.
     *
     * @param fxmlPath Caminho do arquivo FXML da tela a ser removida.
     */
    void removeCachedScreen(String fxmlPath);

    /**
     * Define um listener para mudanças de tela.
     *
     * @param listener Implementação do listener.
     */
    void setScreenChangeListener(ScreenChangeListener listener);

    /**
     * Listener para escutar mudanças de tela.
     */
    interface ScreenChangeListener {
        void onScreenChanged(Object controller);
    }




















































































































}
