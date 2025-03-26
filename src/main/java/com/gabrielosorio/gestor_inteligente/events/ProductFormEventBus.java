package com.gabrielosorio.gestor_inteligente.events;

import com.gabrielosorio.gestor_inteligente.events.listeners.ProductFormListener;

import java.util.ArrayList;
import java.util.List;

public class ProductFormEventBus {
    private static ProductFormEventBus instance;
    private final List<ProductFormListener> listeners = new ArrayList<>();

    private ProductFormEventBus() {}

    public static ProductFormEventBus getInstance() {
        if (instance == null) {
            instance = new ProductFormEventBus();
        }
        return instance;
    }

    public void register(ProductFormListener listener) {
        listeners.add(listener);
    }

    public void unregister(ProductFormListener listener) {
        listeners.remove(listener);
    }

    public void publish(ProductCodeEditAttemptEvent event) {
        for (ProductFormListener listener : listeners) {
            listener.onProductCodeEditAttempt(event);
        }
    }

    public void publish(ProductFormSaveEvent event) {
        for (ProductFormListener listener : listeners) {
            listener.onSave(event);
        }
    }

    public void publish(ProductFormCancelEvent event) {
        for (ProductFormListener listener : listeners) {
            listener.onCancel(event);
        }
    }

    public void publish(ProductFormShortcutEvent event) {
        for (ProductFormListener listener : listeners) {
            listener.onHandleShortcutEvent(event);
        }
    }

}
