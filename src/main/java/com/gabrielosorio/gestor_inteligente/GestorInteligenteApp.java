package com.gabrielosorio.gestor_inteligente;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.repository.factory.PSQLRepositoryFactory;
import com.gabrielosorio.gestor_inteligente.service.base.ScreenLoaderService;
import com.gabrielosorio.gestor_inteligente.service.impl.ServiceFactory;
import com.gabrielosorio.gestor_inteligente.service.view.ScreenLoaderServiceImpl;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class GestorInteligenteApp extends Application {
    double x,y = 0;
    private static Stage pStage;
    PSQLRepositoryFactory psqlRepositoryFactory = new PSQLRepositoryFactory(ConnectionFactory.getInstance(DBScheme.POSTGRESQL));
    private ServiceFactory serviceFactory = new ServiceFactory(psqlRepositoryFactory);
    private ScreenLoaderService screenLoaderService = new ScreenLoaderServiceImpl(serviceFactory);;

    @Override
    public void start(Stage primaryStage) throws IOException {
        pStage = primaryStage;
        screenLoaderService.loadLogin(primaryStage);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

    }

    public static Stage getPrimaryStage(){
        return pStage;
    }

    public static void main(String[] args) {
        launch();
    }
}