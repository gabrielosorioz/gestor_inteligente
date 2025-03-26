package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.repository.impl.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.psql.PSQLProductStrategy;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;
import com.gabrielosorio.gestor_inteligente.service.impl.ProductServiceImpl;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ProductManagerController implements Initializable, ShortcutHandler {




    @FXML
    private AnchorPane mainContent,tableBody;

    @FXML
    private Pane shadow;

    @FXML
    private Label productLabel;

    @FXML
    private TextField searchField;

    @FXML
    private HBox btnAdd;

    private AnchorPane productForm;
    private ProductFormController productFormController;
    private ProductTbViewController productTbViewController;
    private static int initializeCounter = 0;
    private boolean isProductFormVisible;
    private final Duration FORM_ANIMATION_DURATION = Duration.seconds(0.4);
    private final Duration FADE_DURATION = Duration.seconds(0.2);
    private final double FORM_HIDDEN_POSITION = 750;
    private final double FORM_VISIBLE_POSITION = 0;
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("ProductManagerController initialized " + (++initializeCounter) + " time(s).");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#999999"));
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        tableBody.setEffect(shadow);
        loadTableView();
        loadProductForm();
        configureShadowClick();
        setUpSearchField(searchField);

        btnAdd.setOnMouseClicked(mouseEvent -> addNewProduct());

    }

    @Override
    public void handleShortcut(KeyCode keyCode) {
        if(keyCode.equals(KeyCode.F5)){
            if(isProductFormVisible){
                hideProductForm();
            }
            Platform.runLater(() -> searchField.requestFocus());
        }

        if(keyCode.equals(KeyCode.F4)){
            productFormController.save();
        }

        if(keyCode.equals(KeyCode.ESCAPE)){
            productFormController.cancel();
        }

        if(keyCode.equals(KeyCode.F1)){
            addNewProduct();
        }
    }

    private void loadTableView() {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/product-manager/ProductTbView.fxml"));
            this.productTbViewController = new ProductTbViewController();
            loader.setController(this.productTbViewController);
            TableView tableView = loader.load();
            configureTableViewLayout(tableView);
            configureTableRowFactory(tableView);
            tableBody.getChildren().add(0, tableView);
        } catch (IOException e) {
            log.severe("Error loading StockTableView :" + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void loadProductForm(){
        try {

            FXMLLoader loader =  new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/product-manager/ProductForm.fxml"));
            ProductRepository productRepository = new ProductRepository(new PSQLProductStrategy(ConnectionFactory.getInstance()));
            ProductService productService = new ProductServiceImpl(productRepository);
            loader.setController(new ProductFormController(productTbViewController,this,productService));
            productForm = loader.load();
            productFormController = loader.getController();
            configureProductFormLayout();
        } catch (IOException e){
            log.severe("Error loading the product form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setUpSearchField(TextField searchField){
        TextFieldUtils.setUpperCaseTextFormatter(searchField);
        searchField.textProperty().addListener((obsValue, OldValue, newValue) -> {
            productTbViewController.searchFilteredStock(newValue);
        });
        searchField.setOnKeyPressed(keyEvent -> {
            handleShortcut(keyEvent.getCode());
        });
    @Override
    public void onProductCodeEditAttempt(ProductCodeEditAttemptEvent attemptEvent) {
        Node warningDialog = attemptEvent.getWarningDialog();
        mainContent.getChildren().add(warningDialog);
    }

    @Override
    public void onSave(ProductFormSaveEvent productFormSaveEvent) {
        productsTable.refresh();
        productFormManager.toggle();
    }

    private void configureTableRowFactory(TableView<Product> stockTableView) {
        stockTableView.setRowFactory(new Callback<>() {
            @Override
            public TableRow<Product> call(TableView<Product> tableView) {
                TableRow<Product> row = new TableRow<>() {
                    @Override
                    protected void updateItem(Product item, boolean empty) {
                        super.updateItem(item, empty);
                        setPrefHeight(empty || item == null ? 0 : 68);
                        setOnMouseClicked(event -> {
                            if (item != null) {
                                showProductData(item);
                            }
                        });
                    }
                };
                return row;
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