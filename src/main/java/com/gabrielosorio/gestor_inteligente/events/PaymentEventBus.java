package com.gabrielosorio.gestor_inteligente.events;
import com.gabrielosorio.gestor_inteligente.events.listeners.PaymentListener;

import java.util.ArrayList;
import java.util.List;

public class PaymentEventBus {
    private static PaymentEventBus instance;
    private final List<PaymentListener> listeners = new ArrayList<>();

    private PaymentEventBus() {}

    public static PaymentEventBus getInstance() {
        if(instance == null) {
            instance = new PaymentEventBus();
        }
        return instance;
    }

    public void register(PaymentListener listener) {
        listeners.add(listener);
    }

    public void unregister(PaymentListener listener) {
        listeners.remove(listener);
    }

    public void publish(PaymentEvent event) {
        for(PaymentListener listener : listeners) {
            listener.onPaymentFinalized(event);
        }
    }
}


