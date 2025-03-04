package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.service.base.SaleService;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

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
    private Label totalPriceLbl,receiveLbl,paybackLbl,receiveValueLbl,paybackValueLbl,monetaryReceiveLbl,monetaryChangeLbl,
            originalPriceLbl,monetaryOriginalPriceLbl,originalPriceValueLbl;

    private final SaleService saleService;
    private final SaleTableViewController saleTableViewOp;
    private final User user;
    private Map<HBox, PaymentMethod> paymentHboxMap = new HashMap<>();
    private Map<PaymentMethod,Payment> paymentMethods;
    private final Set<TextField> paymentFieldSet = new HashSet<>();
    private Map<PaymentMethod,TextField> paymentFieldMap = new HashMap<>();

    private Sale sale;

    public PaymentViewController(User user, Sale sale, SaleService saleService, SaleTableViewController saleTableViewOp){
        validateSale(sale);
        this.sale = sale;
        this.saleService = saleService;
        this.saleTableViewOp = saleTableViewOp;
        this.user = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        refreshTotalPrice();
        refreshDiscountPrice();
        setUpDiscountField();
        showOriginalPrice();
        setUpHoverPaymentField();
        setUpBtnCheckoutEvent();

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

        // simulating an click to request cash payment
        Platform.runLater(() -> {
            EventHandler<MouseEvent> mouseClickEvent = (EventHandler<MouseEvent>) cashField.getOnMouseClicked();
            if (mouseClickEvent != null) {
                mouseClickEvent.handle(new MouseEvent(
                        MouseEvent.MOUSE_CLICKED,
                        0, 0, 0, 0,
                        MouseButton.PRIMARY, 1,
                        true, true, true, true, true, true, true,
                        true, true, true, null
                ));
            }
        });


    }

    private void setUpBtnCheckoutEvent(){
        btnCheckout.setOnMouseClicked(mouseEvent -> {
            finalizeSale(user,sale);
        });
    }

    private void setUpHoverPaymentField() {
        cashField.focusedProperty().addListener((observableValue, oldValue, isFocused) -> {
            if (isFocused) {
                cashHbox.setStyle("-fx-background-color: #E2EFDD;");
            } else {
                cashHbox.setStyle("-fx-background-color: #fff;");
            }
        });

        pixField.focusedProperty().addListener((observableValue, oldValue, isFocused) -> {
            if (isFocused) {
                pixHbox.setStyle("-fx-background-color: #E2EFDD;");
            } else {
                pixHbox.setStyle("-fx-background-color: transparent;");
            }
        });

        creditField.focusedProperty().addListener((observableValue, oldValue, isFocused) -> {
            if (isFocused) {
                creditHbox.setStyle("-fx-background-color: #E2EFDD;");
            } else {
                creditHbox.setStyle("-fx-background-color: transparent;");
            }
        });

        debitField.focusedProperty().addListener((observableValue, oldValue, isFocused) -> {
            if (isFocused) {
                debitHbox.setStyle("-fx-background-color: #E2EFDD;");
            } else {
                debitHbox.setStyle("-fx-background-color: transparent;");
            }
        });


    }

    private void loadPaymentFieldEvents(Map<PaymentMethod, TextField> paymentFieldMap) {
        List<TextField> paymentFields = Arrays.asList(cashField, pixField, debitField, creditField);

        paymentFieldMap.forEach((paymentMethod, paymentField) -> {
            // Click event on field
            paymentField.setOnMouseClicked(mouseEvent -> {
                requestPayment(paymentField, paymentMethod);
                paymentField.requestFocus();
                paymentField.positionCaret(paymentField.getText().length());
            });

            // Key event pressed setup

            paymentField.setOnKeyPressed(keyPressed -> {
                if (keyPressed.getCode().equals(KeyCode.F2)) {
                    finalizeSale(user,sale);
                } else if (keyPressed.getCode().equals(KeyCode.DOWN)) {
                    //
                    int currentIndex = paymentFields.indexOf(paymentField);
                    int nextIndex = (currentIndex + 1) % paymentFields.size();
                    TextField nextField = paymentFields.get(nextIndex);
                    nextField.requestFocus();
                    TextFieldUtils.lastPositionCursor(nextField);

                    // Call the request payment method on next field
                    PaymentMethod nextPaymentMethod = paymentFieldMap.entrySet().stream()
                            .filter(entry -> entry.getValue().equals(nextField))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);

                    if (nextPaymentMethod != null) {
                        requestPayment(nextField, nextPaymentMethod);
                    }
                } else if (keyPressed.getCode().equals(KeyCode.UP)) {
                    // Navegar para o campo anterior
                    int currentIndex = paymentFields.indexOf(paymentField);
                    int previousIndex = (currentIndex - 1 + paymentFields.size()) % paymentFields.size();
                    TextField previousField = paymentFields.get(previousIndex);
                    previousField.requestFocus();
                    TextFieldUtils.lastPositionCursor(previousField);


                    // Call the request payment method on previous field
                    PaymentMethod previousPaymentMethod = paymentFieldMap.entrySet().stream()
                            .filter(entry -> entry.getValue().equals(previousField))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);

                    if (previousPaymentMethod != null) {
                        requestPayment(previousField, previousPaymentMethod);
                    }
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
                        finalizeSale(user,sale);
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

    private void confirmPayment(Sale sale) {
        final Set<Payment> uniquePayments = new HashSet<>(paymentMethods.values());
        final List<Payment> listPayment = new ArrayList<>(uniquePayments);

        // Remove payments with value equal to zero
        listPayment.removeIf(payment -> payment.getValue().compareTo(BigDecimal.ZERO) == 0);

        sale.setPaymentMethods(listPayment);
    }

    private void finalizeSale(User user,Sale sale){
        confirmPayment(sale);
        saleService.processSale(user,sale);
        closeWindow();
        clearItems();
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

    private void showOriginalPrice(){
        if(sale.getTotalDiscount().compareTo(BigDecimal.ZERO) > 0){
            originalPriceLbl.setVisible(true);
            monetaryOriginalPriceLbl.setVisible(true);
            originalPriceValueLbl.setVisible(true);
            originalPriceValueLbl.setText(TextFieldUtils.formatText(sale.getOriginalTotalPrice().toPlainString()));
        } else {
            originalPriceLbl.setVisible(false);
            monetaryOriginalPriceLbl.setVisible(false);
            originalPriceValueLbl.setVisible(false);
        }
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
                    showOriginalPrice();
                });
            }
        });

    }

    private void validateSale(Sale sale){
        if (sale == null) {
            throw new IllegalArgumentException("Error at initialize Payment View Controller: Sale is null");
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new IllegalArgumentException("Error at initialize Payment View Controller: Sale items are null or empty");
        }
        if (sale.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Error at initialize Payment View Controller: Total price is <= 0");
        }
    }

    private void closeWindow(){
        getStage().close();
    }

    private void clearItems(){
        saleTableViewOp.clearItems();
    }

    private Stage getStage(){
        var stage = (Stage) totalPriceLbl.getScene().getWindow();
        return stage;
    }

}
