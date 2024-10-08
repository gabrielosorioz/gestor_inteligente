package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ProductManagerController implements Initializable {

    private final Duration FORM_ANIMATION_DURATION = Duration.seconds(0.4);
    private final Duration FADE_DURATION = Duration.seconds(0.2);
    private final double FORM_HIDDEN_POSITION = 750;
    private final double FORM_VISIBLE_POSITION = 0;

    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private AnchorPane mainContent,tableBody;

    @FXML
    private Pane shadow;

    @FXML
    private Label productLabel;

    @FXML
    private TextField searchField;


    @FXML
    private HBox btnHBoxNewProduct;

    private AnchorPane stockForm;
    private ProductFormController productFormController;
    private ProductTbViewController productTbViewController;

    private boolean isStockFormVisible;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#999999"));
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        tableBody.setEffect(shadow);
        loadTableView();
        loadStockForm();
        configureShadowClick();
        setUpSearchField(searchField);

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

    private void setUpSearchField(TextField searchField){
        TextFieldUtils.setUpperCaseTextFormatter(searchField);
        searchField.textProperty().addListener((obsValue, OldValue, newValue) -> {
            productTbViewController.searchFilteredStock(newValue);
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

    private void loadStockForm(){
        try {

            FXMLLoader loader =  new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/product-manager/ProductForm.fxml"));
            loader.setController(new ProductFormController(this));
            stockForm = loader.load();
            productFormController = loader.getController();
            productFormController.setProductTableViewController(this.productTbViewController);
            configureStockFormLayout();
        } catch (IOException e){
            log.severe("Error loading the product form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureStockFormLayout(){
        mainContent.getChildren().add(stockForm);
        AnchorPane.setLeftAnchor(stockForm, 550.0);
        AnchorPane.setRightAnchor(stockForm,0.0);
        AnchorPane.setTopAnchor(stockForm,60.0);
        stockForm.setTranslateX(FORM_HIDDEN_POSITION);
    }

    private void configureShadowClick(){
        shadow.setOnMouseClicked(mouseEvent -> toggleStockForm());
    }

    private void showStockForm(){
        animateForm(FORM_VISIBLE_POSITION,0.2);
        shadow.setVisible(true);
    }

    private void hideStockForm(){
        animateForm(FORM_HIDDEN_POSITION,0.0);
        shadow.setVisible(false);
    }

    private void animateForm(double translateX, double fadeToValue){
        TranslateTransition translateTransition = new TranslateTransition(FORM_ANIMATION_DURATION,stockForm);
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

    public void toggleStockForm(){
        if(isStockFormVisible){
            hideStockForm();
        } else {
            showStockForm();
        }
        isStockFormVisible = !isStockFormVisible;
    }

    private void showProductData(Product product){
        productFormController.setProduct(product);
        toggleStockForm();
    }
}
