package com.gabrielosorio.gestor_inteligente.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class SaleProduct {

    private long id;
    private long saleId;
    private Product product;
    private Sale sale;
    private long quantity;
    private BigDecimal unitPrice;
    private BigDecimal originalSubtotal;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private ObjectProperty<BigDecimal> subTotalProperty;
    private ObjectProperty<BigDecimal> unitPriceProperty;

    public SaleProduct(Product product){
        this.product = product;
        unitPrice = product.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
        discount = new BigDecimal(0.00).setScale(2,RoundingMode.HALF_UP);
        quantity = 1;
        originalSubtotal = calculateOriginalSubtotal();
        subTotal = calculateSubtotal();
        subTotalProperty = new SimpleObjectProperty<>(calculateSubtotal());
        unitPriceProperty = new SimpleObjectProperty<>(unitPrice);
    }

    public SaleProduct(Product product,long quantity){
        this.product = product;
        unitPrice = product.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
        discount = new BigDecimal(0.00).setScale(2,RoundingMode.HALF_UP);
        this.quantity = quantity;
        originalSubtotal = calculateOriginalSubtotal();
        subTotal = calculateSubtotal();
        subTotalProperty = new SimpleObjectProperty<>(calculateSubtotal());
        unitPriceProperty = new SimpleObjectProperty<>(unitPrice);
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
        unitPriceProperty.set(unitPrice);
        subTotal = calculateSubtotal();
        subtotalProperty().set(subTotal);
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
        this.saleId = sale.getId();
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
        subTotal = calculateSubtotal();
        originalSubtotal = calculateOriginalSubtotal();
        subTotalProperty.set(subTotal);
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        unitPriceProperty.set(this.unitPrice);
        subTotal = calculateSubtotal();
        originalSubtotal = calculateOriginalSubtotal();
        subTotalProperty.set(subTotal);
    }

    public BigDecimal getSubTotal() {
        subTotal = calculateSubtotal();
        return subTotal;
    }

    public ObjectProperty<BigDecimal> subtotalProperty(){
        return subTotalProperty;
    }

    public ObjectProperty<BigDecimal> unitPriceProperty(){
        return unitPriceProperty;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getDiscount() {
        return discount.setScale(2,RoundingMode.HALF_UP);
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        subTotal = calculateSubtotal();
        originalSubtotal = calculateOriginalSubtotal();
        subTotalProperty.set(subTotal);
    }

    private BigDecimal calculateSubtotal(){
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .subtract(discount)
                .max(BigDecimal.ZERO)
                .setScale(2,RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOriginalSubtotal(){
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2,RoundingMode.HALF_UP);
    }

    public BigDecimal getOriginalSubtotal() {
        return originalSubtotal;
    }

    public void setOriginalSubtotal(BigDecimal originalSubtotal) {
        this.originalSubtotal = originalSubtotal;
    }

    @Override
    public String toString() {
        return "SaleProduct{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", product=" + product +
                ", sale=" + sale +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subTotal=" + subTotal +
                ", discount=" + discount +
                ", subTotalProperty=" + subTotalProperty +
                '}';
    }
}
