package com.gabrielosorio.gestor_inteligente.events;

public class ProductFormToggleEvent {

    private final boolean isFormVisible;

    public ProductFormToggleEvent(boolean isFormVisible) {
        this.isFormVisible = isFormVisible;
    }

    public boolean isFormVisible() {
        return isFormVisible;
    }
}
