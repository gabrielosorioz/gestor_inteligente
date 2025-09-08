package com.gabrielosorio.gestor_inteligente.view.main;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.service.base.*;
import com.gabrielosorio.gestor_inteligente.service.impl.ServiceFactory;
import com.gabrielosorio.gestor_inteligente.service.navigation.NavigationService;
import com.gabrielosorio.gestor_inteligente.service.navigation.NavigationServiceImpl;
import com.gabrielosorio.gestor_inteligente.view.main.helpers.SideBarController;
import com.gabrielosorio.gestor_inteligente.view.main.helpers.SidebarButton;
import com.gabrielosorio.gestor_inteligente.view.shared.RequestFocus;
import com.gabrielosorio.gestor_inteligente.view.shared.ScreenManagerImpl;
import com.gabrielosorio.gestor_inteligente.view.shared.ShortcutHandler;
import com.gabrielosorio.gestor_inteligente.view.shared.base.ScreenManager;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import java.util.ResourceBundle;

public class MainNavigationController implements Initializable {

    @FXML private VBox menuBtn;
    @FXML private AnchorPane mainContent,header,root;
    @FXML private ImageView menuIcon;

    private SideBarController sideBarController;

    private boolean isSidebarOpen = true;

    private ShortcutHandler activeShortcutHandler;

    private final ServiceFactory serviceFactory;
    private final User user;

    private NavigationService navigationService;
    private ScreenManager screenManager;

    public MainNavigationController(ServiceFactory serviceFactory, User user) {
        this.serviceFactory = serviceFactory;
        this.user = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        screenManager = new ScreenManagerImpl(mainContent);
        navigationService = new NavigationServiceImpl(screenManager, serviceFactory, this::collapseSidebarIfOpen,user);

        setupScreenChangeListener();

        Platform.runLater(this::addGlobalKeyFilter);
        addHeaderShadow(header);
        setUpMenuButtonToggle();
        loadSidebar();
        navigationService.openHome();
    }

    private void setupScreenChangeListener() {
        screenManager.setScreenChangeListener(controller -> {
            handleShortcutController(controller);
            handleFocusRequest(controller);
        });
    }

    private void handleShortcutController(Object controller) {
        if (controller instanceof ShortcutHandler) {
            activeShortcutHandler = (ShortcutHandler) controller;
        } else {
            activeShortcutHandler = null;
        }
    }

    private void handleFocusRequest(Object controller) {
        if (controller instanceof RequestFocus) {
            ((RequestFocus) controller).requestFocusOnField();
        }
    }

    private void collapseSidebarIfOpen() {
        Platform.runLater(() -> {
            if (isSidebarOpen) {
                toggleSideBar();
            }
        });
    }

    private void addGlobalKeyFilter() {
        GestorInteligenteApp.getPrimaryStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (shouldIgnoreKeyEvent()) {
                return;
            }

            if (activeShortcutHandler != null) {
                activeShortcutHandler.handleShortcut(event.getCode());
                event.consume();
            }
        });
    }

    private boolean shouldIgnoreKeyEvent() {
        var focusOwner = GestorInteligenteApp.getPrimaryStage().getScene().getFocusOwner();
        return focusOwner instanceof TextInputControl || focusOwner instanceof Button;
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/Sidebar.fxml"));
            Node sidebar = loader.load();
            sideBarController = loader.getController();

            configureSidebarLayout(sidebar);
            createSidebarButtons();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura o layout da sidebar
     */
    private void configureSidebarLayout(Node sidebar) {
        AnchorPane.setBottomAnchor(sidebar, 0.0);
        AnchorPane.setTopAnchor(sidebar, 0.0);
        root.getChildren().add(1, sidebar);
    }

    private void createSidebarButtons() {
        var buttons = new SidebarButton[] {
                new SidebarButton("Início",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-casa-48.png",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-casa-48-white.png",
                        navigationService::openHome),

                new SidebarButton("Vender",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48.png",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48-white.png",
                        navigationService::openPointOfSale),

                new SidebarButton("Produtos",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48.png",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48-white.png",
                        navigationService::openProductManager),

                new SidebarButton("Relatório Vendas",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-relatório-gráfico-48.png",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-relatório-gráfico-48-white.png",
                        navigationService::openSalesReport),

                new SidebarButton("Mov. do caixa",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-caixa-registradora-48.png",
                        "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-caixa-registradora-48-white.png",
                        navigationService::openCheckoutMovement)
        };

        for (SidebarButton button : buttons) {
            sideBarController.addButton(button);
        }
    }

    private void toggleSideBar() {
        Platform.runLater(() -> {
            var animation = createSidebarAnimation();
            animation.play();
        });
    }

    private SidebarAnimation createSidebarAnimation() {
        VBox sidebar = sideBarController.getSidebar();
        VBox shortcutSidebar = sideBarController.getShortCutSideBar();

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.2), sidebar);
        RotateTransition rotate = new RotateTransition(Duration.millis(150), menuBtn);

        if (isSidebarOpen) {
            return createCloseAnimation(slide, rotate, sidebar, shortcutSidebar);
        } else {
            return createOpenAnimation(slide, rotate, sidebar, shortcutSidebar);
        }
    }

    /**
     * Cria animação para fechar sidebar
     */
    private SidebarAnimation createCloseAnimation(TranslateTransition slide, RotateTransition rotate,
                                                  VBox sidebar, VBox shortcutSidebar) {
        rotate.setByAngle(-180);
        slide.setToX(-265);
        slide.setOnFinished(e -> sidebar.setPrefWidth(0));

        shortcutSidebar.setVisible(true);
        shortcutSidebar.setStyle("-fx-background-color: #fff;");

        Image menuIconDef = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64.png");
        menuIcon.setImage(menuIconDef);
        isSidebarOpen = false;

        return new SidebarAnimation(slide, rotate);
    }

    private SidebarAnimation createOpenAnimation(TranslateTransition slide, RotateTransition rotate,
                                                 VBox sidebar, VBox shortcutSidebar) {
        rotate.setByAngle(180);
        sidebar.setPrefWidth(265);
        slide.setToX(0);

        Image menuIconActive = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64-active.png");
        menuIcon.setImage(menuIconActive);

        slide.setOnFinished(e -> {
            shortcutSidebar.setVisible(false);
            shortcutSidebar.setStyle("-fx-background-color: transparent;");
        });

        isSidebarOpen = true;

        return new SidebarAnimation(slide, rotate);
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

    private static class SidebarAnimation {
        private final TranslateTransition slide;
        private final RotateTransition rotate;

        public SidebarAnimation(TranslateTransition slide, RotateTransition rotate) {
            this.slide = slide;
            this.rotate = rotate;
        }

        public void play() {
            slide.play();
            rotate.play();
        }
    }

}
