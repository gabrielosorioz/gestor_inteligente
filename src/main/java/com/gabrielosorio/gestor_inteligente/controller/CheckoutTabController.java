package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.utils.StockDataUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CheckoutTabController implements Initializable {

    @FXML
    private Button btnAddCheckout;

    @FXML
    private TextField codeField;

    @FXML
    private AnchorPane content;

    @FXML
    private Tab currentTab;

    @FXML
    private TextField qtdField;

    @FXML
    private VBox productTable;

    private HashMap<String,Product> productData = new HashMap<>();

    private HashMap<String,SaleProduct> itemsSales = new HashMap<>();

    private HashMap<String,ProductItemController> saleProductControllers = new HashMap<>();

    private FrontCheckoutController FCKController;

    private int itemCounter;

    private final Logger log = Logger.getLogger(getClass().getName());


    private void fetchProductsData() {
        StockDataUtils.fetchStockData().forEach(item -> {
            Product product = item.getProduct();
            String productCode = String.valueOf(product.getProductCode());
            String barCode = product.getBarCode();
            productData.put(productCode,product);
            productData.put(barCode,product);
        });
    }

    private void addEventOnQtdField(){
        qtdField.setText("1");

        qtdField.textProperty().addListener(((observableValue, s, t1) -> {
            String plainText = t1.replaceAll("[^0-9]", "");
            qtdField.setText(plainText);
        }));

        qtdField.setOnKeyPressed(event -> {
            KeyCode pressedKey = event.getCode();
            boolean isEmpty = (qtdField.getText().isEmpty() || qtdField.getText().isBlank());

            if(pressedKey.equals(KeyCode.ENTER)){
                codeField.requestFocus();
                if(qtdField.getText().isBlank()){
                    qtdField.setText("1");
                }
            }

            if(KeyCode.F3 == event.getCode()){
                createSale();
            }
        });
    }

    private void addEventOnCodeField(){
        codeField.setOnMouseClicked(mouseEvent -> {

            if(qtdField.getText().isBlank()){
                qtdField.setText("1");
            }
        });

        codeField.setOnKeyPressed(keyEvent -> {
            KeyCode pressedKey = keyEvent.getCode();
            String id = codeField.getText().trim();
            String qtdStr = qtdField.getText().trim();
            boolean isCodeFieldEmpty = id.isEmpty() || id.isBlank();

            if(pressedKey.equals(KeyCode.ENTER)){

                if(isCodeFieldEmpty) {
                    qtdField.requestFocus();
                }

                if(!isCodeFieldEmpty){
                    if(itemsSales.containsKey(id)){
                        SaleProduct itemSale = getItemSale(id);
                        int newQuantity = itemSale.getQuantity() + Integer.parseInt(qtdStr);
                        itemSale.setQuantity(newQuantity);
                        updateSaleItem(itemSale);
                        codeField.clear();
                        qtdField.setText("1");
                    }

                    if(!itemsSales.containsKey(id)){
                        Product newItem = getProductData(id);
                        addItemSale(newItem);
                        SaleProduct newSaleProduct = itemsSales.get(id);
                        newSaleProduct.setQuantity(Integer.parseInt(qtdStr));
                        addItemSaleView(id);
                        codeField.clear();
                        qtdField.setText("1");

                    }
                }
            }

            if(pressedKey.equals(KeyCode.F3)){
                createSale();
            }

        });


    }

    private void createSale(){
        HashSet<SaleProduct> itemSet = new HashSet<>(itemsSales.values());
        ArrayList<SaleProduct> items = new ArrayList<>(itemSet);
        final Sale sale = new Sale(items);
        showPaymentView(sale);
    }

    private void addItemSale(Product product){
        if(product == null){
            log.severe("Error at add product to list of products for sale: Product is null.");
            throw new IllegalArgumentException("Product is null");
        }
        if(product.getProductCode() == null && (product.getBarCode() == null)){
            log.severe("Product ID and BarCode are null or empty.");
            throw new IllegalArgumentException("Product ID and BarCode are empty.");
        }

        SaleProduct newSaleItem = new SaleProduct(product);

        if(newSaleItem.getProduct().getProductCode() != null){
           String productCode = String.valueOf(newSaleItem.getProduct().getProductCode());
           itemsSales.put(productCode,newSaleItem);
        }

        if(newSaleItem.getProduct().getBarCode() != null && !newSaleItem.getProduct().getBarCode().isBlank()){
            String barCode = newSaleItem.getProduct().getBarCode();
            itemsSales.put(barCode,newSaleItem);
        }

        log.info("Product added to the list of products for sale." + " BarCode: " + itemsSales.get(newSaleItem.getProduct().getBarCode()).getProduct().getBarCode() + " ID: " + itemsSales.get(String.valueOf(newSaleItem.getProduct().getProductCode())).getProduct().getProductCode());
    }

    private void addItemSaleView(String id){

        if(itemsSales.containsKey(id)){

            SaleProduct newItemSaleView = getItemSale(id);

            try {
                FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/ProductItem.fxml"));
                ProductItemController controller = new ProductItemController((++itemCounter), newItemSaleView);
                loader.setController(controller);

                saleProductControllers.put(newItemSaleView.getProduct().getBarCode(), controller);
                saleProductControllers.put(String.valueOf(newItemSaleView.getProduct().getProductCode()),controller);

                HBox productItemRow = loader.load();
                productTable.getChildren().add(productItemRow);

            } catch (Exception e) {
                log.severe("ERROR at load new tab view: " + e.getMessage());
            }

        }

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

    private SaleProduct getItemSale(String id){
        SaleProduct saleProduct = itemsSales.get(id);
        if(saleProduct != null){
            return saleProduct;
        } else {
            log.severe("Item Sale not found: " + id);
        }
        throw new IllegalArgumentException("Invalid item sale ID" + id);
    }

    private ProductItemController getItemController(String id){
        ProductItemController controller = saleProductControllers.get(id);
        if(controller != null){
            return controller;
        } else {
            log.severe("Item controller not found: " + id);
        }
        throw new IllegalArgumentException("Invalid ID Controller " + id);
    }

    private void setFocusOnCodeField(){
        Platform.runLater(() -> {
            if (currentTab.isSelected()) {
                codeField.requestFocus();
            }
        });
    }

    private void setSelectedTabListener(){
        if(currentTab != null) {
            currentTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    Platform.runLater(() -> {
                        codeField.requestFocus();
                    });
                }
            });
        }
    }

    private void setOnCloseTab(Tab tab){
        Platform.runLater(() -> {
            tab.setOnCloseRequest(event -> {
                if(FCKController.getListTabLength() == 1){
                    event.consume();
                    Platform.runLater(()->{
                        codeField.requestFocus();
                    });
                }
            });
        });
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

    private void updateSaleItem(SaleProduct saleProduct){
        String id = String.valueOf(saleProduct.getProduct().getProductCode());
        ProductItemController controller = getItemController(id);
        controller.setSaleProduct(saleProduct);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fetchProductsData();
        btnAddCheckout.setOnMouseClicked(event -> {
            FCKController.addNewTab();
        });

        setFocusOnCodeField();
        addEventOnCodeField();
        addEventOnQtdField();
        setSelectedTabListener();
        setOnCloseTab(currentTab);

    }

    public void setFCKController(FrontCheckoutController FCKController){
        this.FCKController = FCKController;
    }

}
