package com.gabrielosorio.gestor_inteligente.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TextFieldUtils {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final Set<TextField> textFieldSet = new HashSet<>();

    /**
     * Adds a listener to a TextField to format its text as currency.
     * The listener will only be added if it is not already present.
     *
     * @param field The TextField to which the listener will be added.
     */
    public static void addPriceListener(TextField field) {
        // If the field is empty or blank, set it to "0,00"
        if (field.getText().isBlank() || field.getText().isEmpty()) {
            field.setText("0,00");
        }

        // Check if the listener has already been added
        if(!textFieldSet.contains(field)){
            field.textProperty().addListener((observableValue, oldValue, newValue) -> {
                String formattedText = formatText(newValue);
                if (!newValue.equals(formattedText)) {
                    Platform.runLater(() -> {
                        field.setText(formattedText);
                        field.positionCaret(field.getText().length());
                    });
                }
            });
            System.out.println("Listener added");
            textFieldSet.add(field);
        }
    }

    /**
     * Formats a text as Brazilian currency.
     *
     * @param text The text to be formatted.
     * @return The text formatted as currency.
     */
    private static String formatText(String text) {
        // Remove all non-numeric characters
        String plainText = text.replaceAll("[^0-9]", "");

        // Ensure the text has at least 3 digits
        if (plainText.length() < 3) {
            plainText = String.format("%03d", Integer.parseInt(plainText.isEmpty() ? "0" : plainText));
        }

        // Insert a dot before the last two digits
        StringBuilder builder = new StringBuilder(plainText);
        builder.insert(plainText.length() - 2, ".");

        try {
            // Format the text as Brazilian currency
            return CURRENCY_FORMAT.format(Double.parseDouble(builder.toString())).substring(3);
        } catch (NumberFormatException e) {
            return "0,00";
        }
    }
}
