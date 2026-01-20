package com.gabrielosorio.gestor_inteligente.events;

import com.gabrielosorio.gestor_inteligente.model.Sale;

public class SaleUpdatedEvent {
    private final Sale sale;

    public SaleUpdatedEvent(Sale sale) {
        this.sale = sale;
    }

    public Sale getSale() {
        return sale;
    }
}


