package com.gabrielosorio.gestor_inteligente.events;
import com.gabrielosorio.gestor_inteligente.model.Product;
import java.util.Optional;

public class ProductFormSaveEvent {
    private final Optional<Product> product;

    public ProductFormSaveEvent(Optional<Product> product) {
        this.product = product;
    }

    public Optional<Product> getProduct() {
        return product;
    }
}
