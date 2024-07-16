    package com.gabrielosorio.gestor_inteligente.controller;

    import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
    import javafx.application.Platform;
    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;
    import javafx.scene.control.Button;
    import javafx.scene.control.TextField;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.HBox;
    import java.net.URL;
    import java.util.ResourceBundle;

    public class PaymentViewController implements Initializable {

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

    @FXML
    private void setCredit(MouseEvent mouseEvent){
       requestPayment(creditField);
    }

    @FXML
    private void setDebit(MouseEvent mouseEvent){
        requestPayment(debitField);
    }

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
