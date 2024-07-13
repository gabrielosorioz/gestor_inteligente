package com.gabrielosorio.gestor_inteligente.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import java.text.NumberFormat;
import java.util.Locale;

public class TextFieldUtils {

    private final static NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));

    public static void addPriceListener(TextField field) {
        field.setText("0,00");
        field.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.equals(formatText(newValue))) {
                Platform.runLater(() -> {
                    field.setText(formatText(newValue));
                    field.positionCaret(field.getText().length());
                });
            }
        });
    }

    private static String formatText(String text) {
        String plainText = text.replaceAll("[^0-9]", "");
        if (plainText.length() < 3) {
            plainText = String.format("%03d", Integer.parseInt(plainText.isEmpty() ? "0" : plainText));
        }

        StringBuilder builder = new StringBuilder(plainText);
        builder.insert(plainText.length() - 2, ".");

        try {
            return format.format(Double.parseDouble(builder.toString())).substring(3);
        } catch (NumberFormatException e) {
            return "0,00";
        }
    }
}

