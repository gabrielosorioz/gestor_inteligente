package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import javafx.beans.property.ObjectProperty;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;


public class Payment {

    private long id;
    private PaymentMethod paymentMethod;
    private BigDecimal value;


    public Payment(PaymentMethod paymentMethod, BigDecimal value) {
        this.paymentMethod = paymentMethod;
        this.value = value;
        this.id = paymentMethod.getId();
    }

    public Payment(PaymentMethod paymentMethod){
        this.paymentMethod = paymentMethod;
        this.id = paymentMethod.getId();
        this.value = new BigDecimal(0.00);
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

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id == payment.id && paymentMethod == payment.paymentMethod && Objects.equals(value, payment.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paymentMethod, value);
    }
}
