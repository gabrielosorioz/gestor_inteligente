package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class StockManagerController implements Initializable {

    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private AnchorPane mainContent;

    @FXML
    private Pane shadow;

    @FXML
    private Label productLabel;

    private AnchorPane stockForm;

    private StockFormController stockController;

    private boolean isStockFormVisible;

    private void showStockForm(){
        TranslateTransition translateStockForm = new TranslateTransition(Duration
                .seconds(0.4),stockForm);
        translateStockForm.setToX(0);
        shadow.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.seconds(0.1),shadow);
        fade.setFromValue(0.0);
        fade.setToValue(0.2);
        fade.play();
        translateStockForm.play();
    }

    private void hideStockForm(){
        TranslateTransition translateProduct = new TranslateTransition(Duration.seconds(0.4),stockForm);
        translateProduct.setToX(750);
        FadeTransition fade = new FadeTransition(Duration.seconds(0.05),shadow);
        fade.setFromValue(0.2);
        fade.setToValue(0.0);
        fade.setOnFinished(event -> shadow.setVisible(false));
        fade.play();
        translateProduct.play();
    }

    private void loadStockForm(){
        try {

            FXMLLoader loader =  new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/stock/StockRegisterForm.fxml"));
            loader.setController(new StockFormController());
            stockForm = loader.load();
            stockController = loader.getController();
            mainContent.getChildren().add(stockForm);
            AnchorPane.setLeftAnchor(stockForm,550.0);
            AnchorPane.setRightAnchor(stockForm,0.0);
            AnchorPane.setTopAnchor(stockForm,60.00);
            stockForm.setTranslateX(stockForm.getPrefWidth());
        } catch (IOException e){
            log.severe("Error loading the product form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void toggleStockForm(){
        if(isStockFormVisible){
            hideStockForm();
        } else {
            showStockForm();
        }
        isStockFormVisible = !isStockFormVisible;
    }



    private void loadTableView(){
        try {
            TableView tableView = FXMLLoader.load(GestorInteligenteApp.class.getResource("fxml/stock/StockTableView.fxml"));
            AnchorPane.setTopAnchor(tableView,252.00);
            AnchorPane.setRightAnchor(tableView,105.00);
            AnchorPane.setBottomAnchor(tableView,148.00);
            AnchorPane.setLeftAnchor(tableView,55.00);
            setCellEvent(tableView);
            mainContent.getChildren().add(0,tableView);
        } catch (IOException e) {
            log.severe("Error loading StockTableView :" + e.getMessage());
            throw new RuntimeException(e);
        }


    }

    private void setCellEvent(TableView stockTableView){
        stockTableView.setRowFactory(new Callback<TableView<Stock>, TableRow<Stock>>() {
            @Override
            public TableRow<Stock> call(TableView<Stock> tableView) {
                TableRow<Stock> row = new TableRow<Stock>() {
                    @Override
                    protected void updateItem(Stock item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setPrefHeight(30); // Altura da linha
                            setGraphic(null);
                            setText(null);
                        } else {
                            setPrefHeight(68);

                            setOnMouseClicked(mouseEvent -> {
                                Stock selectedStockItem = tableView.getSelectionModel().getSelectedItem();
                                showStockData(selectedStockItem);
                            });
                        }
                    }
                };
                return row;
            }
        });
    }

    private void showStockData(Stock stock){
        stockController.setStock(stock);
        toggleStockForm();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadStockForm();
        loadTableView();

        shadow.setOnMouseClicked(mouseEvent -> {
            toggleStockForm();
        });

    }
}
