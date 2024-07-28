package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
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


    private HashMap<String, Product> generateProducts() {
        HashMap<String, Product> productData = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            int barCode = 1000 + i;
            int productId = 2000 + i;
            Product product = Product.builder()
                    .barCode(String.valueOf(barCode))
                    .productId(productId)
                    .description("Product " + (i + 1))
                    .sellingPrice(new BigDecimal("17.99"))
                    .build();

            // adds product with product id and barcode as a key
            productData.put(String.valueOf(product.getProductID()), product);
            productData.put(product.getBarCode(), product);
        }

        return productData;
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
                showPaymentView();
            }
        });
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

    private void showPaymentView(){
        FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/payment-view.fxml"));
        Stage paymentRoot = new Stage();

        try {
            Scene scene = new Scene(fxmlLoader.load());
            paymentRoot.setScene(scene);
            paymentRoot.show();

        } catch (IOException e){
            log.severe("ERROR at load payment view: " + e.getMessage());
            e.printStackTrace();
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


    private void addProduct(SaleProduct saleProduct) {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/product-item.fxml"));
            ProductItemController controller = new ProductItemController((++itemCounter),saleProduct);
            loader.setController(controller);
            HBox productItemRow = loader.load();
            productTable.getChildren().add(productItemRow);

        } catch (Exception e){
            log.severe("ERROR at load new tab view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int itemCounter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



        Product travesseiro = Product.builder()
                .barCode("789")
                .productId(54)
                .description("Travesseiro Bob Conforto".toUpperCase())
                .sellingPrice(new BigDecimal(1.00))
                .build();

        SaleProduct travesseiroItem = new SaleProduct();

        travesseiroItem.setProduct(travesseiro);
        travesseiroItem.setDiscount(new BigDecimal(0.00));
        travesseiroItem.setQuantity(0);


        btnAddCheckout.setOnMouseClicked(event -> {
            FCKController.addNewTab();
        });

        addEventOnKeyPressed(codeField,qtdField);
        setFocusOnCodeField();
        setSelectedTabListener();
        setOnCloseTab(currentTab);

        for (int i=0 ; i <= 10; i++){
            addProduct(travesseiroItem);
        }

    }

    public void setFCKController(FrontCheckoutController FCKController){
        this.FCKController = FCKController;
    }

}
