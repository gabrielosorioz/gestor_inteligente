package com.gabrielosorio.gestor_inteligente.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SaleProduct {

    private long id;
    private long saleId;
    private Product product;
    private Sale sale;
    private long quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private BigDecimal discount;


    public SaleProduct(Product product){
       this.product = product;
       unitPrice = product.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
       discount = new BigDecimal(0.00).setScale(2,RoundingMode.HALF_UP);
       quantity = 1;
    }

    public SaleProduct(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSaleId() {
        return saleId;
    }

    public void setSaleId(long saleId) {
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

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
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
