package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.utils.ProductCalculationUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {

    private Integer id;
    private Integer productCode;
    private String barCode;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Supplier supplier;
    private Category category;
    private double profitMargin;
    private double markupPercent;
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

    }

    public static ProductBuilder builder(){
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private Integer id;
        private Integer productCode;
        private String barCode;
        private String description;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private double profitMargin;
        private double markupPercent;
        private Status status;
        private Timestamp dateCreate;
        private Timestamp dateUpdate;
        private Timestamp dateDelete;
        private Supplier supplier;
        private Category category;

        public ProductBuilder id(Integer id){
            this.id = id;
            return this;
        }

        public ProductBuilder productCode(Integer productCode){
            this.productCode = productCode;
            return this;
        }

        public ProductBuilder barCode(String barCode){
            this.barCode = barCode;
            return this;
        }

        public ProductBuilder description(String description){
            this.description = description;
            return this;
        }

        public ProductBuilder costPrice(BigDecimal costPrice){
            this.costPrice = costPrice;
            return this;
        }

        public ProductBuilder sellingPrice(BigDecimal sellingPrice){
            this.sellingPrice = sellingPrice;
            return this;
        }

        public ProductBuilder status(Status status){
            this.status = status;
            return this;
        }

        public ProductBuilder dateCreate(Timestamp dateCreate){
            this.dateCreate = dateCreate;
            return this;
        }

        public ProductBuilder dateUpdate(Timestamp dateUpdate){
            this.dateUpdate = dateUpdate;
            return this;
        }

        public ProductBuilder dateDelete(Timestamp dateDelete){
            this.dateDelete = dateDelete;
            return this;
        }

        public ProductBuilder category(Category category){
            this.category = category;
            return this;
        }

        public ProductBuilder supplier(Supplier supplier){
            this.supplier = supplier;
            return this;
        }


        public Product build() {
            this.markupPercent = ProductCalculationUtils.calculateMarkup(this.costPrice,this.sellingPrice);
            this.profitMargin = ProductCalculationUtils.calculateProfitMargin(this.costPrice,this.sellingPrice);
            Product product = new Product(this);
            ProductValidator.validate(product);
            return product;
        }

    }

    private void validate() {
        ProductValidator.validate(this);
    }

    private void updateCalculations(){
        this.markupPercent = ProductCalculationUtils.calculateMarkup(this.costPrice,this.sellingPrice);
        this.profitMargin = ProductCalculationUtils.calculateProfitMargin(this.costPrice,this.sellingPrice);
    }


    public Integer getId() {
        return id;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public String getBarCode() {
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

    public Supplier getSupplier() {
        return supplier;
    }

    public Category getCategory() {
        return category;
    }

    public double getMarkupPercent() {
        return markupPercent;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setProductID(Integer productCode) {
        this.productCode = productCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCostPrice(BigDecimal costPrice) {
        ProductValidator.validatePrices(costPrice,this.sellingPrice);
        this.costPrice = costPrice;
        validate();
        updateCalculations();
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        ProductValidator.validatePrices(this.costPrice,sellingPrice);
        this.sellingPrice = sellingPrice;
        updateCalculations();
        validate();
    }


    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public void setCategory(Category category) {
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

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productCode=" + productCode +
                ", barCode='" + barCode + '\'' +
                ", description='" + description + '\'' +
                ", costPrice=" + costPrice +
                ", sellingPrice=" + sellingPrice +
                ", supplier=" + supplier +
                ", category=" + category +
                ", profitMargin=" + profitMargin +
                ", markupPercent=" + markupPercent +
                ", status=" + status +
                ", dateCreate=" + dateCreate +
                ", dateUpdate=" + dateUpdate +
                ", dateDelete=" + dateDelete +
                '}';
    }
}
