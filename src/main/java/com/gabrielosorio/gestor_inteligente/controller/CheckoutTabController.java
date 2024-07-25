package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CheckoutTabController implements Initializable {

    @FXML
    private Button btnAddCheckout;

    @FXML
    private TextField codeField;

    @FXML
    private AnchorPane content;

    @FXML
    private Tab currentTab;

    @FXML
    private Label itemLbl,codeLbl,descriptionLbl,priceLbl,subtotalLbl;

    @FXML
    private TextField qtdFieldHbox,discountField,qtdField;


    private FrontCheckoutController FCKController;

    private final Logger log = Logger.getLogger(getClass().getName());

    private void addEventOnKeyPressed(TextField codeField, TextField qtdField){
        codeField.setOnKeyPressed(event -> {
            KeyCode pressedKey = event.getCode();
            boolean isEmpty = (codeField.getText().isEmpty() || codeField.getText().isBlank());

            if(pressedKey.equals(KeyCode.ENTER)){
                if(isEmpty){
                    qtdField.requestFocus();
                }
            }

            if(KeyCode.F3 == event.getCode()){
                showPaymentView();
            }
        });

        qtdField.setOnKeyPressed(event -> {
            KeyCode pressedKey = event.getCode();
            boolean isEmpty = (qtdField.getText().isEmpty() || qtdField.getText().isBlank());

            if(pressedKey.equals(KeyCode.ENTER)){
                if(isEmpty){
                    codeField.requestFocus();
                }
            }

            if(KeyCode.F3 == event.getCode()){
                showPaymentView();
            }
        });
    }

    private void setFocusOnCodeField(){
        Platform.runLater(() -> {
            if (currentTab.isSelected()) {
                codeField.requestFocus();
            }
        });
    }

    private void setSelectedTabListener(){
        if(currentTab != null) {
            currentTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    Platform.runLater(() -> {
                        codeField.requestFocus();
                    });
                }
            });
        }
    }

    private void showPaymentView(){
        FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/payment-view.fxml"));
        Stage paymentRoot = new Stage();

        try {
            Scene scene = new Scene(fxmlLoader.load());
            paymentRoot.setScene(scene);
            paymentRoot.show();

        } catch (IOException e){
            log.severe("ERROR at load payment view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setOnCloseTab(Tab tab){
        Platform.runLater(() -> {
            tab.setOnCloseRequest(event -> {
                if(FCKController.getListTabLength() == 1){
                    event.consume();
                    Platform.runLater(()->{
                        codeField.requestFocus();
                    });
                }
            });
        });
    }

    private List<Label> labels;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        btnAddCheckout.setOnMouseClicked(event -> {
            FCKController.addNewTab();
        });

        addEventOnKeyPressed(codeField,qtdField);
        setFocusOnCodeField();
        setSelectedTabListener();
        setOnCloseTab(currentTab);


    }


    public void setFCKController(FrontCheckoutController FCKController){
        this.FCKController = FCKController;
    }

}
