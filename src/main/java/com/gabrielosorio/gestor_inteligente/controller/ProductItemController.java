package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class ProductItemController implements Initializable {



    @FXML
    private Label codeLbl;

    @FXML
    private Label descriptionLbl;

    @FXML
    private TextField discountField;

    @FXML
    private Label itemLbl;

    @FXML
    private TextField qtdField;

    @FXML
    private Label subtotalPriceLbl;

    @FXML
    private Label unitPriceLbl;

    private CheckoutTabController cktController;

    private SaleProduct saleProduct;

    private int itemOrder;

    public ProductItemController(int itemOrder,SaleProduct saleProduct){
        this.itemOrder = itemOrder;
        this.saleProduct = saleProduct;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        changeQuantity(saleProduct);
        changeDiscount(saleProduct);
        showProductData(saleProduct);
    }

    private void showProductData(SaleProduct saleProduct){
        itemLbl.setText(String.valueOf(itemOrder));
        codeLbl.setText(String.valueOf(saleProduct.getProduct().getProductID()));
        descriptionLbl.setText(saleProduct.getProduct().getDescription());
        unitPriceLbl.setText(String.valueOf(saleProduct.getUnitPrice()));
        qtdField.setText(String.valueOf(saleProduct.getQuantity()));
        discountField.setText(String.valueOf(saleProduct.getDiscount()));
        subtotalPriceLbl.setText(String.valueOf(saleProduct.getSubTotal()));
    }


//    public void setSaleProduct(SaleProduct saleProduct){
//        this.saleProduct = saleProduct;
//    }

    public void setItem(int itemQtd){
        itemLbl.setText(String.valueOf(itemQtd));
    }

    private void changeQuantity(SaleProduct saleProduct){

        if(saleProduct.equals(null)){
            throw new IllegalArgumentException("The SaleProduct is null");
        }

        qtdField.textProperty().addListener(((observableValue, s, t1) -> {

            if(observableValue.getValue().isEmpty() || observableValue.getValue().isBlank()){
                qtdField.setText("0");
            }

            int formattedQuantity = convertToInt(observableValue.getValue());

            saleProduct.setQuantity(formattedQuantity);

            qtdField.setText(String.valueOf(formattedQuantity));

            subtotalPriceLbl.setText(String.valueOf(saleProduct.getSubTotal()));
        }));
    }

    private void changeDiscount(SaleProduct saleProduct){
        TextFieldUtils.addPriceListener(discountField);
        discountField.textProperty().addListener(((observableValue, oldValue, newValue) -> {
            try {
                String newDiscount = observableValue.getValue().replace(",",".");
                saleProduct.setDiscount(convertToBigDecimal(newDiscount));
                subtotalPriceLbl.setText(String.valueOf(saleProduct.getSubTotal()));
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }));
    }

    private int convertToInt(String text){
        // Remove all non-numeric characters
        String plainText = text.replaceAll("[^0-9]", "");
        return Integer.parseInt(plainText);
    }

    public BigDecimal convertToBigDecimal(String valorString) {
        // Remove all dots that are not the decimal separator
        int ultimoPonto = valorString.lastIndexOf('.');
        String valorLimpo = valorString.substring(0, ultimoPonto).replace(".", "")
                + valorString.substring(ultimoPonto);

        // convert to BigDecimal
        return new BigDecimal(valorLimpo);
    }

}
