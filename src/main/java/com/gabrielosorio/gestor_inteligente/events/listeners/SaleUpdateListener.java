package com.gabrielosorio.gestor_inteligente.events.listeners;

import com.gabrielosorio.gestor_inteligente.events.SaleUpdatedEvent;

public interface SaleUpdateListener {
    void onSaleUpdated(SaleUpdatedEvent saleEvent);
}
