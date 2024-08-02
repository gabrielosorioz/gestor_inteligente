package com.gabrielosorio.gestor_inteligente.controller;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

import static com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils.formatText;

public class PaymentViewController implements Initializable {

    private final Logger log = Logger.getLogger(PaymentViewController.class.getName());

    @FXML
    private TextField cashField,creditField,debitField,pixField;

    @FXML
    private HBox cashHbox,creditHbox,debitHbox,pixHbox;

    @FXML
    private Button btnCheckout;


    @FXML
    private void setCash(MouseEvent mouseEvent){
       requestPayment(cashField);
    }

    private Sale sale;

    public PaymentViewController(Sale sale){
        if (sale == null) {
            throw new IllegalArgumentException("Error at initialize Payment View Controller: Sale is null");
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new IllegalArgumentException("Error at initialize Payment View Controller: Sale items are null or empty");
        }
        if (sale.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Error at initialize Payment View Controller: Total price is <= 0");
        }
        this.sale = sale;

    @FXML
    private void setPix(MouseEvent mouseEvent){
        requestPayment(pixField);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            creditHbox.requestFocus();
            addClickListener(cashField);
            addClickListener(debitField);
            addClickListener(creditField);
            addClickListener(pixField);
        });

    }


    private void requestPayment(TextField paymentField){
        setCurrencyMask(paymentField);
        paymentField.requestFocus();
        paymentField.positionCaret(paymentField.getText().length());
    }

    private void addClickListener(TextField textField) {
            textField.setOnMouseClicked(event -> {
                setCurrencyMask(textField);
                // Mover o cursor para a última posição quando o campo for clicado
                Platform.runLater(() -> textField.positionCaret(textField.getText().length()));
            });
        }

    private void setCurrencyMask(TextField currencyField){
        TextFieldUtils.addPriceListener(currencyField);
    }

}
