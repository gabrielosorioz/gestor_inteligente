package com.gabrielosorio.gestor_inteligente.model;

import java.sql.Timestamp;

public class AuditInventory {

    private long id;
    private Stock stock;
    private String productName;
    private Timestamp lastUpdate;
    private String movementType;

    public AuditInventory(long id, Stock stock, String productName, Timestamp lastUpdate, String movementType) {
        this.id = id;
        this.stock = stock;
        this.productName = productName;
        this.lastUpdate = lastUpdate;
        this.movementType = movementType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Stock getInventory() {
        return stock;
    }

    public void setInventory(Stock stock) {
        this.stock = stock;
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
