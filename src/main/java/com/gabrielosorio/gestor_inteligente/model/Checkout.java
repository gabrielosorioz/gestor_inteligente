package com.gabrielosorio.gestor_inteligente.model;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.TypeCheckoutMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Checkout {

    private long id;
    private CheckoutStatus status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal initialCash=BigDecimal.ZERO;;
    private BigDecimal totalEntry=BigDecimal.ZERO;;
    private BigDecimal totalExit=BigDecimal.ZERO;;
    private BigDecimal closingBalance=BigDecimal.ZERO;;
    private User openedBy;
    private User closedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CheckoutMovement> movements = new ArrayList<>();
    private List<Sale> sales;
    private BigDecimal totalAmountSales = BigDecimal.ZERO;

    public Checkout(long id, CheckoutStatus status, LocalDateTime openedAt, LocalDateTime closedAt, BigDecimal initialCash, BigDecimal totalEntry, BigDecimal totalExit, BigDecimal closingBalance, User openedBy, User closedBy, LocalDateTime createdAt, LocalDateTime updatedAt, List<CheckoutMovement> movements, List<Sale> sales, BigDecimal totalAmountSales) {
        this.id = id;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.initialCash = initialCash;
        this.totalEntry = totalEntry;
        this.totalExit = totalExit;
        this.closingBalance = closingBalance;
        this.openedBy = openedBy;
        this.closedBy = closedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.movements = movements;
        this.sales = sales;
        this.totalAmountSales = totalAmountSales;
    }

    public Checkout(){}

    public Checkout(long id, CheckoutStatus status, LocalDateTime openedAt, LocalDateTime closedAt, BigDecimal initialCash, BigDecimal totalEntry, BigDecimal totalExit, BigDecimal closingBalance, User openedBy, User closedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.initialCash = initialCash;
        this.totalEntry = totalEntry;
        this.totalExit = totalExit;
        this.closingBalance = closingBalance;
        this.openedBy = openedBy;
        this.closedBy = closedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addMovement(CheckoutMovement movement) {
        this.movements.add(movement);
        if (movement.getType() == TypeCheckoutMovement.ENTRADA) {
            this.totalEntry = this.totalEntry.add(movement.getValue());
        } else if (movement.getType() == TypeCheckoutMovement.SAIDA) {
            this.totalExit = this.totalExit.add(movement.getValue());
        }
    }

    public List<CheckoutMovement> getMovements() {
        return movements;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CheckoutStatus getStatus() {
        return status;
    }

    public void setStatus(CheckoutStatus status) {
        this.status = status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public BigDecimal getInitialCash() {
        return initialCash;
    }

    public void setInitialCash(BigDecimal initialCash) {
        this.initialCash = initialCash;
    }

    public BigDecimal getTotalEntry() {
        return totalEntry;
    }

    public void setTotalEntry(BigDecimal totalEntry) {
        this.totalEntry = totalEntry;
    }

    public BigDecimal getTotalExit() {
        return totalExit;
    }

    public void setTotalExit(BigDecimal totalExit) {
        this.totalExit = totalExit;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public User getOpenedBy() {
        return openedBy;
    }

    public void setOpenedBy(User openedBy) {
        this.openedBy = openedBy;
    }

    public User getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy = closedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
