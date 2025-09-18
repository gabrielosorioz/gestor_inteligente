package com.gabrielosorio.gestor_inteligente.view.shared;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
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

    public void showAndWait(Stage parentStage, AnimationType animationType) {
        // Configure the Stage
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);

        // Calcular posição automaticamente baseada no stage pai
        double parentX = parentStage.getX();
        double parentY = parentStage.getY();
        double parentWidth = parentStage.getWidth();
        double parentHeight = parentStage.getHeight();

        // Posicionar no canto superior direito do stage pai
        toastStage.setX(parentX + parentWidth - 320); // 320 é a largura aproximada do toast
        toastStage.setY(parentY + 50);

        // Create the scene and configure transparency
        Scene scene = new Scene(root);
        toastStage.setScene(scene);
        toastStage.show();
        toastStage.getScene().setFill(Color.TRANSPARENT);

        // Aplicar animação baseada no tipo
        applyAnimation(toastStage, animationType);
    }

    private void applyAnimation(Stage toastStage, AnimationType animationType) {
        switch (animationType) {
            case FADE_IN_OUT:
                applyFadeAnimation(toastStage);
                break;
            case SLIDE_DOWN_FROM_TOP:
                applySlideDownAnimation(toastStage);
                break;
            case SLIDE_UP_FROM_BOTTOM:
                applySlideUpAnimation(toastStage);
                break;
            case SLIDE_IN_FROM_RIGHT:
                applySlideRightAnimation(toastStage);
                break;
            case SLIDE_IN_FROM_LEFT:
                applySlideLeftAnimation(toastStage);
                break;
            case SCALE_IN_OUT:
                applyScaleAnimation(toastStage);
                break;
            default:
                applyFadeAnimation(toastStage);
                break;
        }
    }

    private void applyFadeAnimation(Stage toastStage) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(2));

        fadeIn.setOnFinished(event -> fadeOut.play());
        fadeOut.setOnFinished(event -> toastStage.close());
        fadeIn.play();
    }

    private void applySlideDownAnimation(Stage toastStage) {
        // Salvar posição original
        double originalY = toastStage.getY();
        toastStage.setY(originalY - 100); // Começar acima

        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideIn.setFromY(-100);
        slideIn.setToY(0);

        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideOut.setFromY(0);
        slideOut.setToY(-100);
        slideOut.setDelay(Duration.seconds(2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        slideIn.setOnFinished(event -> slideOut.play());
        slideOut.setOnFinished(event -> {
            fadeOut.play();
            fadeOut.setOnFinished(e -> toastStage.close());
        });

        slideIn.play();
    }

    private void applySlideUpAnimation(Stage toastStage) {
        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideIn.setFromY(100);
        slideIn.setToY(0);

        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideOut.setFromY(0);
        slideOut.setToY(100);
        slideOut.setDelay(Duration.seconds(2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        slideIn.setOnFinished(event -> slideOut.play());
        slideOut.setOnFinished(event -> {
            fadeOut.play();
            fadeOut.setOnFinished(e -> toastStage.close());
        });

        slideIn.play();
    }

    private void applySlideRightAnimation(Stage toastStage) {
        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideIn.setFromX(300);
        slideIn.setToX(0);

        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideOut.setFromX(0);
        slideOut.setToX(300);
        slideOut.setDelay(Duration.seconds(2));

        slideIn.setOnFinished(event -> slideOut.play());
        slideOut.setOnFinished(event -> toastStage.close());

        slideIn.play();
    }

    private void applySlideLeftAnimation(Stage toastStage) {
        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideIn.setFromX(-300);
        slideIn.setToX(0);

        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(Duration.millis(500), root);
        slideOut.setFromX(0);
        slideOut.setToX(-300);
        slideOut.setDelay(Duration.seconds(2));

        slideIn.setOnFinished(event -> slideOut.play());
        slideOut.setOnFinished(event -> toastStage.close());

        slideIn.play();
    }

    private void applyScaleAnimation(Stage toastStage) {
        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(Duration.millis(500), root);
        scaleIn.setFromX(0.1);
        scaleIn.setFromY(0.1);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        javafx.animation.ScaleTransition scaleOut = new javafx.animation.ScaleTransition(Duration.millis(500), root);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.1);
        scaleOut.setToY(0.1);
        scaleOut.setDelay(Duration.seconds(2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        scaleIn.setOnFinished(event -> scaleOut.play());
        scaleOut.setOnFinished(event -> {
            fadeOut.play();
            fadeOut.setOnFinished(e -> toastStage.close());
        });

        scaleIn.play();
    }

    public enum AnimationType {
        FADE_IN_OUT,
        SLIDE_DOWN_FROM_TOP,
        SLIDE_UP_FROM_BOTTOM,
        SLIDE_IN_FROM_RIGHT,
        SLIDE_IN_FROM_LEFT,
        SCALE_IN_OUT
    }
}
