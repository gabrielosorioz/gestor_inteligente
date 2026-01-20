package com.gabrielosorio.gestor_inteligente.model;

import javafx.beans.property.*;
import com.gabrielosorio.gestor_inteligente.view.table.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@TableViewComponent
public class SaleProduct {

    private Long id;
    private long saleId;
    private Product product;
    private Sale sale;
    private long quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal originalSubtotal = BigDecimal.ZERO;
    private BigDecimal subTotal = BigDecimal.ZERO;

    @TableColumnConfig(header = "Desconto", order = 5, columnType = ColumnType.DEFAULT)
    @EditableColumn(
            editType = EditType.MONETARY,
            propertyUpdater = "setDiscount",
            currencySymbol = "R$"
    )
    private BigDecimal discount = BigDecimal.ZERO;

    private ObjectProperty<BigDecimal> subTotalProperty = new SimpleObjectProperty<>();
    private ObjectProperty<BigDecimal> unitPriceProperty = new SimpleObjectProperty<>();
    private LongProperty quantityProperty = new SimpleLongProperty();
    private ObjectProperty<BigDecimal> discountProperty = new SimpleObjectProperty<>();


    @TableColumnConfig(header = "Código", order = 1)
    public LongProperty productCodeProperty() {
        if (product != null) {
            return product.productCodeProperty();
        }
        return new SimpleLongProperty(0);
    }

    @TableColumnConfig(header = "Descrição", order = 2)
    public StringProperty productDescriptionProperty() {
        if (product != null) {
            return product.descriptionProperty();
        }
        return new SimpleStringProperty("N/A");
    }

    @TableColumnConfig(header = "Quantidade", order = 3)
    @EditableColumn(editType = EditType.INTEGER, propertyUpdater = "setQuantity")
    public LongProperty quantityProperty() {
        return quantityProperty;
    }

    @TableColumnConfig(header = "Preço Unit.", order = 4, columnType = ColumnType.MONETARY)
    public ObjectProperty<BigDecimal> unitPriceProperty() {
        return unitPriceProperty;
    }


    @TableColumnConfig(header = "Subtotal", order = 6, columnType = ColumnType.MONETARY)
    public ObjectProperty<BigDecimal> subtotalProperty() {
        return subTotalProperty;
    }

    public SaleProduct(Product product){
        this.product = product;
        unitPrice = product.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
        discount = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);
        quantity = 1;
        originalSubtotal = calculateOriginalSubtotal();
        subTotal = calculateSubtotal();

        // Inicializar properties
        subTotalProperty.set(subTotal);
        unitPriceProperty.set(unitPrice);
        quantityProperty.set(quantity);
        discountProperty.set(discount);
    }

    public SaleProduct(Product product, long quantity){
        this.product = product;
        unitPrice = product.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
        discount = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);
        this.quantity = quantity;
        originalSubtotal = calculateOriginalSubtotal();
        subTotal = calculateSubtotal();

        // Inicializar properties
        subTotalProperty = new SimpleObjectProperty<>(calculateSubtotal());
        unitPriceProperty = new SimpleObjectProperty<>(unitPrice);
        quantityProperty = new SimpleLongProperty(quantity);
        discountProperty = new SimpleObjectProperty<>(discount);
    }

    public SaleProduct(){
        // Inicializar properties mesmo no construtor vazio
        subTotalProperty = new SimpleObjectProperty<>(BigDecimal.ZERO);
        unitPriceProperty = new SimpleObjectProperty<>(BigDecimal.ZERO);
        quantityProperty = new SimpleLongProperty(0);
        discountProperty = new SimpleObjectProperty<>(BigDecimal.ZERO);
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSaleId() {
        return saleId;
    }

    public void setSaleId(long saleId) {
        this.saleId = saleId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            unitPrice = product.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
            unitPriceProperty.set(unitPrice);
            subTotal = calculateSubtotal();
            subTotalProperty.set(subTotal);
        }
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
        if (sale != null) {
            this.saleId = sale.getId();
        }
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
        quantityProperty.set(quantity);

        // Recalcular valores
        subTotal = calculateSubtotal();
        originalSubtotal = calculateOriginalSubtotal();
        subTotalProperty.set(subTotal);
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        unitPriceProperty.set(this.unitPrice);

        // Recalcular valores
        subTotal = calculateSubtotal();
        originalSubtotal = calculateOriginalSubtotal();
        subTotalProperty.set(subTotal);
    }

    public BigDecimal getSubTotal() {
        subTotal = calculateSubtotal();
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getDiscount() {
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        discountProperty.set(discount);

        // Recalcular valores
        subTotal = calculateSubtotal();
        originalSubtotal = calculateOriginalSubtotal();
        subTotalProperty.set(subTotal);
    }

    private BigDecimal calculateSubtotal(){
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .subtract(discount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOriginalSubtotal(){
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getOriginalSubtotal() {
        return originalSubtotal;
    }

    public void setOriginalSubtotal(BigDecimal originalSubtotal) {
        this.originalSubtotal = originalSubtotal;
    }

    @Override
    public String toString() {
        return "SaleProduct{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", product=" + product +
                ", sale=" + sale +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subTotal=" + subTotal +
                ", discount=" + discount +
                ", subTotalProperty=" + subTotalProperty +
                ", quantityProperty=" + quantityProperty +
                '}';
    }
}