package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;

import java.math.BigDecimal;

public class SalePayment {

    private long id;
    private long saleId;
    private long paymentId;
    private BigDecimal amount;
    private Payment payment;
    private Sale sale;
    private int installments = 1;

    public SalePayment(Payment payment, Sale sale) {
        this.payment = payment;
        this.sale = sale;
        this.paymentId = payment.getId();
        this.saleId = sale.getId();
        amount = payment.getValue();
        if(payment.getPaymentMethod().equals(PaymentMethod.CREDIT0)){
            this.installments = payment.getInstallments();
        }
    }

    public int getInstallments() {
        return installments;
    }

    public void setInstallments(int installments) {
        this.installments = installments;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

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

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }
}
