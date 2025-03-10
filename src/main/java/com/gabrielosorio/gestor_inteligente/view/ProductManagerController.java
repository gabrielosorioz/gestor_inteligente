package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.storage.PSQLProductStrategy;
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
    }

    private void configureTableViewLayout(TableView<Stock> tableView){
        AnchorPane.setTopAnchor(tableView, 109.0);
        AnchorPane.setRightAnchor(tableView,9.0);
        AnchorPane.setBottomAnchor(tableView,0.0);
        AnchorPane.setLeftAnchor(tableView,9.0);
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
            }
        });
    }

    private void configureProductFormLayout(){
        mainContent.getChildren().add(productForm);
        AnchorPane.setLeftAnchor(productForm, 550.0);
        AnchorPane.setRightAnchor(productForm,0.0);
        AnchorPane.setTopAnchor(productForm,39.0);
        productForm.setTranslateX(FORM_HIDDEN_POSITION);
    }

    private void configureShadowClick(){
        shadow.setOnMouseClicked(mouseEvent -> toggleProductForm());
    }

    private void showProductForm(){
        animateForm(FORM_VISIBLE_POSITION,0.2);
        shadow.setVisible(true);
    }

    private void hideProductForm(){
        animateForm(FORM_HIDDEN_POSITION,0.0);
        shadow.setVisible(false);
    }

    private void animateForm(double translateX, double fadeToValue){
        TranslateTransition translateTransition = new TranslateTransition(FORM_ANIMATION_DURATION,productForm);
        translateTransition.setToX(translateX);

        FadeTransition fadeTransition = new FadeTransition(FADE_DURATION, shadow);
        fadeTransition.setFromValue(shadow.getOpacity());
        fadeTransition.setToValue(fadeToValue);

        fadeTransition.setOnFinished(actionEvent -> {
            if(fadeToValue == 0.0){
                shadow.setVisible(false);
            }
        });

        fadeTransition.play();
        translateTransition.play();
    }

    public void toggleProductForm(){
        if(isProductFormVisible){
            hideProductForm();
            productFormController.lockIDField();
        } else {
            showProductForm();
        }
        isProductFormVisible = !isProductFormVisible;
    }

    private void showProductData(Product product){
        productFormController.setProduct(Optional.ofNullable(product));
        toggleProductForm();
    }

    private void addNewProduct(){
        productFormController.setProduct(Optional.empty());
        productFormController.lockIDField();
        if(!isProductFormVisible){
            toggleProductForm();
        }
    }

    public void addContent(Node node){
        this.mainContent.getChildren().add(node);
    }

}
