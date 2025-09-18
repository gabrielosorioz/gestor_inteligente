package com.gabrielosorio.gestor_inteligente.view.shared.product;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import java.util.List;
import java.util.function.Consumer;

public class ProductSearchableTable {

    private final ProductService productService;
    private final ProductTableFactory tableFactory;
    private final ProductTableFactory.Preset preset;
    private final Double[] customWidths;

    private TableView<Product> tableView;
    private ObservableList<Product> productsObservableList;
    private ProductSearcher productSearcher;

    public ProductSearchableTable(ProductService productService) {
        this.productService = productService;
        this.tableFactory = new ProductTableFactory();
        this.preset = ProductTableFactory.TablePreset.DEFAULT;
        this.customWidths = null;
        initializeData();
    }

    public ProductSearchableTable(ProductService productService, String cssPath, ProductTableFactory.Preset preset) {
        this.productService = productService;
        this.tableFactory = new ProductTableFactory(cssPath);
        this.preset = preset;
        this.customWidths = null;
        initializeData();
    }

    public ProductSearchableTable(ProductService productService, String cssPath,
                                  double codigo, double descricao, double precoCusto,
                                  double precoVenda, double quantidade, double categoria) {
        this.productService = productService;
        this.tableFactory = new ProductTableFactory(cssPath);
        this.preset = null;
        this.customWidths = new Double[]{codigo, descricao, precoCusto, precoVenda, quantidade, categoria};
        initializeData();
    }

    private void initializeData() {
        List<Product> productsList = productService.findAllProducts();
        productsObservableList = FXCollections.observableArrayList(productsList);
        productSearcher = new ProductSearcher(productsObservableList);
    }

    public TableView<Product> createTable() {
        if (customWidths != null) {
            tableView = tableFactory.createTable(productsObservableList,
                    customWidths[0], customWidths[1], customWidths[2],
                    customWidths[3], customWidths[4], customWidths[5]);
        } else {
            tableView = tableFactory.createTable(productsObservableList, preset);
        }
        return tableView;
    }

    public TableView<Product> createTableWithRowFactory(Callback<TableView<Product>, javafx.scene.control.TableRow<Product>> rowFactory) {
        if (customWidths != null) {
            tableView = tableFactory.createTable(productsObservableList,
                    customWidths[0], customWidths[1], customWidths[2],
                    customWidths[3], customWidths[4], customWidths[5]);
            tableView.setRowFactory(rowFactory);
        } else {
            tableView = tableFactory.createTableWithRowFactory(productsObservableList, preset, rowFactory);
        }
        return tableView;
    }

    public void configureSearchField(TextField searchField) {
        SearchFieldConfigurator.configureSearchField(searchField, this::performSearch);
    }

    public void configureSearchFieldWithShortcuts(TextField searchField, Consumer<javafx.scene.input.KeyCode> shortcutHandler) {
        SearchFieldConfigurator.configureSearchFieldWithShortcuts(searchField, this::performSearch, shortcutHandler);
    }

    private void performSearch(String searchTerm) {
        if (tableView != null) {
            tableView.setItems(productSearcher.search(searchTerm));
            tableView.refresh();
        }
    }

    public void refreshData() {
        List<Product> productsList = productService.findAllProducts();
        productsObservableList.setAll(productsList);
        if (tableView != null) {
            tableView.refresh();
        }
    }

    public void addProduct(Product product) {
        productsObservableList.add(product);
        if (tableView != null) {
            tableView.refresh();
        }
    }

    public void setTableAnchors(double top, double right, double bottom, double left) {
        if (tableView != null) {
            tableFactory.setTableAnchors(tableView, top, right, bottom, left);
        }
    }

    // Getters
    public TableView<Product> getTableView() {
        return tableView;
    }

    public ObservableList<Product> getProductsObservableList() {
        return productsObservableList;
    }
}
