package com.gabrielosorio.gestor_inteligente.controller;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import static com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils.formatText;

public class PaymentViewController implements Initializable {

    private final Logger log = Logger.getLogger(PaymentViewController.class.getName());

    @FXML
    private TextField cashField,creditField,debitField,pixField,discountField;

    @FXML
    private HBox cashHbox,creditHbox,debitHbox,pixHbox;

    @FXML
    private Button btnCheckout;

    @FXML
    private Label totalPriceLbl,receiveLbl,paybackLbl,receiveValueLbl,paybackValueLbl,monetaryReceiveLbl,monetaryChangeLbl;


    private Map<HBox, PaymentMethod> paymentHboxMap = new HashMap<>();
    private Map<PaymentMethod,Payment> paymentMethods;
    private final Set<TextField> paymentFieldSet = new HashSet<>();
    private Map<PaymentMethod,TextField> paymentFieldMap = new HashMap<>();

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

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        refreshTotalPrice();
        refreshDiscountPrice();
        setUpDiscountField();

        paymentMethods = new HashMap<>();

        /** Link payment methods to TextField elements JavaFX **/
        paymentFieldMap.put(PaymentMethod.DINHEIRO,cashField);
        paymentFieldMap.put(PaymentMethod.DEBITO,debitField);
        paymentFieldMap.put(PaymentMethod.CREDIT0,creditField);
        paymentFieldMap.put(PaymentMethod.PIX,pixField);

        /** Link payment methods to Hbox elements JavaFX **/
        paymentHboxMap.put(cashHbox,PaymentMethod.DINHEIRO);
        paymentHboxMap.put(debitHbox,PaymentMethod.DEBITO);
        paymentHboxMap.put(creditHbox,PaymentMethod.CREDIT0);
        paymentHboxMap.put(pixHbox,PaymentMethod.PIX);

        /** load payment events in node elements JavaFX*/
        loadHboxPaymentEvents(paymentHboxMap);
        loadPaymentFieldEvents(paymentFieldMap);

        receiveLbl.setVisible(false);
        receiveValueLbl.setVisible(false);
        monetaryReceiveLbl.setVisible(false);
        paybackLbl.setVisible(false);
        paybackValueLbl.setVisible(false);
        monetaryChangeLbl.setVisible(false);

