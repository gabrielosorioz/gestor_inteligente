package com.gabrielosorio.gestor_inteligente.model;
import java.math.BigDecimal;

public class SaleCheckoutMovement {

    private long id;
    private CheckoutMovement checkoutMovement;
    private Sale sale;

    public SaleCheckoutMovement(CheckoutMovement checkoutMovement, Sale sale) {
        this.checkoutMovement = checkoutMovement;
        this.sale = sale;
    }

    public SaleCheckoutMovement(long id,CheckoutMovement checkoutMovement, Sale sale) {
        this.checkoutMovement = checkoutMovement;
        this.sale = sale;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CheckoutMovement getCheckoutMovement() {
        return checkoutMovement;
    }

    public void setCheckoutMovement(CheckoutMovement checkoutMovement) {
        this.checkoutMovement = checkoutMovement;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    @Override
    public String toString() {
        return "SaleCheckoutMovement{" +
                "id=" + id +
                ", checkoutMovement=" + checkoutMovement +
                ", sale=" + sale +
                '}';
    }
}
