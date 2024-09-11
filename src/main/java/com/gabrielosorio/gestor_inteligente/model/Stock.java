package com.gabrielosorio.gestor_inteligente.model;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import java.sql.Timestamp;

public class Stock {

    private long id;
    private Product product;
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
                ", productId=" + product.getProductCode() +
                ", barCode='" + getProduct().getBarCode() + '\'' +
                ", productName='" + getProduct().getDescription() + '\'' +
                ", quantity=" + quantity +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
