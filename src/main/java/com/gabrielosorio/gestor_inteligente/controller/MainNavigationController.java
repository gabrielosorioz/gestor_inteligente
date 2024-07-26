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
            frontCheckout = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/sale/front-checkout.fxml"));
            content.getChildren().removeAll();

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


    @FXML
    private void showSaleView(MouseEvent event) {
        loadFrontCheckout();
    }





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        btnSlider.setOnMouseClicked(event -> {
           toggleSideBar();
        });
    }


}
