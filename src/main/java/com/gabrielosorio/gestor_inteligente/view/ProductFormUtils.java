package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.exception.ProductFormException;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import javafx.scene.control.TextField;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ProductFormUtils {

    private static final Logger log = Logger.getLogger(ProductFormUtils.class.getName());

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
        String description = fieldMap.get("descriptionField").getText().trim();
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
        String description = fieldMap.get("descriptionField").getText().trim();
        BigDecimal costPrice = TextFieldUtils.formatCurrency(fieldMap.get("costPriceField").getText());
        BigDecimal sellingPrice = TextFieldUtils.formatCurrency(fieldMap.get("sellingPriceField").getText());
        String quantityText = fieldMap.get("quantityField").getText();
        String productCodeTxt = fieldMap.get("idField").getText();


        int quantity;
        long productCode;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            throw new ProductFormException("A quantidade deve ser um número válido.");
        }

        try {
            productCode = Integer.parseInt(productCodeTxt);
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
        String description = fieldMap.get("descriptionField").getText().trim();
        BigDecimal costPrice = TextFieldUtils.formatCurrency(fieldMap.get("costPriceField").getText());
        BigDecimal sellingPrice = TextFieldUtils.formatCurrency(fieldMap.get("sellingPriceField").getText());

        if(description.isEmpty()){
            fieldMap.get("descriptionField").clear();
            fieldMap.get("descriptionField").requestFocus();
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

}
