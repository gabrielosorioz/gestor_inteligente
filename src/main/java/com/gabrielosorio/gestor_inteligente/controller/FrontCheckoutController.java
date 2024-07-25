package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class FrontCheckoutController implements Initializable {

    @FXML
    private TabPane checkoutPanel;

    private final Logger log = Logger.getLogger(getClass().getName());



    private static int tabCounter;


    public void addNewTab(){
        try {
            Tab newCheckoutTab = getCheckoutTab();
            checkoutPanel.getTabs().add(newCheckoutTab);
            checkoutPanel.getSelectionModel().select(newCheckoutTab);

        } catch (Exception e){
            log.severe("ERROR at load new tab view: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private Tab getCheckoutTab() throws IOException {
        FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/checkout-tab.fxml"));
        Tab newTab = loader.load();
        CheckoutTabController controller = loader.getController();
        newTab.setText("Caixa " + (++tabCounter));
        controller.setFCKController(this);
        return newTab;
    }

    public int getListTabLength(){
        return checkoutPanel.getTabs().size();
    }

    public TabPane getCheckoutPanel(){
        return this.checkoutPanel;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            addNewTab();
        });

    }
}

