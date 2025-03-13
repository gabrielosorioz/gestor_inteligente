package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.repository.impl.CheckoutMovementRepoImpl;
import com.gabrielosorio.gestor_inteligente.repository.impl.CheckoutRepositoryImpl;
import com.gabrielosorio.gestor_inteligente.repository.strategy.psql.PSQLCheckoutMovementStrategy;
import com.gabrielosorio.gestor_inteligente.repository.strategy.psql.PSQLCheckoutStrategy;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutMovementService;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutService;
import com.gabrielosorio.gestor_inteligente.service.impl.CheckoutMovementServiceImpl;
import com.gabrielosorio.gestor_inteligente.service.impl.CheckoutServiceImpl;
import com.gabrielosorio.gestor_inteligente.view.util.SidebarButton;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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
    private final Map<Parent,Object> viewController = new HashMap<>();

    private SideBarController sideBarController;

    private boolean isSidebarOpen = true;

    private ShortcutHandler activeShortcutHandler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            addGlobalKeyFilter();

        });
        addHeaderShadow(header);
        setUpMenuButtonToggle();
        loadSidebar();
        openHome();
    }

    private void addGlobalKeyFilter(){
        GestorInteligenteApp.getPrimaryStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (GestorInteligenteApp.getPrimaryStage().getScene().getFocusOwner() instanceof TextInputControl) {
                return;
            }

            if (GestorInteligenteApp.getPrimaryStage().getScene().getFocusOwner() instanceof Button) {
                return;
            }

            if (activeShortcutHandler != null) {
                activeShortcutHandler.handleShortcut(event.getCode());
                event.consume();
            }
        });
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
            sideBarController.addButton(new SidebarButton("Mov. do caixa", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-caixa-registradora-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-caixa-registradora-48-white.png",this::openCheckoutMovement));

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
                Object controller = loader.getController();
                // Armazena a view e o controlador
                viewCache.put(fxmlPath, newScreen);
                viewController.put(newScreen,controller);

            } catch (IOException e) {
                throw new RuntimeException("Error loading screen: " + fxmlPath, e);
            }

        }

        Object controller = viewController.get(newScreen);

        if(controller instanceof ShortcutHandler){
            activeShortcutHandler = (ShortcutHandler) controller;
        } else {
            activeShortcutHandler = null;
        }

        if(controller instanceof RequestFocus){
            ((RequestFocus) controller).requestFocusOnField();
        }

        mainContent.getChildren().clear();
        mainContent.getChildren().add(newScreen);

    }

    private void loadScreen(String fxmlPath, Object manualController) {
        Parent newScreen = viewCache.get(fxmlPath);

        if (newScreen == null) {
            try {
                FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource(fxmlPath));

                // Define o controlador manualmente, se fornecido
                if (manualController != null) {
                    loader.setController(manualController);
                }

                newScreen = loader.load();
                Object controller = (manualController != null) ? manualController : loader.getController();

                // Armazena a view e o controlador
                viewCache.put(fxmlPath, newScreen);
                viewController.put(newScreen, controller);
            } catch (IOException e) {
                throw new RuntimeException("Error loading screen: " + fxmlPath, e);
            }
        }

        Object controller = viewController.get(newScreen);

        if (controller instanceof ShortcutHandler) {
            activeShortcutHandler = (ShortcutHandler) controller;
        } else {
            activeShortcutHandler = null;
        }

        if (controller instanceof RequestFocus) {
            ((RequestFocus) controller).requestFocusOnField();
        }

        mainContent.getChildren().clear();
        mainContent.getChildren().add(newScreen);
    }


    private void openHome(){
        loadScreen("fxml/Home.fxml");
        Platform.runLater(() -> {
            if(isSidebarOpen){
                toggleSideBar();
            }
        });
    }

    private void openPDV(){
        loadScreen("fxml/sale/CheckoutTabPane.fxml");
        Platform.runLater(() -> {
            if(isSidebarOpen){
                toggleSideBar();
            }
        });
    }

    private void openProductManager(){
        loadScreen("fxml/product-manager/ProductManager.fxml");
        Platform.runLater(() -> {
            if(isSidebarOpen){
                toggleSideBar();
            }
        });
    }

    private void openSalesReport(){
        loadScreen("fxml/reports/SalesReport.fxml");
        Platform.runLater(() -> {
            if(isSidebarOpen){
                toggleSideBar();
            }
        });
    }

    private void openCheckoutMovement(){
        var checkoutRepository = new CheckoutRepositoryImpl();
        var checkoutMovementRepository = new CheckoutMovementRepoImpl();
        checkoutRepository.init(new PSQLCheckoutStrategy(ConnectionFactory.getInstance()));
        checkoutMovementRepository.init(new PSQLCheckoutMovementStrategy(ConnectionFactory.getInstance()));
        CheckoutMovementService cmService = new CheckoutMovementServiceImpl(checkoutMovementRepository);
        CheckoutService cService = new CheckoutServiceImpl(checkoutRepository,cmService);
        loadScreen("fxml/sale/CheckoutMovement.fxml", new CheckoutMovementController(cService,cmService));
        Platform.runLater(() -> {
            if(isSidebarOpen){
                toggleSideBar();
            }
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
                slide.setOnFinished(actionEvent -> {
                    sidebar.setPrefWidth(0);
                });
                shortcutSidebar.setVisible(true);
                shortcutSidebar.setStyle("-fx-background-color: #fff;");
                menuIcon.setImage(menuIconDef);
                isSidebarOpen = false;
            } else {
                rotateTransition.setByAngle(180);
                sidebar.setPrefWidth(265);
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

    private void setUpMenuButtonToggle(){
        menuBtn.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1) {
                toggleSideBar();
            }
        });
    }

}
