package com.gabrielosorio.gestor_inteligente.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashFlow {

    private Integer id;
    private Sale sale;
    private BigDecimal pettyCash;
    private BigDecimal exitCash;
    private BigDecimal entryCash;
    private String movementType;
    private LocalDateTime dateTime;

    public CashFlow(Integer id, Sale sale, BigDecimal pettyCash, BigDecimal exitCash, BigDecimal entryCash, String movementType, LocalDateTime dateTime) {
        this.id = id;
        this.sale = sale;
        this.pettyCash = pettyCash;
        this.exitCash = exitCash;
        this.entryCash = entryCash;
        this.movementType = movementType;
        this.dateTime = dateTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public BigDecimal getPettyCash() {
        return pettyCash;
    }

    public void setPettyCash(BigDecimal pettyCash) {
        this.pettyCash = pettyCash;
    }

    public BigDecimal getExitCash() {
        return exitCash;
    }

    public void setExitCash(BigDecimal exitCash) {
        this.exitCash = exitCash;
    }

    public BigDecimal getEntryCash() {
        return entryCash;
    }

    public void setEntryCash(BigDecimal entryCash) {
        this.entryCash = entryCash;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
