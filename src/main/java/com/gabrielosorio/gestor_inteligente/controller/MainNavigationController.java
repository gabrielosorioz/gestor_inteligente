package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainNavigationController implements Initializable {

    @FXML
    private HBox btnHBoxSale,btnHBoxProductManager,btnHBoxStockManager;

    @FXML
    private Label btnHBoxSaleLbl,btnHBoxProductManagerLbl,btnHBoxStockManagerLbl;

    @FXML
    private Button btnSlider;

    @FXML
    private AnchorPane content;

    @FXML
    private ImageView iconSaleViewer,iconProductViewer,iconStockViewer;

    @FXML
    private AnchorPane slider;

    private final String LBL_DEFAULT_COLOR = "#707070";
    private final String LBL_COLOR_HOVER = "#fff";
    private final String BACKGROUND_DEFAULT_COLOR = "#fff";
    private final String BACKGROUND_COLOR_HOVER = "#695CFE";

    private final Image ICON_SALE_COLOR_HOVER = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48-white.png");
    private final Image ICON_SALE_DEFAULT_COLOR = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48.png");

    private final Image ICON_PRODUCT_COLOR_HOVER = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48-white.png");
    private final Image ICON_PRODUCT_DEFAULT_COLOR = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48.png");

    private final Image ICON_STOCK_COLOR_HOVER = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-lista-da-área-de-transferência-48-white.png");
    private final Image ICON_STOCK_DEFAULT_COLOR = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-lista-da-área-de-transferência-48.png");

    private void setUpBtnHoverEffect(Node btn, Node btnLabel, ImageView iconViewer, Image defaultImage, Image imageHover,FadeTransition fadeTransitionBackground){
        fadeTransitionBackground.setNode(btn);
        fadeTransitionBackground.setFromValue(0.0);
        fadeTransitionBackground.setToValue(1.0);

        btn.setOnMouseEntered(mouseEvent -> {
            btn.setStyle("-fx-background-color: " + BACKGROUND_COLOR_HOVER + ";");
            btnLabel.setStyle("-fx-text-fill: " + LBL_COLOR_HOVER + ";");
            iconViewer.setImage(imageHover);
            fadeTransitionBackground.play();
        });

        btn.setOnMouseExited(mouseEvent -> {
            btn.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
            btnLabel.setStyle("-fx-text-fill: " + LBL_DEFAULT_COLOR + ";");
            iconViewer.setImage(defaultImage);
            fadeTransitionBackground.play();
        });

    }






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
            findProductView = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/stock/ProductManager.fxml"));
            content.getChildren().add(0,findProductView);

            Platform.runLater(() -> {
                toggleSideBar();
            });

        } catch (IOException e){
            e.printStackTrace();
        }
    }





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpBtnHoverEffect(btnHBoxSale,btnHBoxSaleLbl,iconSaleViewer,ICON_SALE_DEFAULT_COLOR,ICON_SALE_COLOR_HOVER,new FadeTransition(Duration.millis(150)));
        setUpBtnHoverEffect(btnHBoxProductManager,btnHBoxProductManagerLbl,iconProductViewer,ICON_PRODUCT_DEFAULT_COLOR,ICON_PRODUCT_COLOR_HOVER,new FadeTransition(Duration.millis(150)));
        setUpBtnHoverEffect(btnHBoxStockManager,btnHBoxStockManagerLbl,iconStockViewer,ICON_STOCK_DEFAULT_COLOR,ICON_STOCK_COLOR_HOVER,new FadeTransition(Duration.millis(150)));

        btnSlider.setOnMouseClicked(event -> {
           toggleSideBar();
        });

        btnHBoxSale.setOnMouseClicked(mouseEvent -> {
            loadFrontCheckout();
        });

        btnHBoxProductManager.setOnMouseClicked(mouseEvent -> {
            loadFindProductView();
        });



    }


}
