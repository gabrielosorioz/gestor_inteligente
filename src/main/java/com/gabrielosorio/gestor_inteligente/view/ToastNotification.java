package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ToastNotification {

    private final Logger log = Logger.getLogger(ToastNotification.class.getName());

    @FXML
    private Pane color;

    @FXML
    private ImageView icon;

    @FXML
    private Text text;

    @FXML
    private Label title;

    private Parent root;

    public ToastNotification(){
        load();
    }


    private void load(){
        FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/ToastNotification.fxml"));
        fxmlLoader.setController(this);
        try {
            this.root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setColor(String color){
        try {
            Color.web(color);

        } catch (IllegalArgumentException e){
            log.warning("Cor inválida: " + color);
            this.color.setStyle("-fx-background-color: " + "transparent" + ";\n" +
                    "    -fx-background-radius: 10 0 0 10px;");
        }
        this.color.setStyle("-fx-background-color: " + color + ";\n" +
                "    -fx-background-radius: 10 0 0 10px;");
    }

    public void setTitle(String message){
        title.setText(message);
    }

    public void setText(String message){
        text.setText(message);
    }

    public void setIcon(Image img){
        icon.setImage(img);
    }

    public void showAndWait(){
        // Configure the Stage
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);
        toastStage.setX(GestorInteligenteApp.getPrimaryStage().getX() + 900);
        toastStage.setY(GestorInteligenteApp.getPrimaryStage().getY() + 50);

        // Create the scene and configure transparency
        Scene scene = new Scene(root);
        // scene.setFill(Color.TRANSPARENT);

        //set scene
        toastStage.setScene(scene);

        // show the Stage
        toastStage.show();
        toastStage.getScene().setFill(Color.TRANSPARENT);

        // fade-in and fade-out animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(2)); // Keep visible for 2 sec

        fadeIn.setOnFinished(event -> fadeOut.play());
        fadeOut.setOnFinished(event -> toastStage.close());

        fadeIn.play();
    }
}
