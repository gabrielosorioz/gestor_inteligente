package com.gabrielosorio.gestor_inteligente.events;

import com.gabrielosorio.gestor_inteligente.events.listeners.FindProductListener;

import java.util.ArrayList;
import java.util.List;

public class FindProductEventBus {
    private static FindProductEventBus instance;
    private final List<FindProductListener> listeners = new ArrayList<>();

    public static FindProductEventBus getInstance() {
        if(instance == null) {
            instance = new FindProductEventBus();
        }
        return instance;
    }

    public void register(FindProductListener listener) {
        listeners.add(listener);
    }

    public void unregister(FindProductListener listener) {
        listeners.remove(listener);
    }

    public void publish(FindProductEvent event) {
        for(FindProductListener listener : listeners) {
            listener.onSelectProduct(event);
        }
    }

}
