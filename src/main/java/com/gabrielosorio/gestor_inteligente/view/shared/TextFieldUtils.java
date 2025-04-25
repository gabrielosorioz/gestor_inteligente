package com.gabrielosorio.gestor_inteligente.view.shared;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

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
    public static String formatText(String text) {
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

    public static BigDecimal formatCurrency(String value) {
        value = normalizeCurrencyString(value);
        DecimalFormat df = createDecimalFormat();
        return parseToBigDecimal(value, df);
    }

    // Normalizes the currency string to a format that DecimalFormat can parse
    private static String normalizeCurrencyString(String value) {
        value = value.trim();
        boolean hasComma = value.contains(",");
        boolean hasDot = value.contains(".");

        if (hasComma && hasDot) {
            if (value.indexOf(',') > value.indexOf('.')) {
                // Comma is the decimal separator, dot is the thousand separator
                value = value.replace(".", "");
                value = value.replace(",", ".");
            } else {
                // Dot is the decimal separator
                value = value.replace(",", "");
            }
        } else if (hasComma) {
            // Only comma present, assume it's the decimal separator
            value = value.replace(",", ".");
        } else if (hasDot) {
            // Only dot present, assume it's the decimal separator
            // No changes are needed as the format is already suitable
        }

        return value;
    }

    // Creates a DecimalFormat instance with appropriate symbols
    private static DecimalFormat createDecimalFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.'); // Set dot as the decimal separator
        symbols.setGroupingSeparator(','); // Set comma as the thousand separator

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        df.setParseBigDecimal(true);
        return df;
    }

    // Parses the normalized string to BigDecimal using the provided DecimalFormat
    private static BigDecimal parseToBigDecimal(String value, DecimalFormat df) {
        BigDecimal bigDecimal = null;
        try {
            bigDecimal = (BigDecimal) df.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return bigDecimal;
    }

    public static void setUpperCaseTextFormatter(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            change.setText(change.getText().toUpperCase());
            return change;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);
    }

    public static void lastPositionCursor(TextField textField){
        int lastPosition = textField.getLength();
        textField.positionCaret(lastPosition);
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Arrays.stream(input.split("\\s+")) // Divide por espaços
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()) // Capitaliza a 1ª letra
                .collect(Collectors.joining(" ")); // Junta de volta
    }



}
