package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.Status;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {

    private Integer id;
    private Integer productID;
    private String barCode;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private double profitPercent;
    private Status status;
    private Timestamp dateCreate;
    private Timestamp dateUpdate;
    private Timestamp dateDelete;

    public Product(ProductBuilder productBuilder) {
        this.id = productBuilder.id;
        this.productID = productBuilder.productId;
        this.barCode = productBuilder.barCode;
        this.description = productBuilder.description;
        this.costPrice = productBuilder.costPrice;
        this.sellingPrice = productBuilder.sellingPrice;
        this.profitPercent = productBuilder.profitPercent;
        this.status = productBuilder.status;
        this.dateCreate = productBuilder.dateCreate;
        this.dateUpdate = productBuilder.dateUpdate;
        this.dateDelete = productBuilder.dateDelete;
    }

    public static ProductBuilder builder(){
        return new ProductBuilder();
    }

    private static class ProductBuilder {
        private Integer id;
        private Integer productId;
        private String barCode;
        private String description;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private double profitPercent;
        private Status status;
        private Timestamp dateCreate;
        private Timestamp dateUpdate;
        private Timestamp dateDelete;

        public ProductBuilder id(Integer id){
            this.id = id;
            return this;
        }

        public ProductBuilder productId(Integer productId){
            this.productId = productId ;
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

        public ProductBuilder profitPercent(double profitPercent){
            this.profitPercent = profitPercent;
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

        public Product build(){
            return new Product(this);
        }

    }


    public Integer getId() {
        return id;
    }

    public Integer getProductID() {
        return productID;
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
        return profitPercent;
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
}
