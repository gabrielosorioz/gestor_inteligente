package com.gabrielosorio.gestor_inteligente;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class GestorInteligenteApp extends Application {
    double x,y = 0;
    @Override
    public void start(Stage primaryStage) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("fxml/navigation_menu.fxml"));
////        primaryStage.initStyle(StageStyle.UNDECORATED);
//
//        root.setOnMousePressed(event -> {
//            x = event.getSceneX();
//            y = event.getSceneY();
//        });
//
//        root.setOnMouseDragged(event -> {
//            primaryStage.setX(event.getScreenX() - x);
//            primaryStage.setY(event.getScreenY() - y);
//        });
//
//        primaryStage.setScene(new Scene(root, 700, 400));
//        primaryStage.show();



        FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/main_navigation.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
//        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}