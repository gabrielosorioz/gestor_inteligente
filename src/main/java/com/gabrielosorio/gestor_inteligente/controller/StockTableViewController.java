package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.ResourceBundle;

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
    private TableColumn<Stock, String> inventoryColumn;

    @FXML
    private TableColumn<Stock, String> sellingPriceCol;

    @FXML
    private TableView<Stock> inventoryTable;

    ObservableList<Stock> stockList = FXCollections.observableArrayList();

    private void setUpColumns() {
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getCategory().getDescription()));
        costPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getCostPrice().toPlainString()));
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductID().toString()));
        inventoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));
        sellingPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getSellingPrice().toPlainString()));

        // Remove resizable and reorderable properties
        categoryColumn.setResizable(false);
        costPriceCol.setResizable(false);
        descriptionCol.setResizable(false);
        idCol.setResizable(false);
        inventoryColumn.setResizable(false);
        sellingPriceCol.setResizable(false);

        categoryColumn.setReorderable(false);
        costPriceCol.setReorderable(false);
        descriptionCol.setReorderable(false);
        idCol.setReorderable(false);
        inventoryColumn.setReorderable(false);
        sellingPriceCol.setReorderable(false);
    }




    private void setUpTableView() {
        inventoryTable.setRowFactory(new Callback<TableView<Stock>, TableRow<Stock>>() {
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
                        }
                    }
                };
                return row;
            }
        });
    }

    private Product createExampleProduct(int id, String description, BigDecimal costPrice, BigDecimal sellingPrice) {
        Category category = new Category(id % 7 + 1, "Categoria " + (id % 7 + 1)); // Dummy categories
        Supplier supplier = new Supplier(id % 4 + 1, "Fornecedor " + (id % 4 + 1)); // Dummy suppliers

        Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());

        return Product.builder()
                .id(id)
                .productId(id)
                .barCode(String.format("%013d", id * 1000000000000L))
                .description(description)
                .costPrice(costPrice)
                .sellingPrice(sellingPrice)
                .supplier(supplier)
                .category(category)
                .status(Status.ACTIVE)
                .dateCreate(now)
                .dateUpdate(now)
                .dateDelete(null)
                .build();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpColumns();
        setUpTableView();

        // Creating 10 different products
        for (int i = 1; i <= 10; i++) {
            Product product = createExampleProduct(i, "Produto " + i, new BigDecimal("100.00").add(new BigDecimal(i * 10)), new BigDecimal("150.00").add(new BigDecimal(i * 15)));
            stockList.add(new Stock(i, product)); // Assuming quantity of 10 for each product
        }

        inventoryTable.setItems(stockList);
        inventoryTable.setFocusTraversable(false);
    }
}