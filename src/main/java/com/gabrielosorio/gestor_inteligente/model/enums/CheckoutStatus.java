package com.gabrielosorio.gestor_inteligente.model.enums;

public enum CheckoutStatus {

    PENDING("Pendente"),
    OPEN("Aberto"),
    CLOSED("Fechado");

    private final String description;

    CheckoutStatus(String description){
        this.description = description;
    }

    public String getName() {
        return description;
    }

    public static CheckoutStatus fromDescription(String description) {
        for (CheckoutStatus status : CheckoutStatus.values()) {
            if (status.description.equalsIgnoreCase(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for description: " + description);
    }

}
