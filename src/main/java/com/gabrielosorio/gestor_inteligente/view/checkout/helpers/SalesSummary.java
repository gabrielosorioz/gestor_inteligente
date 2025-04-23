package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import java.math.BigDecimal;

/**
 * Classe de valor que representa o resumo das estatísticas de vendas
 */
public class SalesSummary {
    private final BigDecimal grossProfit;
    private final BigDecimal cost;
    private final BigDecimal totalSales;
    private final BigDecimal salesAvg;
    private final long salesCount;

    public SalesSummary(BigDecimal grossProfit, BigDecimal cost, BigDecimal totalSales,
                        BigDecimal salesAvg, long salesCount) {
        this.grossProfit = grossProfit;
        this.cost = cost;
        this.totalSales = totalSales;
        this.salesAvg = salesAvg;
        this.salesCount = salesCount;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public BigDecimal getSalesAvg() {
        return salesAvg;
    }

    public long getSalesCount() {
        return salesCount;
    }
}
