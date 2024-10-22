package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.exception.ProductException;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    public static Product createProduct(Map<String, TextField> fieldMap) {
        var id = fieldMap.get("idField").getText().trim();
        var pCode = id.isEmpty() ? 0 : Integer.parseInt(id);

        return Product.builder()
                .productCode(pCode)
                .barCode(getBarCodeFromField(fieldMap.get("barCodeField")))
                .description(fieldMap.get("descriptionField").getText())
                .costPrice(TextFieldUtils.formatCurrency(fieldMap.get("costPriceField").getText()))
                .sellingPrice(TextFieldUtils.formatCurrency(fieldMap.get("sellingPriceField").getText()))
                .dateCreate(Timestamp.from(Instant.now()))
                .status(Status.ACTIVE)
                .supplier(Optional.empty())
                .category(Optional.empty())
                .quantity(Integer.parseInt(fieldMap.get("quantityField").getText()))
                .build();
    }

    public static Product updateProduct(Product product, Map<String, TextField> fieldMap) {
        if (fieldMap.get("idField").getText().trim().isEmpty()) {
            fieldMap.get("idField").setText(String.valueOf(product.getProductCode()));
            throw new ProductException("Product Code field is Empty.");
        }

        product.setBarCode(getBarCodeFromField(fieldMap.get("barCodeField")));
        product.setDescription(fieldMap.get("descriptionField").getText());
        product.setCostPrice(TextFieldUtils.formatCurrency(fieldMap.get("costPriceField").getText()));
        product.setSellingPrice(TextFieldUtils.formatCurrency(fieldMap.get("sellingPriceField").getText()));
        product.setQuantity(Integer.parseInt(fieldMap.get("quantityField").getText()));
        product.setProductCode(Integer.parseInt(fieldMap.get("idField").getText()));
        product.setSupplier(Optional.empty());
        product.setCategory(Optional.empty());
        return product;
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
                showPopUpMessage();
            }
        });
    }

    private static void showPopUpMessage() {

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
