package com.gabrielosorio.gestor_inteligente.view.checkout;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;
import com.gabrielosorio.gestor_inteligente.view.shared.RequestFocus;
import com.gabrielosorio.gestor_inteligente.view.shared.ShortcutHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CheckoutTabPaneController implements Initializable, ShortcutHandler, RequestFocus {

    @FXML
    private TabPane checkoutTabPanel;
    private final Logger log = Logger.getLogger(getClass().getName());
    private int tabCounter;
    private static int initializeCounter = 0;
    private final ProductService PRODUCT_SERVICE;


    public CheckoutTabPaneController(ProductService productService) {
        PRODUCT_SERVICE = productService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("CheckoutTabPaneController initialized " + (++initializeCounter) + " time(s).");
        openFirstTab();

    }

    @Override
    public void requestFocusOnField() {
        Tab activeTab = checkoutTabPanel.getSelectionModel().getSelectedItem();
        if(activeTab != null){
            Object controller = activeTab.getUserData();
            if(controller instanceof RequestFocus){
                ((CheckoutTabController) controller).requestFocusOnField();
            }
        }
    }

    @Override
    public void handleShortcut(KeyCode keyCode) {
        Tab activeTab = checkoutTabPanel.getSelectionModel().getSelectedItem();
        if(activeTab != null){
            Object controller = activeTab.getUserData();
            if(controller instanceof CheckoutTabController){
                ((CheckoutTabController) controller).handleShortcut(keyCode);
            } else {
                log.warning("Active tab does not have a valid controller. ");
            }
        }
    }

    private void openFirstTab(){
        addNewCheckoutTab();
    }

    protected void addNewCheckoutTab(){
        try {
            Tab newCheckoutTab = getCheckoutTab();
            checkoutTabPanel.getTabs().add(newCheckoutTab);
            checkoutTabPanel.getSelectionModel().select(newCheckoutTab);
        } catch (Exception e) {
            log.severe("Error when inserting new checkout tab: " + e.getMessage() + e.getCause());
            throw new RuntimeException(e);
        }
    }

    private Tab getCheckoutTab(){
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/CheckoutTab.fxml"));
            CheckoutTabController checkoutTabController = new CheckoutTabController(this,PRODUCT_SERVICE);
            loader.setController(checkoutTabController);
            Tab newCheckoutTab = loader.load();

            // increments the counter and sets the tab name
            newCheckoutTab.setText("Caixa " +(++tabCounter));

            // Associates the controller with user data from Tab
            newCheckoutTab.setUserData(checkoutTabController);

            return newCheckoutTab;
        } catch (IOException e) {
            log.severe("Error creating a new checkout tab" + e.getMessage() + e.getCause());
            throw new RuntimeException(e);
        }
    }

    public int getListTabLength(){
        return checkoutTabPanel.getTabs().size();
    }

    public TabPane getTabPane(){
        return checkoutTabPanel;
    }
}
