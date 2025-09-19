package com.gabrielosorio.gestor_inteligente.events;

import com.gabrielosorio.gestor_inteligente.model.Product;

public class FindProductEvent {

    private final Product selectedProduct;

    public FindProductEvent(Product selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }

}
