package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.view.util.SidebarButton;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainNavigationController implements Initializable {

    @FXML
    private VBox menuBtn;

    @FXML
    private AnchorPane mainContent,header,root;

    @FXML
    private ImageView menuIcon;

    private final Map<String,Parent> viewCache = new HashMap<>();

    private SideBarController sideBarController;

    private boolean isSidebarOpen = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addHeaderShadow(header);
        loadSidebar();
        openHome();
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/Sidebar.fxml"));
            Node sidebar = loader.load();
            sideBarController = loader.getController();
            AnchorPane.setBottomAnchor(sidebar,0.0);
            AnchorPane.setTopAnchor(sidebar,0.0);
            root.getChildren().add(1,sidebar);

            sideBarController.addButton(new SidebarButton("Início", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-casa-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-casa-48-white.png",this::openHome));
            sideBarController.addButton(new SidebarButton("Vender", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48-white.png",this::openPDV));
            sideBarController.addButton(new SidebarButton("Produtos", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48-white.png", this::openProductManager));
            sideBarController.addButton(new SidebarButton("Relatório Vendas", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-relatório-gráfico-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-relatório-gráfico-48-white.png",this::openSalesReport));

            menuBtn.setOnMouseClicked(mouseEvent -> {
                toggleSideBar();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScreen(String fxmlPath) {
        Parent newScreen = viewCache.get(fxmlPath);

        if(newScreen == null){
            try {
                FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource(fxmlPath));
                newScreen = loader.load();
                viewCache.put(fxmlPath, newScreen);
            } catch (IOException e) {
                throw new RuntimeException("Error loading screen: " + fxmlPath, e);
            }

        }
        mainContent.getChildren().clear();
        mainContent.getChildren().add(newScreen);

    }

    private void openHome(){
        loadScreen("fxml/Home.fxml");
        Platform.runLater(() -> {
            toggleSideBar();
        });
    }

    private void openPDV(){
        loadScreen("fxml/sale/CheckoutTabPane.fxml");
        Platform.runLater(() -> {
            toggleSideBar();
        });
    }

    private void openProductManager(){
        loadScreen("fxml/product-manager/ProductManager.fxml");
        Platform.runLater(() -> {
            toggleSideBar();
        });
    }

    private void openSalesReport(){
        loadScreen("fxml/reports/SalesReport.fxml");
        Platform.runLater(() -> {
            toggleSideBar();
        });
    }

    private void toggleSideBar(){
        Image menuIconDef = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64.png");
        Image menuIconActive = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64-active.png");

        Platform.runLater(() -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.2));
            VBox sidebar = sideBarController.getSidebar();
            VBox shortcutSidebar = sideBarController.getShortCutSideBar();
            slide.setNode(sidebar);

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(150));
            rotateTransition.setNode(menuBtn);

            if(isSidebarOpen) {
                rotateTransition.setByAngle(-180);
                slide.setToX(-265);
                shortcutSidebar.setVisible(true);
                shortcutSidebar.setStyle("-fx-background-color: #fff;");
                menuIcon.setImage(menuIconDef);
                isSidebarOpen = false;
            } else {
                rotateTransition.setByAngle(180);
                slide.setToX(0);
                menuIcon.setImage(menuIconActive);
                slide.setOnFinished(actionEvent -> {
                    shortcutSidebar.setVisible(false);
                    shortcutSidebar.setStyle("-fx-background-color: transparent;");
                });

                isSidebarOpen = true;
            }

            slide.play();
            rotateTransition.play();
        });
    }

    private void addHeaderShadow(Node node){
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2); // Deslocamento horizontal da sombra
        shadow.setOffsetY(2); // Deslocamento vertical da sombra
        shadow.setRadius(10); // Raio da sombra (mais alto = mais difusa)
        shadow.setColor(Color.color(0.8, 0.8, 0.8, 0.5)); //
        node.setEffect(shadow);
        node.setEffect(shadow);
    }

}
