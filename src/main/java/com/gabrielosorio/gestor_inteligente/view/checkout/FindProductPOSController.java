package com.gabrielosorio.gestor_inteligente.view.checkout;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;
import com.gabrielosorio.gestor_inteligente.view.shared.product.ProductSearchableTable;
import com.gabrielosorio.gestor_inteligente.view.shared.product.ProductTableFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import java.net.URL;
import java.util.ResourceBundle;

public class FindProductPOSController implements Initializable {
    @FXML private AnchorPane tableContainer;
    @FXML private TextField searchField;

    private final ProductService productService;
    private ProductSearchableTable searchableTable;
    private String tableCssPath = "/com/gabrielosorio/gestor_inteligente/css/findProductTableViewPOS.css";

    public FindProductPOSController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSearchableTable();
    }

    private void setupSearchableTable() {

        var customPreset = ProductTableFactory.TablePreset.custom(
                 80,
                 300,
                 120,
                 120,
                 120,
                 0);
        searchableTable = new ProductSearchableTable(productService, tableCssPath, customPreset);

        TableView<Product> tableView = searchableTable.createTableWithRowFactory(this::createTableRow);

        // Remove a coluna 'Categoria'
        searchableTable.getTableView()
                .getColumns()
                .remove(5);


        // Configurar campo de pesquisa
        searchableTable.configureSearchField(searchField);

        // Definir anchors da tabela
        searchableTable.setTableAnchors(70.0, 10.0, 10.0, 10.0);

        // Adicionar tabela ao container
        tableContainer.getChildren().add(tableView);
    }

    private TableRow<Product> createTableRow(TableView<Product> tableView) {
        return new TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setPrefHeight(empty || item == null ? 0 : 68);

                setOnMouseClicked(event -> {
                    if (item != null) {
                        handleProductSelection(item);
                    }
                });
            }
        };
    }

    private void handleProductSelection(Product product) {
        System.out.println("Produto selecionado: " + product.getDescription());
    }
}