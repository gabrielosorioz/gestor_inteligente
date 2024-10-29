package com.gabrielosorio.gestor_inteligente;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class GestorInteligenteApp extends Application {
    double x,y = 0;
    private static Stage pStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/MainNavigation.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        pStage = primaryStage;
    }

    public static Stage getPrimaryStage(){
        return pStage;
    }

    public static void main(String[] args) {
        launch();
    }
}