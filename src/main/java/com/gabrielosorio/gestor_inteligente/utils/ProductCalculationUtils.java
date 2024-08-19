package com.gabrielosorio.gestor_inteligente.utils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductCalculationUtils {

    public static double calculateProfitMargin(BigDecimal costPrice, BigDecimal sellingPrice){
       ProductValidator.validatePrices(costPrice,sellingPrice);

        BigDecimal grossMargin = sellingPrice.subtract(costPrice);
        BigDecimal percentProfitMargin = grossMargin.divide(sellingPrice, 4, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100));
        return percentProfitMargin.doubleValue();

    }

    public static double calculateMarkup(BigDecimal costPrice, BigDecimal sellingPrice){
        ProductValidator.validatePrices(costPrice,sellingPrice);

        BigDecimal grossMargin = sellingPrice.subtract(costPrice);
        BigDecimal markup = grossMargin.divide(costPrice, 4, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100));
        return markup.doubleValue();
    }



}
