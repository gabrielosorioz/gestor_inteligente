package com.gabrielosorio.gestor_inteligente.validation;

import com.gabrielosorio.gestor_inteligente.exception.SaleValidationException;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;

import java.math.BigDecimal;

public class SaleValidator {

    public static void validate(Sale sale){
        validateDiscounts(sale);
        validateProducts(sale);
        validateSaleAmounts(sale);
    }

    private static void validateSaleAmounts(Sale sale){
        if(sale.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new SaleValidationException("The total sale sale should be positive.");
        }

        if(sale.getTotalAmountPaid().compareTo(sale.getTotalPrice()) < 0){
            throw new SaleValidationException("The total paid cannot be less than the total price.");
        }
    }

    private static void validateProducts(Sale sale){
        if(sale.getItems().isEmpty()){
            throw new SaleValidationException("The sale must have at least one product.");
        }

        for (SaleProduct saleProduct : sale.getItems()) {
            if (saleProduct.getQuantity() <= 0) {
                throw new SaleValidationException("The quantity of products must be positive.");
            }
            if (saleProduct.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SaleValidationException("The unit price of product must be positive.");
            }
        }

    }

    private static void validateDiscounts(Sale sale){
        if(sale.getTotalDiscount().compareTo(BigDecimal.ZERO) < 0){
            throw new SaleValidationException("The total discount cannot be negative.");
        }

        if(sale.getTotalDiscount().compareTo(sale.getTotalPrice()) > 0){
            throw new SaleValidationException("The discount cannot be greater than the sale price. ");
        }

    }


}
