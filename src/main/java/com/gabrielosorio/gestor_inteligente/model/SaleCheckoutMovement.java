package com.gabrielosorio.gestor_inteligente.model;
import java.math.BigDecimal;

public class SaleCheckoutMovement {

    private Long id;
    private CheckoutMovement checkoutMovement;
    private Sale sale;
    private BigDecimal value;
    private String obs;

    public SaleCheckoutMovement(Long id, CheckoutMovement checkoutMovement, Sale sale, BigDecimal value, String obs) {
        this.id = id;
        this.checkoutMovement = checkoutMovement;
        this.sale = sale;
    }

    public SaleCheckoutMovement(CheckoutMovement checkoutMovement, Sale sale) {
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

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    @Override
    public String toString() {
        return "SaleCheckoutMovement{" +
                "id=" + id +
                ", checkoutMovement=" + checkoutMovement +
                ", sale=" + sale +
                ", value=" + value +
                ", obs='" + obs + '\'' +
                '}';
    }
}
