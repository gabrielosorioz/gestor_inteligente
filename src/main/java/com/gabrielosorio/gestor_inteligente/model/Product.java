package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.utils.ProductCalculationUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

public class Product {

    private long id;
    private long productCode;
    private Optional<String> barCode;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Optional<Supplier> supplier;
    private Optional<Category> category;
    private double profitMargin;
    private double markupPercent;
    private long quantity;
    private Status status;
    private Timestamp dateCreate;
    private Timestamp dateUpdate;
    private Timestamp dateDelete;

    public Product(ProductBuilder productBuilder) {
        this.id = productBuilder.id;
        this.productCode = productBuilder.productCode;
        this.barCode = productBuilder.barCode;
        this.description = productBuilder.description;
        this.costPrice = productBuilder.costPrice;
        this.sellingPrice = productBuilder.sellingPrice;
        this.profitMargin = productBuilder.profitMargin;
        this.status = productBuilder.status;
        this.dateCreate = productBuilder.dateCreate;
        this.dateUpdate = productBuilder.dateUpdate;
        this.dateDelete = productBuilder.dateDelete;
        this.supplier = productBuilder.supplier;
        this.category = productBuilder.category;
        this.markupPercent = productBuilder.markupPercent;
        this.quantity = productBuilder.quantity;
    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private long id;
        private long productCode;
        private Optional<String> barCode = Optional.empty();
        private String description;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private double profitMargin;
        private double markupPercent;
        private long quantity;
        private Status status;
        private Timestamp dateCreate;
        private Timestamp dateUpdate;
        private Timestamp dateDelete;
        private Optional<Supplier> supplier = Optional.empty();
        private Optional<Category> category = Optional.empty();

        public ProductBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ProductBuilder productCode(long productCode) {
            this.productCode = productCode;
            return this;
        }

        public ProductBuilder barCode(Optional<String> barCode) {
            this.barCode = barCode;
            return this;
        }

        public ProductBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProductBuilder costPrice(BigDecimal costPrice) {
            this.costPrice = costPrice;
            return this;
        }

        public ProductBuilder sellingPrice(BigDecimal sellingPrice) {
            this.sellingPrice = sellingPrice;
            return this;
        }

        public ProductBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public ProductBuilder dateCreate(Timestamp dateCreate) {
            this.dateCreate = dateCreate;
            return this;
        }

        public ProductBuilder dateUpdate(Timestamp dateUpdate) {
            this.dateUpdate = dateUpdate;
            return this;
        }

        public ProductBuilder dateDelete(Timestamp dateDelete) {
            this.dateDelete = dateDelete;
            return this;
        }

        public ProductBuilder category(Optional<Category> category) {
            this.category = category;
            return this;
        }

        public ProductBuilder supplier(Optional<Supplier> supplier) {
            this.supplier = supplier;
            return this;
        }

        public ProductBuilder quantity(long quantity) {
            this.quantity = quantity;
            return this;
        }

        public Product build() {
            this.markupPercent = ProductCalculationUtils.calculateMarkup(this.costPrice, this.sellingPrice);
            this.profitMargin = ProductCalculationUtils.calculateProfitMargin(this.costPrice, this.sellingPrice);
            Product product = new Product(this);
            ProductValidator.validate(product);
            return product;
        }
    }

    private void validate() {
        ProductValidator.validate(this);
    }

    private void updateCalculations() {
        this.markupPercent = ProductCalculationUtils.calculateMarkup(this.costPrice, this.sellingPrice);
        this.profitMargin = ProductCalculationUtils.calculateProfitMargin(this.costPrice, this.sellingPrice);
    }

    public long getId() {
        return id;
    }

    public long getProductCode() {
        return productCode;
    }

    public Optional<String> getBarCode() {
        return barCode;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public double getProfitPercent() {
        return profitMargin;
    }

    public Status getStatus() {
        return status;
    }

    public Timestamp getDateCreate() {
        return dateCreate;
    }

    public Timestamp getDateUpdate() {
        return dateUpdate;
    }

    public Timestamp getDateDelete() {
        return dateDelete;
    }

    public Optional<Supplier> getSupplier() {
        return supplier;
    }

    public Optional<Category> getCategory() {
        return category;
    }

    public double getMarkupPercent() {
        return markupPercent;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setProductCode(long productCode) {
        this.productCode = productCode;
    }

    public void setBarCode(Optional<String> barCode) {
        this.barCode = barCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCostPrice(BigDecimal costPrice) {
        ProductValidator.validatePrices(costPrice, this.sellingPrice);
        this.costPrice = costPrice;
        validate();
        updateCalculations();
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        ProductValidator.validatePrices(this.costPrice, sellingPrice);
        this.sellingPrice = sellingPrice;
        updateCalculations();
        validate();
    }

    public void setSupplier(Optional<Supplier> supplier) {
        this.supplier = supplier;
    }

    public void setCategory(Optional<Category> category) {
        this.category = category;
    }

    public void setProfitPercent(double profitMargin) {
        this.profitMargin = profitMargin;
    }

    public void setMarkupPercent(double markupPercent) {
        this.markupPercent = markupPercent;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDateCreate(Timestamp dateCreate) {
        this.dateCreate = dateCreate;
    }

    public void setDateUpdate(Timestamp dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public void setDateDelete(Timestamp dateDelete) {
        this.dateDelete = dateDelete;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productCode=" + productCode +
                ", barCode=" + barCode.orElse("N/A") +
                ", description='" + description + '\'' +
                ", costPrice=" + costPrice +
                ", sellingPrice=" + sellingPrice +
                ", supplier=" + supplier.map(Supplier::toString).orElse("N/A") +
                ", category=" + category.map(Category::toString).orElse("N/A") +
                ", profitMargin=" + profitMargin +
                ", markupPercent=" + markupPercent +
                ", status=" + status +
                ", dateCreate=" + dateCreate +
                ", dateUpdate=" + dateUpdate +
                ", dateDelete=" + dateDelete +
                ", quantity " + quantity +
                '}';
    }
}
