package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.view.util.SidebarButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SideBarController implements Initializable {

    @FXML
    private VBox sideBar,shortCutSideBar;

    private boolean isSidebarOpen = true;

    private List<HBox> sidebarButtons = new ArrayList<>();
    private List<VBox> sidebarShortcuts = new ArrayList<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addShadow(sideBar);
        addShadow(shortCutSideBar);
    }


    public void addButton(SidebarButton sidebarButton){
        HBox buttonContainer = createSidebarButton(sidebarButton);
        VBox shortcutContainer = createShortCutButton(sidebarButton);
        buttonContainer.setUserData(sidebarButton);
        shortcutContainer.setUserData(sidebarButton);

        sideBar.getChildren().add(buttonContainer);
        shortCutSideBar.getChildren().add(shortcutContainer);

        sidebarButtons.add(buttonContainer);
        sidebarShortcuts.add(shortcutContainer);

        setUpButtonHover(buttonContainer,shortcutContainer);

        buttonContainer.setOnMouseClicked(e -> {
            selectButton(buttonContainer,shortcutContainer);
            sidebarButton.getAction().run();
        });

        shortcutContainer.setOnMouseClicked(e -> {
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
        icon.setUserData(false);
        icon.setId("icon");

        vBox.getChildren().add(icon);

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
            shIcon.setUserData(true);
            shIcon.setImage(new Image(sidebarButton.getHoverIconPath()));
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
            imgView.setUserData(false);
            SidebarButton sideBtn = (SidebarButton) vBox.getUserData();
            imgView.setImage(new Image(sideBtn.getIconPath()));
        });
    }

    private void setUpButtonHover(HBox buttonBox, VBox shortcutBox){
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

        shortcutBox.setOnMouseEntered(e -> {
            shortcutBox.lookup("#background").setStyle("-fx-background-color: #695CFE;");
            ImageView shIcon = (ImageView) shortcutBox.lookup("#icon");
            SidebarButton sidebarButton = (SidebarButton) shortcutBox.getUserData();
            shIcon.setImage(new Image(sidebarButton.getHoverIconPath()));
        });

        shortcutBox.setOnMouseExited(e -> {
            ImageView shIcon = (ImageView) shortcutBox.lookup("#icon");

            if(shIcon.getUserData().equals(Boolean.TRUE)){
                return;
            }

            shortcutBox.lookup("#background").setStyle("-fx-background-color: transparent;");
            SidebarButton sidebarButton = (SidebarButton) shortcutBox.getUserData();
            shIcon.setImage(new Image(sidebarButton.getIconPath()));
        });


    }

    public VBox getSidebar(){
        return sideBar;
    }

    public VBox getShortCutSideBar(){
        return shortCutSideBar;
    }

    private void addShadow(Node node){
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2); // Deslocamento horizontal da sombra
        shadow.setOffsetY(2); // Deslocamento vertical da sombra
        shadow.setRadius(10); // Raio da sombra (mais alto = mais difusa)
        shadow.setColor(Color.color(0.8, 0.8, 0.8, 0.5)); //
        node.setEffect(shadow);
        node.setEffect(shadow);
    }

}
