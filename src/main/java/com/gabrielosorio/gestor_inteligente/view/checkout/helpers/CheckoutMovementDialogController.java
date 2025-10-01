package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import com.gabrielosorio.gestor_inteligente.view.shared.RequestFocus;
import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

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

    private BiConsumer<BigDecimal,String> onConfirmAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        obsField.clear();
        onEscapeEvent(obsField);
        onEscapeEvent(valueField);
        priceListener(valueField);
        btnOk.setOnAction(e -> {
            if(onConfirmAction != null){
                onConfirmAction.accept(getValue(),getObs());
            }
        });
    }

    public void setOnConfirm(BiConsumer<BigDecimal,String> onConfirmAction){
        this.onConfirmAction = onConfirmAction;
    }

    public BigDecimal getValue(){
        return TextFieldUtils.formatCurrency(valueField.getText());
    }

    public String getObs() {
        return obsField.getText();
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
        obsField.clear();
        valueField.clear();
    }

    private void onEscapeEvent(Node node) {
        node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (mainContent != null && mainContent.getParent() instanceof AnchorPane parent) {
                    parent.getChildren().remove(mainContent);
                    obsField.clear();
                }
                event.consume();
            }
        });
    }

    @Override
    public void requestFocusOnField() {
        valueField.requestFocus();
        valueField.positionCaret(valueField.getLength());
    }

    public String getTitle() {
        return title.getText();
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }
}
