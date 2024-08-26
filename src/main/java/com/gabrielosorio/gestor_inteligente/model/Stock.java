package com.gabrielosorio.gestor_inteligente.model;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import java.sql.Timestamp;

public class Stock {

    private Integer id;
    private Product product;
    private Integer productId;
    private String barCode;
    private String productName;
    private int quantity;
    private Timestamp lastUpdate;

    public Stock(Integer id, Product product, Integer productId, String barCode, String productName, int quantity, Timestamp lastUpdate) {
        this.id = id;
        this.product = product;
        this.productId = productId;
        this.barCode = barCode;
        this.productName = productName;
        this.quantity = quantity;
        this.lastUpdate = lastUpdate;
    }

    public Stock(Product product, int quantity){
        ProductValidator.validate(product);
        this.product = product;
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", product=" + product +
                ", productId=" + productId +
                ", barCode='" + barCode + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
