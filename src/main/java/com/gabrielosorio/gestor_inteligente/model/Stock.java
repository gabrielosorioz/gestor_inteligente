package com.gabrielosorio.gestor_inteligente.model;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import java.sql.Timestamp;

public class Stock {

    private long id;
    private Product product;
    private long productId;
    private String barCode;
    private String productName;
    private long quantity;
    private Timestamp lastUpdate;


    public Stock(Product product, long quantity){
        ProductValidator.validate(product);
        this.product = product;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
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

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
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
