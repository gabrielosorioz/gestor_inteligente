package com.gabrielosorio.gestor_inteligente.model;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class CheckoutMovement {

    private long id;
    private Optional<Sale> sale = Optional.empty();
    private Checkout checkout;
    private CheckoutMovementType checkoutMovementType;
    private LocalDateTime dateTime;
    private Payment payment;
    private BigDecimal value;
    private String obs;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Optional<Sale> getSale() {
        return sale;
    }

    public void setSale(Optional<Sale> sale) {
        this.sale = sale;
    }

    public Checkout getCheckout() {
        return checkout;
    }

    public void setCheckout(Checkout checkout) {
        this.checkout = checkout;
    }

    public CheckoutMovementType getMovementType() {
        return checkoutMovementType;
    }

    public void setMovementType(CheckoutMovementType checkoutMovementType) {
        this.checkoutMovementType = checkoutMovementType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
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
}
