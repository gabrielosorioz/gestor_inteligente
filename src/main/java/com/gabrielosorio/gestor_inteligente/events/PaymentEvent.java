package com.gabrielosorio.gestor_inteligente.events;
import com.gabrielosorio.gestor_inteligente.model.Sale;

public class PaymentEvent {
    private final Sale sale;

    public PaymentEvent(Sale sale) {
        this.sale = sale;
    }

    public Sale getSale() {
        return sale;
    }

}

