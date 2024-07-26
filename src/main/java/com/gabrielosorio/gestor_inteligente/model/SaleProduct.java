package com.gabrielosorio.gestor_inteligente.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SaleProduct {

    private Integer id;
    private Integer saleId;
    private Product product;
    private Sale sale;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private BigDecimal discount;

    public SaleProduct(Integer id, Integer saleId, Product product, Sale sale, int quantity, BigDecimal unitPrice, BigDecimal subTotal, BigDecimal discount) {
        this.id = id;
        this.saleId = saleId;
        this.product = product;
        this.sale = sale;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
        this.discount = discount;
    }

    public SaleProduct(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        unitPrice = product.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .subtract(discount)
                .setScale(2,RoundingMode.HALF_UP);
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getDiscount() {
        return discount.setScale(2,RoundingMode.HALF_UP);
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}
