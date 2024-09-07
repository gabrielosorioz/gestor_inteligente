package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.utils.StockDataUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class CheckoutTabController implements Initializable {

    private HashMap<String,Product> productData = new HashMap<>();

    private HashMap<String,SaleProduct> cartProduct = new HashMap<>();
    private ObservableList<SaleProduct> cartProductObsList = FXCollections.observableArrayList();
    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private TableView<SaleProduct> cartTable;

    @FXML
    private TableColumn<SaleProduct, String> descriptionCol;

    @FXML
    private TableColumn<SaleProduct, String> discountCol;

    @FXML
    private TableColumn<SaleProduct, String> codeCol;

    @FXML
    private TableColumn<SaleProduct, String> quantityCol;

    @FXML
    private TableColumn<SaleProduct, String> sellingPriceCol;

    @FXML
    private TableColumn<SaleProduct, String> subTotalCol;

    @FXML
    private Tab checkoutTab;

    @FXML
    private HBox btnAddNewCheckoutTab;

    @FXML
    private TextField searchField,qtdField;




    private final CheckoutTabPaneController checkoutTabPaneController;

    public CheckoutTabController(CheckoutTabPaneController checkoutTabPaneController){
        this.checkoutTabPaneController = checkoutTabPaneController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fetchProductsData();
        setUpColumns();

        setFocusOnSearchField();
        setSelectedTabListener();
        setOnCloseTab();
        cartTable.setItems(cartProductObsList);

        cartTable.setFocusTraversable(false);

        btnAddNewCheckoutTab.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 1){
                checkoutTabPaneController.addNewCheckoutTab();
            }
        });

        setUpEventSearchField();

    }

    private void monetaryLabel(TableColumn<SaleProduct,String> currencyColumn){
        currencyColumn.setCellFactory(column -> {
            TableCell<SaleProduct, String> cell = new TableCell<>() {
                private final Label currencyLabel = new Label("R$");
                private final Text valueText = new Text();

                {
                    currencyLabel.setStyle(
                            "-fx-text-fill: black;"
                    );
                    currencyLabel.setPrefWidth(50);
                    valueText.setTextAlignment(TextAlignment.RIGHT);
                    HBox hbox = new HBox(5, currencyLabel, valueText);
                    hbox.setAlignment(Pos.CENTER_LEFT);
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


    private void setUpColumns(){
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode().toString()));
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        sellingPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getSellingPrice().toPlainString()));
        quantityCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));
        discountCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiscount().toPlainString()));
        subTotalCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubTotal().toPlainString()));

        monetaryLabel(subTotalCol);
        monetaryLabel(discountCol);
        monetaryLabel(sellingPriceCol);

        codeCol.setResizable(false);
        descriptionCol.setResizable(false);
        sellingPriceCol.setResizable(false);
        quantityCol.setResizable(false);
        discountCol.setResizable(false);
        subTotalCol.setResizable(false);

        codeCol.setReorderable(false);
        descriptionCol.setReorderable(false);
        sellingPriceCol.setReorderable(false);
        discountCol.setReorderable(false);
        subTotalCol.setReorderable(false);
        quantityCol.setReorderable(false);

        codeCol.setSortable(false);
        descriptionCol.setSortable(false);
        sellingPriceCol.setSortable(false);
        subTotalCol.setSortable(false);
        discountCol.setSortable(false);
        quantityCol.setSortable(false);
    }

    private void setFocusOnSearchField(){
        Platform.runLater(() -> {
            if (checkoutTab.isSelected()) {
                searchField.requestFocus();
            }
        });
    }

    private void setSelectedTabListener(){
        if(checkoutTab != null) {
            checkoutTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    Platform.runLater(() -> {
                        searchField.requestFocus();
                    });
                }
            });
        }
    }

    private void setOnCloseTab(){
        Platform.runLater(() -> {
            checkoutTab.setOnCloseRequest(event -> {
                if(checkoutTabPaneController.getListTabLength() == 1){
                    event.consume();
                    Platform.runLater(()->{
                        searchField.requestFocus();
                    });
                }
            });
        });
    }

    private void setUpEventSearchField(){
        searchField.setOnKeyPressed(keyEvent -> {
            KeyCode pressedKey = keyEvent.getCode();
            String search = searchField.getText().trim();
            String qtdStr = qtdField.getText().trim();
            boolean isCodeFieldEmpty = search.isEmpty() || search.isBlank();

            if(pressedKey.equals(KeyCode.ENTER)){
                if(!isCodeFieldEmpty){

                    if(cartProduct.containsKey(search)){
                        increaseProductToCart(search,Integer.parseInt(qtdStr));
                    } else {
                        insertProductToCart(search, Integer.parseInt(qtdStr));
                    }

                    searchField.clear();
                    qtdField.setText("1");

                }


            }

        });
    }

    private Product getProductData(String id){
        Product product = productData.get(id);
        if(product != null){
            return product;
        } else {
            log.severe("Product not found for id " + id);
        }
        throw new IllegalArgumentException("Invalid Product ID: " + id);
    }

    private void insertProductToCart(String id, int quantity){
        Product product = getProductData(id);
        ProductValidator.validate(product);
        final SaleProduct newSaleItem = new SaleProduct(product);
        newSaleItem.setQuantity(quantity);

        cartProduct.put(String.valueOf(newSaleItem.getProduct().getProductCode()),newSaleItem);
        if(newSaleItem.getProduct().hasBarCode()){
            cartProduct.put(newSaleItem.getProduct().getBarCode(),newSaleItem);
        }

        cartProductObsList.add(newSaleItem);
        cartTable.refresh();
        log.info("Product added to the list of products for sale." + " BarCode: " + cartProduct.get(newSaleItem.getProduct().getBarCode()).getProduct().getBarCode() + " ID: " + cartProduct.get(String.valueOf(newSaleItem.getProduct().getProductCode())).getProduct().getProductCode());
    }

    private void increaseProductToCart(String id, int quantity){
        SaleProduct itemSale = searchProductInCart(id);
        itemSale.setQuantity(itemSale.getQuantity() + quantity);
        cartTable.refresh();
    }

    private SaleProduct searchProductInCart(String search){
        SaleProduct saleProduct = cartProduct.get(search);
        if(saleProduct != null){
            return saleProduct;
        } else {
            log.severe("Cart product not found: " + search);
        }
        throw new IllegalArgumentException("Invalid cart product ID" + search);
    }

    private void fetchProductsData() {
        StockDataUtils.fetchStockData().forEach(item -> {
            Product product = item.getProduct();
            String productCode = String.valueOf(product.getProductCode());
            String barCode = product.getBarCode();
            productData.put(productCode,product);
            productData.put(barCode,product);
            System.out.println(product);
        });
    }

    private void createSale(){
        HashSet<SaleProduct> itemSet = new HashSet<>(cartProduct.values());
        ArrayList<SaleProduct> items = new ArrayList<>(itemSet);
        final Sale sale = new Sale(items);
        showPaymentView(sale);
    }

    private void showPaymentView(Sale sale){

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/PaymentView.fxml"));
            Stage paymentRoot = new Stage();
            PaymentViewController controller = new PaymentViewController(sale);
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load());
            paymentRoot.setScene(scene);
            paymentRoot.show();

        } catch (Exception e){
            log.severe("ERROR at load payment view: " + e.getMessage());
            e.printStackTrace();
        }
    }




}