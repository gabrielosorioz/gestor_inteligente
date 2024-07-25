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


    private void loadFXML(String filename){
        TabPane fxml = null;

        try {
            fxml = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/sale/front-checkout.fxml"));
            content.getChildren().removeAll();

            AnchorPane.setTopAnchor(fxml,60.00);
            AnchorPane.setBottomAnchor(fxml,0.00);
            AnchorPane.setLeftAnchor(fxml,0.00);
            AnchorPane.setRightAnchor(fxml,0.00);
            content.getChildren().add(0,fxml);

            Platform.runLater(() -> {
                toggleSideBar();
            });




        } catch (IOException e){
            System.out.println("ERROR: Error at load FXML file: " + filename);
        }


    }


    @FXML
    private void showSaleView(MouseEvent event) {
        loadFXML("co");
    }



    private boolean isClosed = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



        btnSlider.setOnMouseClicked(event -> {
           toggleSideBar();
        });
    }


}
