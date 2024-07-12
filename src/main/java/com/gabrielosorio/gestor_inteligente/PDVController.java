package com.gabrielosorio.gestor_inteligente;

import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.*;

public class PDVController implements Initializable {

    @FXML
    private Button btnCredito,btnDebito,btnDinheiro,btnPix;

    @FXML
    private TextField paymentField1,paymentField2,paymentField3,paymentField4;

    @FXML
    private Label paymentLbl1,paymentLbl2,paymentLbl3,paymentLbl4;

    @FXML
    private Label descontoLbl;

    private List<Label> paymentLabels;
    private List<TextField> paymentFields;
    private List<Payment> payments;
    private Set<String> selectedPaymentTypes;
    private int lastIndexField = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadLists();
    }

    @FXML
    void setCredito(ActionEvent event) {
        setPaymentMethod(btnCredito,PaymentMethod.CREDIT0);
    }

    @FXML
    void setDebito(ActionEvent event) {
        setPaymentMethod(btnDebito,PaymentMethod.DEBITO);
    }

    @FXML
    void setDinheiro(ActionEvent event) {
        setPaymentMethod(btnDinheiro,PaymentMethod.DINHEIRO);
    }

    @FXML
    void setPix(ActionEvent event) {
        setPaymentMethod(btnPix,PaymentMethod.PIX);
    }

    private void loadLists(){
        paymentLabels = Arrays.asList(paymentLbl1,paymentLbl2,paymentLbl3,paymentLbl4);
        paymentFields = Arrays.asList(paymentField1,paymentField2,paymentField3,paymentField4);
        selectedPaymentTypes = new HashSet<>();
        payments = new ArrayList<>();
    }

    private void setPaymentMethod(Button btnPayment, PaymentMethod method){
        String paymentType = btnPayment.getText().trim();

        if(selectedPaymentTypes.contains(paymentType)){
            System.out.println("Payment type already selected.");
            return;
        }

        if(lastIndexField < paymentFields.size()){
            showPaymentInput(btnPayment,paymentFields.get(lastIndexField),paymentLabels.get(lastIndexField));
            selectedPaymentTypes.add(paymentType);
            payments.add(new Payment(method, 0.00));
            System.out.println(method.getDescription() + ": R$ " + payments.get(lastIndexField).getValue());
            lastIndexField++;
        }
    }

    private void showPaymentInput(Button btnPayment,TextField paymentField, Label paymentLabel){
        String paymentName = btnPayment.getText().trim();
        btnPayment.setStyle("-fx-border-color: #000000;");
        paymentLabel.setText(paymentName);
        paymentLabel.setVisible(true);
        paymentField.setVisible(true);
        paymentField.requestFocus();
    }

    private void processPayment(){
    }


}
