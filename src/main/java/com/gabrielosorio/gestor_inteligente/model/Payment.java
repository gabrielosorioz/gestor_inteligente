package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import javafx.beans.property.ObjectProperty;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;


public class Payment {

    private long id;
    private PaymentMethod paymentMethod;
    private String description;
    private BigDecimal value;
    private int installments = 1;



    public Payment(PaymentMethod paymentMethod, BigDecimal value) {
        this.paymentMethod = paymentMethod;
        this.value = value;
        this.id = paymentMethod.getId();
        this.description = paymentMethod.getDescription();
    }

    public Payment(PaymentMethod paymentMethod){
        this.paymentMethod = paymentMethod;
        this.id = paymentMethod.getId();
        this.value = new BigDecimal(0.00);
    }

    public Payment(String description){
        this.description = description;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod){
        this.paymentMethod = paymentMethod;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getValue() {
        return value;
    }


    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paymentMethod, value);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getInstallments() {
        return installments;
    }

    public void setInstallments(int installments) {
        if (installments <= 0) {
            throw new IllegalArgumentException("The number of installments must be greater than zero.");
        }
        if (paymentMethod == null) {
            throw new IllegalStateException("Payment method must be defined before setting installments.");
        }
        if (paymentMethod.equals(PaymentMethod.CREDIT0)) {
            this.installments = installments;
        } else {
            throw new UnsupportedOperationException("Installments are only allowed for credit.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id == payment.id && paymentMethod == payment.paymentMethod && Objects.equals(value, payment.value);
    }

}
