package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.view.util.SidebarButton;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainNavigationController implements Initializable {

    @FXML
    private VBox menuBtn;

    @FXML
    private AnchorPane mainContent,header;

    @FXML
    private ImageView menuIcon;

    @FXML
    private VBox slider2,shortCutSideBar;

    private boolean isSidebarOpen = true;

    private List<HBox> sidebarButtons = new ArrayList<>();
    private List<VBox> sidebarShortcuts = new ArrayList<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupMenuToggle();
        addSidebarButtons();
    }

    public void addSidebarButtons() {
        addButton(new SidebarButton("Início", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-casa-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-casa-48-white.png",() -> System.out.println("Ir para Inicio")));
        addButton(new SidebarButton("Vender", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-tag-de-preço-de-venda-48-white.png", this::handleSaleAction));
        addButton(new SidebarButton("Produtos", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-produto-novo-48-white.png", this::handleProductManager));
        addButton(new SidebarButton("Estoque", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-lista-da-área-de-transferência-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-lista-da-área-de-transferência-48-white.png", this::handleStockManager));
        addButton(new SidebarButton("Relatório Vendas", "file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-relatório-gráfico-48.png","file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-relatório-gráfico-48-white.png", this::handleSalesReport));
    }

    private void addButton(SidebarButton sidebarButton){
        HBox buttonContainer = createSidebarButton(sidebarButton);
        VBox shortcutContainer = createShortCutButton(sidebarButton);
        buttonContainer.setUserData(sidebarButton);
        shortcutContainer.setUserData(sidebarButton);

        slider2.getChildren().add(buttonContainer);
        shortCutSideBar.getChildren().add(shortcutContainer);

        sidebarButtons.add(buttonContainer);
        sidebarShortcuts.add(shortcutContainer);

        setUpButtonHover(buttonContainer);

        buttonContainer.setOnMouseClicked(e -> {
            selectButton(buttonContainer,shortcutContainer);
            sidebarButton.getAction().run();
        });

    }

    private HBox createSidebarButton(SidebarButton sidebarButton){
        HBox buttonBox = new HBox();
        buttonBox.setLayoutX(15.0);
        buttonBox.setLayoutY(21.0);
        buttonBox.setPrefHeight(45.0);
        buttonBox.setPrefWidth(257.0);
        buttonBox.getStyleClass().add("sidebar-hbox-btn");
        buttonBox.setId("background");

        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefHeight(45.0);
        iconBox.setPrefWidth(45.0);
        ImageView icon = new ImageView(new Image(sidebarButton.getIconPath()));
        icon.setId("icon");
        icon.setFitWidth(30);
        icon.setFitHeight(30);
        iconBox.getChildren().add(icon);

        Label label = new Label(sidebarButton.getLabel());
        label.getStyleClass().add("sidebar-btn-lbl");
        HBox.setMargin(label, new Insets(0,0,0,20));
        label.setUserData(false);
        label.setId("label");

        HBox labelBox = new HBox(label);
        labelBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        labelBox.setPrefWidth(203);
        labelBox.setPrefHeight(55);

        buttonBox.getChildren().addAll(iconBox, labelBox);
        buttonBox.setOnMouseClicked(e -> sidebarButton.getAction().run());

        return buttonBox;

    }

    private VBox createShortCutButton(SidebarButton sidebarButton){
        VBox vBox = new VBox();
        vBox.getStyleClass().add("shortcut-sidebar-btn");
        vBox.setAlignment(Pos.CENTER);
        vBox.setLayoutX(15.0);
        vBox.setLayoutY(21.0);
        vBox.setPrefHeight(45.0);
        vBox.setPrefWidth(45.0);
        vBox.setId("background");

        ImageView icon = new ImageView(new Image(sidebarButton.getIconPath()));
        icon.setFitHeight(30);
        icon.setFitWidth(30);
        icon.setPickOnBounds(true);
        icon.setPreserveRatio(true);
        icon.setId("icon");

        vBox.getChildren().add(icon);

        if (sidebarButton.getAction() != null) {
            vBox.setOnMouseClicked(event -> sidebarButton.getAction().run());
        }

        return vBox;
    }

    private void selectButton(HBox buttonBox, VBox shortcutBox){
        resetButtonStyle();
        resetShortCutStyle();

        HBox hbox = (HBox) buttonBox.lookup("#background");
        Label lbl = (Label) buttonBox.lookup("#label");
        ImageView img = (ImageView) buttonBox.lookup("#icon");
        SidebarButton sidebarButton = (SidebarButton) buttonBox.getUserData();
        hbox.setStyle("-fx-background-color: #695CFE;");
        lbl.setStyle("-fx-text-fill: #fff;");
        lbl.setUserData(true);
        img.setImage(new Image(sidebarButton.getHoverIconPath()));

        if(shortcutBox.getUserData().equals(buttonBox.getUserData())){
            shortcutBox.lookup("#background").setStyle("-fx-background-color: #695CFE;");
            ImageView shIcon = (ImageView) shortcutBox.lookup("#icon");
            shIcon.setImage(new Image(sidebarButton.getHoverIconPath()));
            toggleSideBar();
        }


    }

    private void resetButtonStyle(){
        sidebarButtons.forEach(hBox -> {
            HBox background = (HBox) hBox.lookup("#background");
            Label lbl = (Label) hBox.lookup("#label");
            lbl.setUserData(false);
            ImageView img = (ImageView) hBox.lookup("#icon");
            SidebarButton sidebarButton = (SidebarButton) hBox.getUserData();
            background.setStyle("-fx-background-color: transparent;");
            lbl.setStyle("-fx-text-fill: #747474;");
            img.setImage(new Image(sidebarButton.getIconPath()));
        });

    }

    private void resetShortCutStyle(){
        sidebarShortcuts.forEach(vBox -> {
            vBox.lookup("#background").setStyle("-fx-background-color: transparent;");
            ImageView imgView = (ImageView) vBox.lookup("#icon");
            SidebarButton sideBtn = (SidebarButton) vBox.getUserData();
            imgView.setImage(new Image(sideBtn.getIconPath()));
        });
    }

    private void setUpButtonHover(HBox buttonBox){
        buttonBox.setOnMouseEntered(event -> {
            HBox hbox = (HBox) buttonBox.lookup("#background");
            Label lbl = (Label) buttonBox.lookup("#label");
            ImageView img = (ImageView) buttonBox.lookup("#icon");
            SidebarButton sidebarButton = (SidebarButton) buttonBox.getUserData();
            hbox.setStyle("-fx-background-color: #695CFE;");
            lbl.setStyle("-fx-text-fill: #fff;");
            img.setImage(new Image(sidebarButton.getHoverIconPath()));

        });

        buttonBox.setOnMouseExited(event -> {

            HBox hbox = (HBox) buttonBox.lookup("#background");
            Label lbl = (Label) buttonBox.lookup("#label");
            ImageView img = (ImageView) buttonBox.lookup("#icon");

            if(lbl.getUserData().equals(Boolean.TRUE)){
                return;
            }
            SidebarButton sidebarButton = (SidebarButton) buttonBox.getUserData();
            hbox.setStyle("-fx-background-color: transparent;");
            lbl.setStyle("-fx-text-fill: #747474;");
            img.setImage(new Image(sidebarButton.getIconPath()));
        });
    }

    private void setupMenuToggle(){
        menuBtn.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 1){
                    toggleSideBar();
                }
            }
        });
    }

    private void toggleSideBar(){
        Image menuIconDef = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64.png");
        Image menuIconActive = new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cardápio-64-active.png");

        Platform.runLater(() -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.2));
            slide.setNode(slider2);

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(150));
            rotateTransition.setNode(menuBtn);

            if(isSidebarOpen) {
                rotateTransition.setByAngle(-180);
                slide.setToX(-265);
                shortCutSideBar.setVisible(true);
                shortCutSideBar.setStyle("-fx-background-color: #fff;");
                menuIcon.setImage(menuIconDef);
                isSidebarOpen = false;
            } else {
                rotateTransition.setByAngle(180);
                slide.setToX(0);
                menuIcon.setImage(menuIconActive);
                slide.setOnFinished(actionEvent -> {
                    shortCutSideBar.setVisible(false);
                    shortCutSideBar.setStyle("-fx-background-color: transparent;");
                });

                isSidebarOpen = true;
            }

            slide.play();
            rotateTransition.play();
        });
    }

    private void handleSaleAction() {
        System.out.println("Abrindo tela de vendas...");
    }

    private void handleProductManager() {
        System.out.println("Abrindo gerenciador de produtos...");
    }

    private void handleStockManager() {
        System.out.println("Abrindo gerenciador de estoque...");
    }

    private void handleSalesReport() {
        System.out.println("Abrindo relatório de vendas...");
    }
}
