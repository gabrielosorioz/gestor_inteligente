package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;

public class CheckoutMovementType {

    private long id;
    private String name;

    public CheckoutMovementType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public CheckoutMovementType(CheckoutMovementTypeEnum checkoutMovementTypeEnum) {
        this.id = checkoutMovementTypeEnum.getId();
        this.name = checkoutMovementTypeEnum.getName();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
