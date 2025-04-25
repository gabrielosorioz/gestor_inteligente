package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.view.table.ColumnType;
import com.gabrielosorio.gestor_inteligente.view.table.TableColumnConfig;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewComponent;
import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@TableViewComponent
public class CheckoutMovement {

    // Atributos básicos
    private long id;
    private Checkout checkout;
    private CheckoutMovementType checkoutMovementType;
    private LocalDateTime dateTime;
    private Payment payment;
    private BigDecimal value;
    private String obs;

    private final LongProperty idProperty = new SimpleLongProperty();

    private final ObjectProperty<Checkout> checkoutProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<CheckoutMovementType> movementTypeProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDateTime> dateTimeProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<Payment> paymentProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<BigDecimal> valueProperty = new SimpleObjectProperty<>();

    private final StringProperty obsProperty = new SimpleStringProperty();


    public CheckoutMovement() {

    }

    // Getters and Setters for basic fields

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        this.idProperty.set(id);
    }

    public Checkout getCheckout() {
        return checkout;
    }

    public void setCheckout(Checkout checkout) {
        this.checkout = checkout;
        this.checkoutProperty.set(checkout);
    }

    public CheckoutMovementType getMovementType() {
        return checkoutMovementType;
    }

    public void setMovementType(CheckoutMovementType checkoutMovementType) {
        this.checkoutMovementType = checkoutMovementType;
        this.movementTypeProperty.set(checkoutMovementType);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        this.dateTimeProperty.set(dateTime);
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        this.paymentProperty.set(payment);
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
        this.valueProperty.set(value);
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
        this.obsProperty.set(obs);
    }

    // Methods to access observable properties

    public LongProperty idProperty() {
        return idProperty;
    }

    public ObjectProperty<Checkout> checkoutProperty() {
        return checkoutProperty;
    }

    public ObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTimeProperty;
    }

    @TableColumnConfig(header = "Observação", order = 6)
    public StringProperty obsProperty() {
        return obsProperty;
    }

    @TableColumnConfig(header = "Data", order = 0)
    public SimpleStringProperty dateProperty() {
        LocalDate dt = dateTime.toLocalDate();
        var formattedDate = dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return new SimpleStringProperty(formattedDate);
    }

    @TableColumnConfig(header = "Hora", order = 1)
    public SimpleStringProperty timeProperty(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return new SimpleStringProperty(dateTime.toLocalTime().format(formatter));
    }

    @TableColumnConfig(header = "Tipo de Movimento", order = 2)
    public SimpleStringProperty movementTypeProperty() {
        return new SimpleStringProperty(checkoutMovementType.getName());
    }

    @TableColumnConfig(header = "F. Pagamento", order = 3)
    public SimpleStringProperty paymentProperty() {
        return new SimpleStringProperty(payment.getDescription().toUpperCase());
    }

    @TableColumnConfig(header = "Valor", order = 4, columnType = ColumnType.MONETARY)
    public ObjectProperty<BigDecimal> valueProperty() {
        return valueProperty;
    }

    @Override
    public String toString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return "CheckoutMovement{" +
                "id=" + id +
                ", checkout=" + (checkout != null ? checkout.toString() : "null") +
                ", checkoutMovementType=" + (checkoutMovementType != null ? checkoutMovementType.getName() : "null") +
                ", dateTime=" + (dateTime != null ? dateTime.format(dateFormatter) + " " + dateTime.format(timeFormatter) : "null") +
                ", payment=" + (payment != null ? payment.getDescription() : "null") +
                ", value=" + value +
                ", obs='" + obs + '\'' +
                '}';
    }

}
