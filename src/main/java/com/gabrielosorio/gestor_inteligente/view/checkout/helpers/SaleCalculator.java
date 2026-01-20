package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Responsável por todos os cálculos relacionados a vendas.
 * Centraliza lógica de descontos, totais e troco.
 */
public class SaleCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calcula o subtotal original somando todos os produtos.
     */
    public BigDecimal calculateOriginalSubtotal(List<SaleProduct> products) {
        if (products == null || products.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }

        return products.stream()
                .filter(item -> item != null && item.getOriginalSubtotal() != null)
                .map(SaleProduct::getOriginalSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);
    }

    /**
     * Calcula o desconto total dos itens.
     */
    public BigDecimal calculateItemsDiscount(List<SaleProduct> products) {
        if (products == null || products.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }

        return products.stream()
                .filter(item -> item != null && item.getDiscount() != null)
                .map(SaleProduct::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);
    }

    /**
     * Limita o desconto da venda ao máximo permitido.
     * Não pode exceder o valor após descontos dos itens.
     */
    public BigDecimal clampSaleDiscount(BigDecimal saleDiscount,
                                        BigDecimal originalSubtotal,
                                        BigDecimal itemsDiscount) {
        saleDiscount = sanitize(saleDiscount);
        originalSubtotal = sanitize(originalSubtotal);
        itemsDiscount = sanitize(itemsDiscount);

        BigDecimal maxAllowed = originalSubtotal
                .subtract(itemsDiscount)
                .max(BigDecimal.ZERO)
                .setScale(SCALE, ROUNDING);

        if (saleDiscount.compareTo(maxAllowed) > 0) {
            return maxAllowed;
        }

        if (saleDiscount.signum() < 0) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }

        return saleDiscount;
    }

    /**
     * Calcula o desconto total (itens + venda).
     */
    public BigDecimal calculateTotalDiscount(BigDecimal itemsDiscount,
                                             BigDecimal saleDiscount) {
        return sanitize(itemsDiscount)
                .add(sanitize(saleDiscount))
                .setScale(SCALE, ROUNDING);
    }

    /**
     * Calcula o subtotal líquido (original - descontos).
     */
    public BigDecimal calculateNetSubtotal(BigDecimal originalSubtotal,
                                           BigDecimal totalDiscount) {
        return sanitize(originalSubtotal)
                .subtract(sanitize(totalDiscount))
                .max(BigDecimal.ZERO)
                .setScale(SCALE, ROUNDING);
    }

    /**
     * Sincroniza totais da venda com base na UI.
     */
    public void syncSaleTotals(Sale sale,
                               List<SaleProduct> products,
                               BigDecimal uiSaleDiscount) {
        if (sale == null) return;

        // Atualiza produtos e recalcula totais
        sale.setSaleProducts(products);

        // Aplica desconto da venda com limite
        BigDecimal clamped = clampSaleDiscount(
                uiSaleDiscount,
                sale.getOriginalTotalPrice(),
                sale.getItemsDiscount()
        );
        sale.setSaleDiscount(clamped);
    }

    /**
     * Garante que o valor não seja nulo e tenha escala correta.
     */
    private BigDecimal sanitize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        return value.setScale(SCALE, ROUNDING);
    }
}
