package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;


public class Payment {

    private long id;
    private PaymentMethod paymentMethod;
    private double value;

    public Payment(PaymentMethod paymentMethod, double value) {
        this.paymentMethod = paymentMethod;
        this.value = value;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod){
        this.paymentMethod = paymentMethod;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public double getValue() {
        return value;
    }

}
