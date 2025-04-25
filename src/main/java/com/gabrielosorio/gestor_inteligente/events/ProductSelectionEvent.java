package com.gabrielosorio.gestor_inteligente.events;

import com.gabrielosorio.gestor_inteligente.model.Product;

public class ProductSelectionEvent {
    private final Product product;

    public Product getProduct() {
        return product;
    }

    public ProductSelectionEvent(Product product) {
        this.product = product;
    }
}
