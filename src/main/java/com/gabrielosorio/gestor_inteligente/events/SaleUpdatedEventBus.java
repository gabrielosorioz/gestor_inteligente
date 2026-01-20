package com.gabrielosorio.gestor_inteligente.events;

import com.gabrielosorio.gestor_inteligente.events.listeners.SaleUpdateListener;

import java.util.ArrayList;
import java.util.List;

public class SaleUpdatedEventBus {
    private static SaleUpdatedEventBus instance;
    private final List<SaleUpdateListener> listeners = new ArrayList<>();

    private SaleUpdatedEventBus(){}

    public static SaleUpdatedEventBus getInstance() {
        if(instance == null){
            instance = new SaleUpdatedEventBus();
        }
        return instance;
    }

    public void register(SaleUpdateListener saleUpdateListener){
        listeners.add(saleUpdateListener);
    }

    public void unregister(SaleUpdateListener saleUpdateListener){
        listeners.remove(saleUpdateListener);
    }

    public void publish(SaleUpdatedEvent saleUpdatedEvent){
        for(SaleUpdateListener listener : listeners){
            listener.onSaleUpdated(saleUpdatedEvent);
        }
    }

}
