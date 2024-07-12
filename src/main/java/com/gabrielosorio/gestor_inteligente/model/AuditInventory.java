package com.gabrielosorio.gestor_inteligente.model;

import java.sql.Timestamp;

public class AuditInventory {

    private Integer id;
    private Inventory inventory;
    private String productName;
    private Timestamp lastUpdate;
    private String movementType;

    public AuditInventory(Integer id, Inventory inventory, String productName, Timestamp lastUpdate, String movementType) {
        this.id = id;
        this.inventory = inventory;
        this.productName = productName;
        this.lastUpdate = lastUpdate;
        this.movementType = movementType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }
}
