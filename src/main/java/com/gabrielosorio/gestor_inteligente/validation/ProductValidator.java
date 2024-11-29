package com.gabrielosorio.gestor_inteligente.validation;

import com.gabrielosorio.gestor_inteligente.exception.InvalidProductException;
import com.gabrielosorio.gestor_inteligente.model.Product;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductValidator {

    public static void validate(Product product) {
        // Validate product object itself
        if (product == null) {
            throw new InvalidProductException("Product cannot be null.");
        }

        try {

            validateProductIdOrBarCode(product);
            validateCostPrice(product);
            validateSellingPrice(product);
            validatePrices(product.getCostPrice(),product.getSellingPrice());
            validateCreationDate(product);
            validateProfitMargin(product);
            validateMarkupPercentage(product);
            validateUniqueCodeAndBarCode(product);
            validateUniqueIdAndBarCode(product);

        } catch (InvalidProductException e){
            throw new InvalidProductException("Validation failed for product: " + product + ". " + e.getMessage());
        }

    }



    public static void validateProductIdOrBarCode(Product product){
        if(product.getProductCode() < 0  && product.getBarCode() == null){

            throw new InvalidProductException("Product must have either a Product Id or a Barcode. ");
        }
    }

    private static void validateCostPrice(Product product) {
        if (product.getCostPrice() == null) {
            throw new InvalidProductException("Cost price cannot be null. ");
        }
    }


    private static void validateSellingPrice(Product product) {
        if (product.getSellingPrice() == null) {
            throw new InvalidProductException("Selling price cannot be null.");
        } else if (product.getCostPrice() != null && product.getCostPrice().compareTo(product.getSellingPrice()) > 0) {
            throw new InvalidProductException("Cost price cannot be greater than selling price.");
        }
    }

    private static void validateCreationDate(Product product) {
        if (product.getDateCreate() == null) {
            throw new InvalidProductException("Creation Date cannot be null.");
        }
    }

    private static void validateProfitMargin(Product product) {
        if (product.getProfitPercent() < 0) {
            throw new InvalidProductException("Profit margin cannot be negative.");
        }
    }

    private static void validateMarkupPercentage(Product product) {
        if (product.getMarkupPercent() < 0) {
            throw new InvalidProductException("Markup percentage cannot be negative.");
        }
    }

    public static void validatePrices(BigDecimal costPrice, BigDecimal sellingPrice) {
        if (sellingPrice == null || costPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0 || costPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductException("Selling price and cost price must be greater than zero. ");
        }
        if (costPrice.compareTo(sellingPrice) > 0) {
            throw new InvalidProductException("Cost price cannot be greater than selling price. " + "costPrice=" + costPrice + " sellingPrice=" + sellingPrice);
        }
    }

    private static void validateUniqueCodeAndBarCode(Product product) {
        Optional<String> barCode = product.getBarCode();
        if (product.getProductCode() >= 0 && barCode.isPresent() && barCode.get().equals(String.valueOf(product.getProductCode()))) {
            throw new InvalidProductException("Product code and barcode cannot be the same.");
        }
    }

    private static void validateUniqueIdAndBarCode(Product product) {
        Optional<String> barCode = product.getBarCode();
        if (product.getId() >= 0 && barCode.isPresent() && barCode.get().equals(String.valueOf(product.getId()))) {
            throw new InvalidProductException("Product code and ID cannot be the same.");
        }
    }

    public static boolean pricesGreaterThanZero(BigDecimal costPrice, BigDecimal sellingPrice){
        if (sellingPrice == null || costPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0 || costPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return true;
    }

    public static boolean costPriceLowerThanSellingPrice(BigDecimal costPrice, BigDecimal sellingPrice){
        if(costPrice.compareTo(sellingPrice) > 0){
            return false;
        }
        return true;
    }

}
