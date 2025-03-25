package com.gabrielosorio.gestor_inteligente.events;
import com.gabrielosorio.gestor_inteligente.events.listeners.ProductManagerListener;

import java.util.ArrayList;
import java.util.List;

public class ProductManagerEventBus {
    private static ProductManagerEventBus instance;
    private final List<ProductManagerListener> listeners = new ArrayList<>();

    private ProductManagerEventBus(){};

    public static ProductManagerEventBus getInstance() {
        if(instance == null) {
            instance = new ProductManagerEventBus();
        }
        return instance;
    }

    public void register(ProductManagerListener listener) {listeners.add(listener);}

    public void unregister(ProductManagerListener listener) {listeners.remove(listener);}

    public void publish(ProductSelectionEvent event){
        for(ProductManagerListener listener: listeners){
            listener.onSelectProduct(event);
        }
    }

    public void publish(ProductFormToggleEvent event){
        for(ProductManagerListener listener: listeners){
            listener.onToggleProductForm(event);
        }
    }

    public void publish(ProductAddEvent event){
        for(ProductManagerListener listener: listeners){
            listener.onAddNewProduct(event);
        }
    }






}
