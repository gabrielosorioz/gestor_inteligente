package com.gabrielosorio.gestor_inteligente.model;

import java.math.BigDecimal;

public class SaleItem {

    private Integer id;
    private Integer saleId;
    private Product product;
    private Sale sale;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;

    public SaleItem(Integer id, Integer saleId, Product product, Sale sale, int quantity, BigDecimal unitPrice, BigDecimal subTotal) {
        this.id = id;
        this.saleId = saleId;
        this.product = product;
        this.sale = sale;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }

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
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }
}
