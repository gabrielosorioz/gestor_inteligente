package com.gabrielosorio.gestor_inteligente.model.enums;

public enum CheckoutMovementTypeEnum {
    ENTRADA(1, "ENTRADA"),
    SAIDA(2, "SAÍDA"),
    VENDA(3, "VENDA"),
    FUNDO_DE_CAIXA(4, "FUNDO DE CAIXA"),
    AJUSTE_POSITIVO(5, "AJUSTE POSITIVO"),
    AJUSTE_NEGATIVO(6, "AJUSTE NEGATIVO");

    private final long id;
    private final String name;

    CheckoutMovementTypeEnum(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static CheckoutMovementTypeEnum getById(long id) {
        for (CheckoutMovementTypeEnum type : CheckoutMovementTypeEnum.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid checkout movement type ID: " + id);
    }

    public static CheckoutMovementTypeEnum getByName(String name) {
        for (CheckoutMovementTypeEnum type : CheckoutMovementTypeEnum.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid checkout movement type name: " + name);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}