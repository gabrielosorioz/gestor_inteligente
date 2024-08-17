package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.jfoenix.controls.JFXButton;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainNavigationController implements Initializable {

    @FXML
    private AnchorPane slider;

    @FXML
    private AnchorPane content;

    @FXML
    private Button btnSlider;

    @FXML
    private Button btnSale;

    @FXML
    private HBox btnHbox;

    @FXML
    private HBox btnRegisterHbox;

    @FXML
    private Button btnRegister;





    private boolean isClosed = true;

    private void toggleSideBar(){

        Platform.runLater(() -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.2));
            slide.setNode(slider);

            if(isClosed) {
                slide.setToX(-277);
                isClosed = false;
            } else {
                slide.setToX(0);
                isClosed = true;
            }

            slide.play();
        });
    }


    private void loadFrontCheckout(){
        TabPane frontCheckout = null;

        try {
            content.getChildren().clear();
            frontCheckout = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/sale/FrontCheckout.fxml"));
            AnchorPane.setTopAnchor(frontCheckout,60.00);
            AnchorPane.setBottomAnchor(frontCheckout,0.00);
            AnchorPane.setLeftAnchor(frontCheckout,0.00);
            AnchorPane.setRightAnchor(frontCheckout,0.00);
            content.getChildren().add(0,frontCheckout);

            Platform.runLater(() -> {
                toggleSideBar();
            });


        } catch (IOException e){
            System.out.println("ERROR: Error at load FXML Front of checkout : ");
        }


    }

    private void loadFindProductView(){
        AnchorPane findProductView;

        try {
            content.getChildren().clear();
            findProductView = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/stock/StockManager.fxml"));
            content.getChildren().add(0,findProductView);

            Platform.runLater(() -> {
                toggleSideBar();
            });

        } catch (IOException e){
            e.printStackTrace();
        }
    }


    @FXML
    private void showSaleView(MouseEvent event) {
        loadFrontCheckout();
    }





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        btnSlider.setOnMouseClicked(event -> {
           toggleSideBar();
        });

        btnSale.setOnMouseEntered(mouseEvent -> {
            btnHbox.setStyle("-fx-background-color: #511278");
        });

        btnSale.setOnMouseExited(mouseEvent -> {
            btnHbox.setStyle("-fx-background-color: #741AAC");
        });

        btnSale.setOnMousePressed(mouseEvent -> {
           btnHbox.setStyle("-fx-background-color: #3a0d56");
        });

        btnRegister.setOnMouseEntered(mouseEvent -> {
            btnRegisterHbox.setStyle("-fx-background-color: #511278");
        });

        btnRegister.setOnMouseExited(mouseEvent -> {
            btnRegisterHbox.setStyle("-fx-background-color: #741AAC");
        });

        btnRegister.setOnMousePressed(mouseEvent -> {
           btnRegisterHbox.setStyle("-fx-background-color: #3a0d56");
        });

        btnRegister.setOnMouseClicked(event -> {
            loadFindProductView();
        });







    }


}
