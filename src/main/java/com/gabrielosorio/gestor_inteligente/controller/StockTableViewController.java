package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.utils.StockDataUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class StockTableViewController implements Initializable {

    @FXML
    private TableColumn<Stock, String> categoryColumn;

    @FXML
    private TableColumn<Stock, String> costPriceCol;

    @FXML
    private TableColumn<Stock, String> descriptionCol;

    @FXML
    private TableColumn<Stock, String> idCol;

    @FXML
    private TableColumn<Stock, String> stockColumn;

    @FXML
    private TableColumn<Stock, String> sellingPriceCol;

    @FXML
    private TableView<Stock> stockTable;

    ObservableList<Stock> stockList = FXCollections.observableArrayList();

    List<Stock> stockData = StockDataUtils.fetchStockData();

    private final Logger log = Logger.getLogger(getClass().getName());

    private void setUpColumns() {
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getCategory().getDescription()));
        costPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getCostPrice().toPlainString()));
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode().toString()));
        stockColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));
        sellingPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getSellingPrice().toPlainString()));

        // set monetary label
        monetaryLabel(costPriceCol);
        monetaryLabel(sellingPriceCol);


        // Remove resizable and reorderable properties
        categoryColumn.setResizable(false);
        costPriceCol.setResizable(false);
        descriptionCol.setResizable(false);
        idCol.setResizable(false);
        stockColumn.setResizable(false);
        sellingPriceCol.setResizable(false);

        categoryColumn.setReorderable(false);
        costPriceCol.setReorderable(false);
        descriptionCol.setReorderable(false);
        idCol.setReorderable(false);
        stockColumn.setReorderable(false);
        sellingPriceCol.setReorderable(false);
    }


    private void monetaryLabel(TableColumn<Stock,String> currencyColumn){
        currencyColumn.setCellFactory(column -> {
            TableCell<Stock, String> cell = new TableCell<>() {
                private final Label currencyLabel = new Label("R$");
                private final Text valueText = new Text();

                {
                    HBox hbox = new HBox(5, currencyLabel, valueText);
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                    setText(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        valueText.setText(item);
                        setGraphic(getGraphic());
                    }
                }
            };
            return cell;
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpColumns();
        stockList.addAll(stockData);
        stockTable.setItems(stockList);
        stockTable.setFocusTraversable(false);
    }
}
