package com.gabrielosorio.gestor_inteligente.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class Sale {

    private Integer id;
    private Integer saleId;
    private Product product;
    private Timestamp dataSale;
    private Timestamp dataCancel;
    private List<SaleItem> items;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private List<Payment> paymentMethods;
    private Cliente cliente;
    private String status;

    public Sale(Integer id, Integer saleId, Product product, Timestamp dataSale, Timestamp dataCancel, List<SaleItem> items, BigDecimal amount, BigDecimal totalAmount, BigDecimal discount, List<Payment> paymentMethods, Cliente cliente, String status) {
        this.id = id;
        this.saleId = saleId;
        this.product = product;
        this.dataSale = dataSale;
        this.dataCancel = dataCancel;
        this.items = items;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.paymentMethods = paymentMethods;
        this.cliente = cliente;
        this.status = status;
    }

    public Sale(){}

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

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public List<Payment> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethod(List<Payment> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
