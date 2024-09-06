package com.gabrielosorio.gestor_inteligente.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Sale {

    private long id;
    private long saleId;
    private Timestamp dataSale;
    private Timestamp dataCancel;
    private List<SaleProduct> items;
    private BigDecimal totalPrice;
    private BigDecimal totalDiscount;
    private List<Payment> paymentMethods;
    private String status;

    public Sale (List<SaleProduct> items){

        if(items.isEmpty() || items == null){
            throw new IllegalArgumentException("Error at initializing Sale constructor:Product items for sale is null.");
        }

        this.items = items;
        dataSale = Timestamp.from(Instant.now());
        totalDiscount = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);
        totalPrice = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);
        items.forEach(item -> {
            totalPrice = totalPrice.add(item.getSubTotal());
            totalDiscount =  totalDiscount.add(item.getDiscount());
        });

        paymentMethods = new ArrayList<Payment>();

    }

    public Sale(long id, long saleId, Timestamp dataSale, Timestamp dataCancel, List<SaleProduct> items, BigDecimal totalPrice, BigDecimal totalDiscount, List<Payment> paymentMethods,String status) {
        this.id = id;
        this.saleId = saleId;
        this.dataSale = dataSale;
        this.dataCancel = dataCancel;
        this.items = items;
        this.totalPrice = totalPrice;
        this.totalDiscount = totalDiscount;
        this.paymentMethods = paymentMethods;
        this.status = status;
    }

    public Sale(){}

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

    public Timestamp getDataSale() {
        return dataSale;
    }

    public void setDataSale(Timestamp dataSale) {
        this.dataSale = dataSale;
    }

    public Timestamp getDataCancel() {
        return dataCancel;
    }

    public void setDataCancel(Timestamp dataCancel) {
        this.dataCancel = dataCancel;
    }

    public List<SaleProduct> getItems() {
        return items;
    }

    public void setItems(List<SaleProduct> items) {
        this.items = items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public void setPaymentMethods(List<Payment> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            throw new IllegalArgumentException("Error to set payment: Sale items are null or empty");
        }
        this.paymentMethods = paymentMethods;
    }

    public List<Payment> getPaymentMethods() {
        return paymentMethods;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
