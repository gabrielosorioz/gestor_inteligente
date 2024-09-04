package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
    private VBox menuBtn;

    @FXML
    private AnchorPane content,header;

    @FXML
    private ImageView iconSaleViewer,iconProductViewer,iconStockViewer,iconProductShortcut,iconSaleShortcut,iconStockShortcut,menuIcon;

    @FXML
    private VBox slider2;

    @FXML
    private VBox shortCutSideBar, shortCutBtnSale, shortCutBtnProductManager, shortCutBtnStockManager;

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

    private final Image ICON_MENU_DEFAULT = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64.png");
    private final Image ICON_MENU_ACTIVE = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64-active.png");

    private String activeScreen = "";

    private void setUpBtnHoverEffect(String currentBtn,Node btn, Node btnLabel, ImageView iconViewer, Image defaultImage, Image imageHover,FadeTransition fadeTransitionBackground){
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
            if(activeScreen.isEmpty() || activeScreen.isBlank()){
                btn.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
                btnLabel.setStyle("-fx-text-fill: " + LBL_DEFAULT_COLOR + ";");
                iconViewer.setImage(defaultImage);
                fadeTransitionBackground.play();
            }
            else if (currentBtn.equals(activeScreen)){

            } else {
                btn.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
                btnLabel.setStyle("-fx-text-fill: " + LBL_DEFAULT_COLOR + ";");
                iconViewer.setImage(defaultImage);
                fadeTransitionBackground.play();
            }


        });

    }

    private void setUpBtnHoverEffect(String currentBtn,Node btn, ImageView iconViewer, Image defaultImage, Image imageHover,FadeTransition fadeTransitionBackground){
        fadeTransitionBackground.setNode(btn);
        fadeTransitionBackground.setFromValue(0.0);
        fadeTransitionBackground.setToValue(1.0);

        btn.setOnMouseEntered(mouseEvent -> {
            btn.setStyle("-fx-background-color: " + BACKGROUND_COLOR_HOVER + ";");
            iconViewer.setImage(imageHover);
            fadeTransitionBackground.play();
        });

        btn.setOnMouseExited(mouseEvent -> {
            if(activeScreen.isEmpty() || activeScreen.isBlank()){
                btn.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
                iconViewer.setImage(defaultImage);
                fadeTransitionBackground.play();
            }
            else if (currentBtn.equals(activeScreen)){

            } else {
                btn.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
                iconViewer.setImage(defaultImage);
                fadeTransitionBackground.play();
            }


        });

    }


    private boolean isOpen = true;

    private void toggleSideBar(){

        Platform.runLater(() -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.2));
            slide.setNode(slider2);

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(150));
            rotateTransition.setNode(menuBtn);

            if(isOpen) {
                rotateTransition.setByAngle(-180);
                slide.setToX(-265);
                shortCutSideBar.setVisible(true);
                menuIcon.setImage(ICON_MENU_DEFAULT);
                isOpen = false;
            } else {
                rotateTransition.setByAngle(180);
                slide.setToX(0);
                menuIcon.setImage(ICON_MENU_ACTIVE);
                slide.setOnFinished(actionEvent -> {
                    shortCutSideBar.setVisible(false);
                });

                isOpen = true;
            }

            slide.play();
            rotateTransition.play();
        });
    }


    private void loadCheckoutTabPane(){
        Node frontCheckout = null;

        try {
            content.getChildren().clear();
            frontCheckout = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/sale/CheckoutTabPane.fxml"));
            content.getChildren().add(0,frontCheckout);
            AnchorPane.setTopAnchor(frontCheckout,45.0);
            AnchorPane.setLeftAnchor(frontCheckout,55.0);

            activeScreen = "sale";

            resetAllBtnStyles();
            resetAllShortcutStyles();
            updateBtnActiveStyle(btnHBoxSale,btnHBoxSaleLbl,iconSaleViewer,ICON_SALE_COLOR_HOVER);
            updateBtnActiveStyle(shortCutBtnSale,iconSaleShortcut,ICON_SALE_COLOR_HOVER);


            Platform.runLater(() -> {
                toggleSideBar();
            });


        } catch (IOException e){
            System.out.println("ERROR: Error at load FXML Front of checkout : " + e.getCause());
            e.printStackTrace();
        }


    }

    private void loadFindProductView(){
        AnchorPane findProductView;

        try {
            content.getChildren().clear();
            findProductView = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/product-manager/ProductManager.fxml"));
            content.getChildren().add(0,findProductView);
            activeScreen = "product";

            resetAllBtnStyles();
            resetAllShortcutStyles();
            updateBtnActiveStyle(btnHBoxProductManager,btnHBoxProductManagerLbl,iconProductViewer,ICON_PRODUCT_COLOR_HOVER);
            updateBtnActiveStyle(shortCutBtnProductManager,iconProductShortcut,ICON_PRODUCT_COLOR_HOVER);



            Platform.runLater(() -> {
                toggleSideBar();
            });


        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadStockManager(){
        AnchorPane stockManagerView;

        try {
            content.getChildren().clear();
            stockManagerView = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/stock/StockManager.fxml"));
            content.getChildren().add(0,stockManagerView);
            activeScreen = "stock";

            resetAllBtnStyles();
            resetAllShortcutStyles();
            updateBtnActiveStyle(btnHBoxStockManager,btnHBoxStockManagerLbl,iconStockViewer,ICON_STOCK_COLOR_HOVER);
            updateBtnActiveStyle(shortCutBtnStockManager,iconStockShortcut,ICON_STOCK_COLOR_HOVER);

            Platform.runLater(() -> {
                toggleSideBar();
            });

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void resetAllBtnStyles(){

        btnHBoxSale.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
        btnHBoxSaleLbl.setStyle("-fx-text-fill: " + LBL_DEFAULT_COLOR + ";");
        iconSaleViewer.setImage(ICON_SALE_DEFAULT_COLOR);

        btnHBoxProductManager.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
        btnHBoxProductManagerLbl.setStyle("-fx-text-fill: " + LBL_DEFAULT_COLOR + ";");
        iconProductViewer.setImage(ICON_PRODUCT_DEFAULT_COLOR);

        btnHBoxStockManager.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
        btnHBoxStockManagerLbl.setStyle("-fx-text-fill: " + LBL_DEFAULT_COLOR + ";");
        iconStockViewer.setImage(ICON_STOCK_DEFAULT_COLOR);

    }

    private void resetAllShortcutStyles(){
        shortCutBtnSale.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
        iconSaleShortcut.setImage(ICON_SALE_DEFAULT_COLOR);

        shortCutBtnStockManager.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
        iconStockShortcut.setImage(ICON_STOCK_DEFAULT_COLOR);

        shortCutBtnProductManager.setStyle("-fx-background-color: " + BACKGROUND_DEFAULT_COLOR + ";");
        iconProductShortcut.setImage(ICON_PRODUCT_DEFAULT_COLOR);
    }

    private void updateBtnActiveStyle(Node btn, Node btnLabel, ImageView imageView,Image imageHover){
        btn.setStyle("-fx-background-color: " + BACKGROUND_COLOR_HOVER + ";");
        btnLabel.setStyle("-fx-text-fill: " + LBL_COLOR_HOVER + ";");
        imageView.setImage(imageHover);
    }

    private void updateBtnActiveStyle(Node btn,ImageView imageView,Image imageHover){
        btn.setStyle("-fx-background-color: " + BACKGROUND_COLOR_HOVER + ";");
        imageView.setImage(imageHover);
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#999999")); // Cor da sombra
        shadow.setRadius(15); // O quão difusa é a sombra
        shadow.setOffsetX(-3); // Deslocamento horizontal da sombra
        shadow.setOffsetY(10); //
        slider2.setEffect(shadow);

        DropShadow shadowHeader = new DropShadow();
        shadowHeader.setColor(Color.web("#999999")); // Cor da sombra
        shadowHeader.setRadius(15); // O quão difusa é a sombra
        shadowHeader.setOffsetX(0); // Deslocamento horizontal da sombra
        shadowHeader.setOffsetY(-5);
        header.setEffect(shadowHeader);

        DropShadow shadowShortCut = new DropShadow();
        shadowShortCut.setColor(Color.web("#999999")); // Cor da sombra
        shadowShortCut.setRadius(15); // O quão difusa é a sombra
        shadowShortCut.setOffsetX(-3); // Deslocamento horizontal da sombra
        shadowShortCut.setOffsetY(10);

        shortCutSideBar.setEffect(shadowShortCut);


        setUpBtnHoverEffect("sale",btnHBoxSale,btnHBoxSaleLbl,iconSaleViewer,ICON_SALE_DEFAULT_COLOR,ICON_SALE_COLOR_HOVER,new FadeTransition(Duration.millis(150)));
        setUpBtnHoverEffect("product",btnHBoxProductManager,btnHBoxProductManagerLbl,iconProductViewer,ICON_PRODUCT_DEFAULT_COLOR,ICON_PRODUCT_COLOR_HOVER,new FadeTransition(Duration.millis(150)));
        setUpBtnHoverEffect("stock",btnHBoxStockManager,btnHBoxStockManagerLbl,iconStockViewer,ICON_STOCK_DEFAULT_COLOR,ICON_STOCK_COLOR_HOVER,new FadeTransition(Duration.millis(150)));
        setUpBtnHoverEffect("sale",shortCutBtnSale, new Label(),iconSaleShortcut,ICON_SALE_DEFAULT_COLOR,ICON_SALE_COLOR_HOVER,new FadeTransition(Duration.millis(150)));
        setUpBtnHoverEffect("product",shortCutBtnProductManager, new Label(),iconProductShortcut,ICON_PRODUCT_DEFAULT_COLOR,ICON_PRODUCT_COLOR_HOVER,new FadeTransition(Duration.millis(150)));
        setUpBtnHoverEffect("stock",shortCutBtnStockManager, new Label(),iconStockShortcut,ICON_STOCK_DEFAULT_COLOR,ICON_STOCK_COLOR_HOVER,new FadeTransition(Duration.millis(150)));

        btnHBoxSale.setOnMouseClicked(mouseEvent -> {
            loadCheckoutTabPane();
        });

        btnHBoxProductManager.setOnMouseClicked(mouseEvent -> {
            loadFindProductView();
        });

        btnHBoxStockManager.setOnMouseClicked(mouseEvent -> {
            loadStockManager();
        });

        menuBtn.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 1){
                toggleSideBar();
            }
        });



    }


}