        Platform.runLater(() -> {
            creditHbox.requestFocus();
        });

    }

    private void loadPaymentFieldEvents(Map<PaymentMethod,TextField> paymentFieldMap){
        paymentFieldMap.forEach((paymentMethod, paymentField) -> {
            paymentField.setOnMouseClicked(mouseEvent -> {
                requestPayment(paymentField, paymentMethod);
                paymentField.requestFocus();
                paymentField.positionCaret(paymentField.getText().length());
            });

            paymentField.setOnKeyPressed(keyPressed -> {
                if(keyPressed.getCode().equals(KeyCode.F2)) {
                    processPayment(sale);
                }
            });
        });
    }

    private void loadHboxPaymentEvents(Map<HBox, PaymentMethod> paymentHboxMap){
        paymentHboxMap.forEach((paymentHbox, paymentMethod) -> {
            paymentHbox.setOnMouseClicked(mouseEvent -> {

                TextField paymentField = paymentFieldMap.get(paymentMethod);
                requestPayment(paymentField,paymentMethod);
                paymentField.requestFocus();
                paymentField.positionCaret(paymentField.getText().length());

                paymentField.setOnKeyPressed(keyPressed -> {
                    if(keyPressed.getCode().equals(KeyCode.F2)) {
                        processPayment(sale);
                    }
                });
            });
        });
    }

    private void requestPayment(TextField paymentField, PaymentMethod paymentMethod){

        if (paymentField.getText().isBlank() || paymentField.getText().isEmpty()) {
            paymentField.setText("0,00");
            setPaymentValue(paymentMethod, "0,00");
        }

        if(!paymentFieldSet.contains(paymentField)){
            paymentField.textProperty().addListener((observableValue, oldValue, newValue) -> {
                String formattedText = formatText(newValue);
                setPaymentValue(paymentMethod,formattedText);

                if (!newValue.equals(formattedText)) {
                    Platform.runLater(() -> {
                        paymentField.setText(formattedText);
                        paymentField.positionCaret(paymentField.getText().length());
                    });
                }
            });
            paymentFieldSet.add(paymentField);
        }
    }

    private void processPayment(Sale sale) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (sale == null) {
            throw new IllegalArgumentException("Error processing payment: Sale is null");
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new IllegalArgumentException("Error processing payment: Sale items are null or empty");
        }
        if (sale.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Error processing payment: Total price is <= 0");
        }

        final Set<Payment> uniquePayments = new HashSet<>(paymentMethods.values());
        final List<Payment> listPayment = new ArrayList<>(uniquePayments);
        sale.setPaymentMethods(listPayment);

        for (Payment payment : listPayment) {
            totalAmount = totalAmount.add(payment.getValue());
        }

        if (totalAmount.compareTo(sale.getTotalPrice()) < 0) {
            throw new IllegalArgumentException("Error processing payment: Total amount of payments is less than the sale total price. Total Payments: " + totalAmount + ", Sale Total: " + sale.getTotalPrice());
        }

        listPayment.forEach(payment -> {
            log.info(payment.getPaymentMethod().getDescription() + " R$: " + payment.getValue());
        });
    }

    private void setPaymentValue(PaymentMethod paymentMethod, String value){
        BigDecimal newValue;

        String formattedValue = formatText(value);
        newValue = TextFieldUtils.formatCurrency(formattedValue);

        final Payment payment = paymentMethods.getOrDefault(paymentMethod, new Payment(paymentMethod));
        payment.setValue(newValue);
        paymentMethods.put(paymentMethod,payment);
        calculateTotalAmountPaid();

    }

    private void calculateTotalAmountPaid(){
        final Set<Payment> uniquePayments = new HashSet<>(paymentMethods.values());
        BigDecimal totalAmountPaid = BigDecimal.ZERO.setScale(2,RoundingMode.HALF_UP);

        for (Payment uniquePayment : uniquePayments) {
            totalAmountPaid = totalAmountPaid.add(uniquePayment.getValue());
        }
        sale.setTotalAmountPaid(totalAmountPaid);
        refreshValuesLabel();

    }

    private void refreshValuesLabel(){
        final BigDecimal receiveValue = sale.getTotalPrice().subtract(sale.getTotalAmountPaid()).max(BigDecimal.ZERO).setScale(2,RoundingMode.HALF_UP);
        final BigDecimal changeAmount = sale.getTotalChange();

        if(receiveValue.compareTo(BigDecimal.ZERO) == 0){
            receiveLbl.setVisible(false);
            receiveValueLbl.setVisible(false);
            monetaryReceiveLbl.setVisible(false);
        } else {
            receiveLbl.setVisible(true);
            receiveValueLbl.setVisible(true);
            monetaryReceiveLbl.setVisible(true);

            receiveValueLbl.setText(TextFieldUtils.formatText(receiveValue.toPlainString()));
        }

        if(changeAmount.compareTo(BigDecimal.ZERO) == 0){
            paybackLbl.setVisible(false);
            paybackValueLbl.setVisible(false);
            monetaryChangeLbl.setVisible(false);
        } else {
            paybackLbl.setVisible(true);
            paybackValueLbl.setVisible(true);
            monetaryChangeLbl.setVisible(true);
            paybackValueLbl.setText(TextFieldUtils.formatText(sale.getTotalChange().toPlainString()));
        }

    }

    private void refreshTotalPrice(){
        String totalPriceString = TextFieldUtils.formatText(sale.getTotalPrice().toPlainString());
        totalPriceLbl.setText(totalPriceString);
    }
    
    private void refreshDiscountPrice(){
        String totalDiscount = TextFieldUtils.formatText(sale.getTotalDiscount().toPlainString());
        discountField.setText(totalDiscount);
    }

    private void setUpDiscountField(){
        if (discountField.getText().isBlank() || discountField.getText().isEmpty()) {
            discountField.setText("0,00");
        } else {
            String formattedValue = TextFieldUtils.formatText(discountField.getText());
            discountField.setText(formattedValue);
        }

        discountField.setOnKeyPressed(keyEvent -> {
            KeyCode keyCodePressed = keyEvent.getCode();
            if (keyCodePressed.isArrowKey()) {
                discountField.positionCaret(discountField.getText().length());
            }
        });

        discountField.setOnMouseClicked(mouseEvent -> {
            discountField.positionCaret(discountField.getText().length());
        });

        discountField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            String formattedText = TextFieldUtils.formatText(newValue);

            if (!newValue.equals(formattedText)) {
                Platform.runLater(() -> {
                    discountField.setText(formattedText);
                    discountField.positionCaret(discountField.getText().length());
                    sale.setTotalDiscount(TextFieldUtils.formatCurrency(discountField.getText()));
                    refreshValuesLabel();
                    refreshTotalPrice();
                });
            }
        });

    }

}
