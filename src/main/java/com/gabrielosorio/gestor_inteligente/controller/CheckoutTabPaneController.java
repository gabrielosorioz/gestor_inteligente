package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CheckoutTabPaneController implements Initializable {

    @FXML
    private TabPane checkoutTabPanel;
    private final Logger log = Logger.getLogger(getClass().getName());
    private static int tabCounter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
            CheckoutTabController checkoutTabController = new CheckoutTabController(this);
            loader.setController(checkoutTabController);
            Tab newCheckoutTab = loader.load();
            newCheckoutTab.setText("Caixa " + (++tabCounter));
            return newCheckoutTab;
        } catch (IOException e) {
            log.severe("Error creating a new checkout tab" + e.getMessage() + e.getCause());
            throw new RuntimeException(e);
        }
    }

    public int getListTabLength(){
        return checkoutTabPanel.getTabs().size();
    }


}
