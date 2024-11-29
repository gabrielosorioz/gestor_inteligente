package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.exception.InvalidProductException;
import com.gabrielosorio.gestor_inteligente.exception.ProductException;
import com.gabrielosorio.gestor_inteligente.exception.ProductFormException;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class ProductFormUtils {

    public static void populateProductFields(Optional<Product> product, Map<String, TextField> fieldMap) {
        var codeField = fieldMap.get("idField");
        product.ifPresentOrElse(prod -> {

            setTextField(fieldMap.get("idField"), String.valueOf(prod.getProductCode()));
            setTextField(fieldMap.get("barCodeField"), prod.getBarCode().orElse(""));
            setTextField(fieldMap.get("descriptionField"), prod.getDescription());
            setTextField(fieldMap.get("costPriceField"), prod.getCostPrice().toPlainString());
            setTextField(fieldMap.get("sellingPriceField"), prod.getSellingPrice().toPlainString());
            setTextField(fieldMap.get("quantityField"), String.valueOf(prod.getQuantity()));
            setTextField(fieldMap.get("markupField"), String.valueOf(prod.getMarkupPercent()));
            setTextField(fieldMap.get("categoryField"), prod.getCategory().map(Category::getDescription).orElse(""));
            setTextField(fieldMap.get("supplierField"), prod.getSupplier().map(Supplier::getName).orElse(""));

        }, () -> {
            clearFields(fieldMap);
            codeField.setPromptText("Novo Código.");
        });
    }

    private static void setTextField(TextField field, String val) {
        field.setText(val != null ? val : "");
    }

    private static void clearFields(Map<String, TextField> fieldMap) {
        fieldMap.values().forEach(field -> field.setText(""));
    }

    public static Product createProduct(Map<String, TextField> fieldMap) throws ProductFormException {
        validateProductFields(fieldMap);
        var id = fieldMap.get("idField").getText().trim();
        var pCode = id.isEmpty() ? 0 : Integer.parseInt(id);
        String description = fieldMap.get("descriptionField").getText();
        Optional<String> barCode = getBarCodeFromField(fieldMap.get("barCodeField"));
        BigDecimal costPrice = TextFieldUtils.formatCurrency(fieldMap.get("costPriceField").getText());
        BigDecimal sellingPrice = TextFieldUtils.formatCurrency(fieldMap.get("sellingPriceField").getText());
        var quantityText =  fieldMap.get("quantityField").getText().trim();
        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            throw new ProductFormException("A quantidade deve ser um número válido.");
        }

            return Product.builder()
                    .productCode(pCode)
                    .barCode(barCode)
                    .description(description)
                    .costPrice(costPrice)
                    .sellingPrice(sellingPrice)
                    .dateCreate(Timestamp.from(Instant.now()))
                    .status(Status.ACTIVE)
                    .supplier(Optional.empty())
                    .category(Optional.empty())
                    .quantity(quantity)
                    .build();

    }

    public static Product updateProduct(Product product, Map<String, TextField> fieldMap) throws ProductFormException {
        validateProductFields(product,fieldMap);
        Optional<String> barCode = getBarCodeFromField(fieldMap.get("barCodeField"));
        String description = fieldMap.get("descriptionField").getText();
        BigDecimal costPrice = TextFieldUtils.formatCurrency(fieldMap.get("costPriceField").getText());
        BigDecimal sellingPrice = TextFieldUtils.formatCurrency(fieldMap.get("sellingPriceField").getText());
        String quantityText = fieldMap.get("quantityField").getText();


        int quantity;
        long productCode;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            throw new ProductFormException("A quantidade deve ser um número válido.");
        }

        try {
            productCode = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            throw new ProductFormException("O ID deve ser um número válido.");
        }

        product.setBarCode(barCode);
        product.setDescription(description);
        product.setCostPrice(costPrice);
        product.setSellingPrice(sellingPrice);
        product.setQuantity(quantity);
        product.setProductCode(productCode);
        product.setSupplier(Optional.empty());
        product.setCategory(Optional.empty());
        return product;
    }

    private static void validateProductFields(Map<String, TextField> fieldMap) throws ProductFormException {
        String description = fieldMap.get("descriptionField").getText();
        BigDecimal costPrice = TextFieldUtils.formatCurrency(fieldMap.get("costPriceField").getText());
        BigDecimal sellingPrice = TextFieldUtils.formatCurrency(fieldMap.get("sellingPriceField").getText());

        if(description.isEmpty()){
            throw new ProductFormException("O campo de descrição do produto está vazio.");
        }

        if (!ProductValidator.costPriceLowerThanSellingPrice(costPrice,sellingPrice)) {
            throw new ProductFormException("O preço de custo deve ser menor do que o preço de venda.");
        }

        if(!ProductValidator.pricesGreaterThanZero(costPrice,sellingPrice)){
            throw new ProductFormException("Preço de custo e preço de venda devem ser maiores que zero.");
        }
    }

    private static void validateProductFields(Product product,Map<String, TextField> fieldMap) throws ProductFormException {
        validateProductFields(fieldMap);
        String pCode = fieldMap.get("idField").getText().trim();

        if (pCode.isEmpty()) {
            fieldMap.get("idField").setText(String.valueOf(product.getProductCode()));
            throw new ProductFormException("O campo ID do produto está vazio.");
        }

    }

    private static Optional<String> getBarCodeFromField(TextField barCodeField) {
        String barCodeText = barCodeField.getText().trim();
        return barCodeText.isEmpty() ? Optional.empty() : Optional.of(barCodeText);
    }

    private static void setLockFieldStyle(TextField field) {
        field.setStyle(
                "-fx-border-color: #e0e0e0;" +
                        "-fx-text-fill: #7f7f7f;"
//                "-fx-cursor: pointer;"
        );
        field.setOnMouseEntered(mouseEvent -> {
            if (!field.isEditable()) {
                field.setStyle(
                        "-fx-cursor: hand;" +
                                "-fx-border-color: #e0e0e0;"
                );
            }
        });
    }

    private static void lockField(TextField field) {
        field.setEditable(false);
        setLockFieldStyle(field);
        setupActionField(field);
    }

    private static void unlockField(TextField field) {
        field.setEditable(true);
        field.setStyle("");
    }

    private static void setupActionField(TextField field) {
        field.setOnMouseClicked(click -> {
            if(!field.isEditable()){
                unlockField(field);
                showLockFieldMessage();
            }
        });
    }

    private static void showLockFieldMessage() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/AlertMessage.fxml"));
            Stage popUpStage = new Stage();
            Scene scene = new Scene(fxmlLoader.load());
            scene.setFill(Color.TRANSPARENT);
            popUpStage.initModality(Modality.APPLICATION_MODAL);
            popUpStage.initStyle(StageStyle.TRANSPARENT);
            popUpStage.setScene(scene);
            popUpStage.showAndWait();


        } catch (Exception e) {
//            log.severe("ERROR at load payment view: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
