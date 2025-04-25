package com.gabrielosorio.gestor_inteligente.view.product;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.events.*;
import com.gabrielosorio.gestor_inteligente.events.listeners.ProductFormListener;
import com.gabrielosorio.gestor_inteligente.events.listeners.ProductManagerCancelEvent;
import com.gabrielosorio.gestor_inteligente.events.listeners.ProductManagerSaveEvent;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;
import com.gabrielosorio.gestor_inteligente.utils.TableViewUtils;
import com.gabrielosorio.gestor_inteligente.view.shared.ShortcutHandler;
import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewFactory;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ProductManagerController implements Initializable, ShortcutHandler, ProductFormListener {

    // FXML Components
    @FXML private AnchorPane mainContent, tableBody;
    @FXML private Pane shadow;
    @FXML private Label productLabel;
    @FXML private TextField searchField;
    @FXML private HBox btnAdd;

    // Services
    private final ProductService productService;


    // UI Components
    private AnchorPane productForm;
    private TableView<Product> productsTable;
    private ProductFormManager productFormManager;

    // Data Collections
    private List<Product> productsList;
    private ObservableList<Product> productsObservableList;
    private final ProductManagerEventBus eventBus = ProductManagerEventBus.getInstance();

    private static final Logger log = Logger.getLogger(ProductManagerController.class.getName());

    public ProductManagerController (ProductService productService) {
        this.productService = productService;
        ProductFormEventBus formEventBus = ProductFormEventBus.getInstance();
        formEventBus.register(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setupProductForm();
        setupProductTable();
        initializeUIEffects();
        setupEventHandlers();
    }


    @Override
    public void handleShortcut(KeyCode keyCode) {
        switch (keyCode) {
            case F5:
                productFormManager.hide();
                Platform.runLater(() -> searchField.requestFocus());
                break;
            case F4:
                eventBus.publish(new ProductManagerSaveEvent());
                break;
            case ESCAPE:
                eventBus.publish(new ProductManagerCancelEvent());
                if(productFormManager.isFormVisible){
                    productFormManager.hide();
                }
                break;
            case F1:
                if(!productFormManager.isFormVisible){
                    eventBus.publish(new ProductAddEvent());
                    productFormManager.toggle();
                }
                break;
        }
    }

    /**
     * Setup the product form and it's manager.
     */
    private void setupProductForm() {
        productForm = getProductForm();
        productFormManager = new ProductFormManager(mainContent, productForm);
    }

    /**
     * Setup products table.
     */

    private void setupProductTable() {
        productsList = productService.findAllProducts();
        productsObservableList = FXCollections.observableArrayList(productsList);

        ProductTableViewFactory tbvFactory = new ProductTableViewFactory();
        productsTable = tbvFactory.get(productsObservableList);

        tableBody.getChildren().add(0, productsTable);
    }

    private AnchorPane getProductForm() {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/product-manager/ProductForm.fxml"));
            loader.setController(new ProductFormController(productService));
            AnchorPane productForm = loader.load();
            return productForm;
        } catch (IOException e) {
            log.severe("Error loading the product form: " + e.getMessage());
            return null;
        }
    }

    private void initializeUIEffects() {
        // Apply shadow effect to table
        DropShadow shadowEffect = new DropShadow();
        shadowEffect.setColor(Color.web("#999999"));
        shadowEffect.setRadius(15);
        shadowEffect.setOffsetX(0);
        shadowEffect.setOffsetY(0);
        tableBody.setEffect(shadowEffect);
        onShadowClick();
    }

    private void setupEventHandlers() {
        setUpSearchField(searchField);
        btnAdd.setOnMouseClicked(mouseEvent -> {eventBus.publish(new ProductAddEvent()); productFormManager.show();});
    }

    private void setUpSearchField(TextField searchField) {
        TextFieldUtils.setUpperCaseTextFormatter(searchField);
        searchField.textProperty().addListener((obsValue, oldValue, newValue) -> searchProduct(newValue));
        searchField.setOnKeyPressed(keyEvent -> handleShortcut(keyEvent.getCode()));
    }

    private void onShadowClick() {
        shadow.setOnMouseClicked(mouseEvent -> productFormManager.toggle());
    }

    private void searchProduct(String search) {
        ProductSearcher productSearcher = new ProductSearcher(productsObservableList);
        productsTable.setItems(productSearcher.search(search));
        productsTable.refresh();
    }

    @Override
    public void onProductCodeEditAttempt(ProductCodeEditAttemptEvent attemptEvent) {
        Node warningDialog = attemptEvent.getWarningDialog();
        mainContent.getChildren().add(warningDialog);
    }

    @Override
    public void onSave(ProductFormSaveEvent productFormSaveEvent) {
        if(productsTable == null){
            productsList = productService.findAllProducts();
            productsObservableList = FXCollections.observableArrayList(productsList);
            ProductTableViewFactory tbvFactory = new ProductTableViewFactory();
            productsTable = tbvFactory.get(productsObservableList);
        }

        productFormSaveEvent.getProduct().ifPresent(product -> {
            productsTable.getItems().add(product);
        });

        productsTable.refresh();
        productFormManager.toggle();
    }

    @Override
    public void onCancel(ProductFormCancelEvent productFormCancelEvent) {
        productFormManager.toggle();
    }

    @Override
    public void onHandleShortcutEvent(ProductFormShortcutEvent productFormShortcutEvent) {
        var keyCode = productFormShortcutEvent.getKeyCode();
        handleShortcut(keyCode);
    }

    /** Inner class to manage the product form functionality */
    private class ProductFormManager {

        private static final Duration FORM_ANIMATION_DURATION = Duration.seconds(0.4);
        private static final Duration FADE_DURATION = Duration.seconds(0.2);
        private static final double FORM_HIDDEN_POSITION = 750;
        private static final double FORM_VISIBLE_POSITION = 0;
        private boolean isFormVisible;

        private final AnchorPane mainContent,productForm;


        public ProductFormManager(AnchorPane mainContent, AnchorPane productForm) {
            this.mainContent = mainContent;
            this.productForm = productForm;
            config();
        }

        private void config(){
            configureProductFormLayout(mainContent,productForm);
        }

        protected void toggle() {
            if (isFormVisible) {
                hide();
            } else {
                show();
            }
            eventBus.publish(new ProductFormToggleEvent(isFormVisible));
            isFormVisible = !isFormVisible;
        }

        private void configureProductFormLayout(AnchorPane mainContent, AnchorPane productForm) {
            mainContent.getChildren().add(productForm);
            AnchorPane.setLeftAnchor(productForm, 550.0);
            AnchorPane.setRightAnchor(productForm, 0.0);
            AnchorPane.setTopAnchor(productForm, 39.0);
            productForm.setTranslateX(FORM_HIDDEN_POSITION);
        }

        private void show() {
            animateForm(FORM_VISIBLE_POSITION, 0.2);
            shadow.setVisible(true);
        }

        private void hide() {
            animateForm(FORM_HIDDEN_POSITION, 0.0);
            shadow.setVisible(false);
        }

        private void animateForm(double translateX, double fadeToValue) {
            // Create and configure transition for form
            TranslateTransition translateTransition = new TranslateTransition(FORM_ANIMATION_DURATION, productForm);
            translateTransition.setToX(translateX);

            // Create and configure transition for shadow
            FadeTransition fadeTransition = new FadeTransition(FADE_DURATION, shadow);
            fadeTransition.setFromValue(shadow.getOpacity());
            fadeTransition.setToValue(fadeToValue);

            // Handle shadow visibility after fade completes
            fadeTransition.setOnFinished(actionEvent -> {
                if (fadeToValue == 0.0) {
                    shadow.setVisible(false);
                }
            });

            // Play animations
            fadeTransition.play();
            translateTransition.play();
        }

    }

    /**
     * Inner class to create product table view
     */
    private class ProductTableViewFactory {
        private final TableView<Product> tableView;

        public ProductTableViewFactory() {
            TableViewFactory<Product> productTableViewFactory = new TableViewFactory<>(Product.class);
            this.tableView = productTableViewFactory.createTableView("/com/gabrielosorio/gestor_inteligente/css/productTbView.css");

        }

        public TableView<Product> get(ObservableList<Product> observableList){
            configure();
            tableView.setItems(observableList);
            return tableView;
        }

        private void configure() {
            configureTableViewLayout(tableView);
            configureTableRowFactory(tableView);
        }

        private void configureTableViewLayout(TableView<Product> tableView) {
            // Configure column widths
            TableViewUtils.getColumnById(tableView, "productCodeProp").setPrefWidth(120.0);
            TableViewUtils.getColumnById(tableView, "descriptionProp").setPrefWidth(410.00);
            TableViewUtils.getColumnById(tableView, "costPriceProp").setPrefWidth(160.00);
            TableViewUtils.getColumnById(tableView, "sellingPriceProp").setPrefWidth(160.00);
            TableViewUtils.getColumnById(tableView, "quantityProp").setPrefWidth(130.0);
            TableViewUtils.getColumnById(tableView, "getCategoryDescription").setPrefWidth(140.0);

            // Set anchors for table
            AnchorPane.setTopAnchor(tableView, 109.0);
            AnchorPane.setRightAnchor(tableView, 9.0);
            AnchorPane.setBottomAnchor(tableView, 0.0);
            AnchorPane.setLeftAnchor(tableView, 9.0);

            // Apply column styling
            tableView.getColumns().forEach(c -> {
                c.setStyle("-fx-alignment: center;");
                TableViewUtils.resetColumnProps(c);
            });
        }

        private void configureTableRowFactory(TableView<Product> tableView) {
            tableView.setRowFactory(new ProductTableRowFactory());
        }

        /**
         * Inner class to handle table row factory implementation
         */
        private class ProductTableRowFactory implements Callback<TableView<Product>, TableRow<Product>> {
            @Override
            public TableRow<Product> call(TableView<Product> tableView) {
                return new TableRow<>() {
                    @Override
                    protected void updateItem(Product item, boolean empty) {
                        super.updateItem(item, empty);
                        setPrefHeight(empty || item == null ? 0 : 68);
                        setOnMouseClicked(event -> {
                            if (item != null) {
                                eventBus.publish(new ProductSelectionEvent(item));
                                productFormManager.toggle();
                            }
                        });
                    }
                };
            }
        }

    }

    /**
     * Inner class to handle product search functionality
     */
    private class ProductSearcher {
        private final ObservableList<Product> productList;

        public ProductSearcher(ObservableList<Product> productList) {
            this.productList = productList;
        }

        public FilteredList<Product> search(String searchTerm) {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return new FilteredList<>(productList, p -> true);
            }

            final String searchLower = searchTerm.toLowerCase();
            return new FilteredList<>(productList, product -> matchesProduct(product, searchLower));
        }

        private boolean matchesProduct(Product product, String searchTerm) {
            if (isNumeric(searchTerm)) {
                long searchNumber = Long.parseLong(searchTerm);

                if (searchNumber == product.getProductCode()) {
                    return true;
                }

                return product.getBarCode()
                        .filter(ProductSearcher::isNumeric)
                        .map(Long::parseLong)
                        .filter(barCode -> barCode == searchNumber)
                        .isPresent();
            }

            return product.getDescription().toLowerCase().contains(searchTerm);
        }

        private static boolean isNumeric(String str) {
            return str.matches("\\d+");
        }
    }
}