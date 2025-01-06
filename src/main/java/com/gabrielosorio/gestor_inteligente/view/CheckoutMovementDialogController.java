package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class CheckoutMovementDialogController implements Initializable, RequestFocus {

    @FXML
    private TextArea obsField;

    @FXML
    private AnchorPane mainContent;

    @FXML
    private Button btnOk;

    @FXML
    private Label title;

    @FXML
    private TextField valueField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Foi mal por comer a sua prima");
        priceListener(valueField);
    }

    public Button getBtnOk(){
        return btnOk;
    }

    public BigDecimal getValue(){
        return TextFieldUtils.formatCurrency(valueField.getText());
    }

    private void priceListener(TextField priceField) {
        if (priceField.getText().isBlank() || priceField.getText().isEmpty()) {
            priceField.setText("0,00");
        } else {
            String formattedValue = TextFieldUtils.formatText(priceField.getText());
            priceField.setText(formattedValue);
        }

        priceField.setOnMouseClicked(mouseEvent -> {
            priceField.positionCaret(priceField.getText().length());
        });

        priceField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            String formattedText = TextFieldUtils.formatText(newValue);

            if (!newValue.equals(formattedText)) {
                Platform.runLater(() -> {
                    priceField.setText(formattedText);
                    priceField.positionCaret(priceField.getText().length());
                });
            }
        });
    }

    public void close(){
        AnchorPane parent = (AnchorPane) mainContent.getParent();
        parent.getChildren().remove(mainContent);
    }

    @Override
    public void requestFocusOnField() {
        valueField.requestFocus();
        valueField.positionCaret(valueField.getLength());
    }
}
