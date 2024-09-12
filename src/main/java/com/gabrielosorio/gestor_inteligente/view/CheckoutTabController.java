package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.storage.ProductStorage;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class CheckoutTabController implements Initializable {

    private HashMap<String, Product> productData = new HashMap<>();

    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private Tab checkoutTab;

    @FXML
    private HBox btnAddNewCheckoutTab;

    @FXML
    private TextField searchField, qtdField;

    @FXML
    private AnchorPane content;

    @FXML
    private Label totalPriceLbl;

    private SaleTableViewController saleTable;

    private final CheckoutTabPaneController checkoutTabPaneController;

    public CheckoutTabController(CheckoutTabPaneController checkoutTabPaneController) {
        this.checkoutTabPaneController = checkoutTabPaneController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTableView();
        fetchProductsData();
        setUpEvents();
        setDropShadowToBody();
        showTotalPrice();
    }


    private void setUpEvents() {
        setFocusOnSearchField();
        setUpTabActions();
        setUpEventSearchField();
        setUpEventQtdField();
        setUpEventBtnAddCheckout();
    }

    private void setFocusOnSearchField() {
        Platform.runLater(() -> {
            if (checkoutTab.isSelected()) {
                searchField.requestFocus();
            }
        });
    }

    private void setUpSelectTabListener() {
        if (checkoutTab != null) {
            checkoutTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    Platform.runLater(() -> {
                        searchField.requestFocus();
                    });
                }
            });
        }
    }

    private void setUpEventCloseTab() {
        Platform.runLater(() -> {
            checkoutTab.setOnCloseRequest(event -> {
                if (checkoutTabPaneController.getListTabLength() == 1) {
                    event.consume();
                    Platform.runLater(() -> {
                        searchField.requestFocus();
                    });
                }
            });
        });
    }

    private void setUpTabActions() {
        setUpSelectTabListener();
        setUpEventCloseTab();
    }

    private void setUpEventSearchField() {
        searchField.setOnKeyPressed(keyEvent -> {
            KeyCode pressedKey = keyEvent.getCode();
            String search = searchField.getText().trim();
            var qtdStr = qtdField.getText().trim();
            boolean isCodeFieldEmpty = search.isEmpty() || search.isBlank();

            if (pressedKey.equals(KeyCode.F3)) {
                createSale();
            }

            if (pressedKey.equals(KeyCode.ENTER)) {
                if (isCodeFieldEmpty) {
                    qtdField.requestFocus();
                }

                if (!isCodeFieldEmpty) {
                    addItem(search,Integer.parseInt(qtdStr));
                }
            }
        });
    }

    private void setDropShadowToBody() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#b7b7b7"));
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        content.setEffect(shadow);
    }

    private void setUpEventQtdField() {
        qtdField.setText("1");


        qtdField.textProperty().addListener(((observableValue, s, t1) -> {
            String plainText = t1.replaceAll("[^0-9]", "");
            qtdField.setText(plainText);
        }));

        qtdField.focusedProperty().addListener((obsValue, oldValue, newValue) -> {
            if (qtdField.getText().isEmpty() || qtdField.getText().matches("^0+$")) {
                qtdField.setText("1");
            }
        });

        qtdField.setOnKeyPressed(event -> {
            KeyCode pressedKey = event.getCode();

            if (KeyCode.F3 == event.getCode()) {
                createSale();
            }

            if (pressedKey.equals(KeyCode.ENTER)) {
                searchField.requestFocus();
                if (qtdField.getText().isBlank()) {
                    qtdField.setText("1");
                }
            }
        });
    }

    private void setUpEventBtnAddCheckout() {
        btnAddNewCheckoutTab.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 1) {
                checkoutTabPaneController.addNewCheckoutTab();
            }
        });
    }

    private Optional<Product> getProductData(String id) {
        Product product = productData.get(id);
        return Optional.ofNullable(product);
    }

    private void fetchProductsData() {
        ProductStorage.getInstance().getProducts().forEach(product -> {
           String productCode = String.valueOf(product.getProductCode());
           Optional<String> barCode = product.getBarCode();

           productData.put(productCode,product);
           barCode.ifPresent(bc -> productData.put(bc,product));
        });

    }

    private void showTotalPrice(){
        saleTable.totalPricePropProperty().addListener((obsVal, oldVal, newVal) -> {
            if(newVal != null){
                totalPriceLbl.setText(TextFieldUtils.formatText(newVal.setScale(2,RoundingMode.HALF_UP).toPlainString()));
                log.info("Total Price Label Updated: " + newVal);
            }
        });
    }

    private void showProductNotFoundMessage(){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(null);
            alert.setContentText("Produto não encontrado");
            alert.showAndWait();
        });
    }

    private void loadTableView() {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/SaleTableView.fxml"));
            SaleTableViewController saleTableViewController = new SaleTableViewController();
            loader.setController(saleTableViewController);
            TableView tableView = loader.load();
            configureTableViewLayout(tableView);
            content.getChildren().add(0, tableView);
            this.saleTable = loader.getController();
        } catch (IOException e) {
            log.severe("Error loading StockTableView :" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void configureTableViewLayout(TableView<SaleProduct> tableView){
        AnchorPane.setRightAnchor(tableView,5.0);
        AnchorPane.setBottomAnchor(tableView,54.0);
        AnchorPane.setLeftAnchor(tableView,5.0);
    }

    private void addItem(String search, long quantity){
        Optional<Product> productOptional = getProductData(search);

        if(productOptional.isEmpty()){
            showProductNotFoundMessage();
            searchField.clear();
            qtdField.setText("1");
            return;
        }

        var product = productOptional.get();
        final var newItem = new SaleProduct(product,quantity);
        saleTable.add(newItem);
        searchField.clear();
        qtdField.setText("1");

    }

    private void createSale(){
        saleTable.createSale();
    }

}