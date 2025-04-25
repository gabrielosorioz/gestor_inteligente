package com.gabrielosorio.gestor_inteligente.model;
import com.gabrielosorio.gestor_inteligente.model.enums.SaleStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Sale {

    private long id;
    private Timestamp dateSale;
    private Timestamp dataCancel;
    private List<SaleProduct> saleProducts;
    private BigDecimal totalChange = BigDecimal.ZERO;
    private BigDecimal totalAmountPaid = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);;
    private BigDecimal originalTotalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);;
    private BigDecimal totalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);;
    private BigDecimal totalDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);;
    private List<Payment> paymentMethods;
    private SaleStatus status;

    public Sale (List<SaleProduct> saleProducts){

        if(saleProducts.isEmpty() || saleProducts == null){
            throw new IllegalArgumentException("Error at initializing Sale constructor:Product saleProducts for sale is null.");
        }

        this.saleProducts = new ArrayList<>(saleProducts);
        this.dateSale = Timestamp.from(Instant.now());
        this.paymentMethods = new ArrayList<>();

        calculateTotals();
    }
    
    public Sale(){
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getDateSale() {
        return dateSale;
    }

    public void setDateSale(Timestamp dateSale) {
        this.dateSale = dateSale;
    }

    public Timestamp getDataCancel() {
        return dataCancel;
    }

    public void setDataCancel(Timestamp dataCancel) {
        this.dataCancel = dataCancel;
    }

    public List<SaleProduct> getSaleProducts() {
        return saleProducts;
    }

    public void setSaleProducts(List<SaleProduct> saleProducts) {
        if(saleProducts == null || saleProducts.isEmpty()){
            throw new IllegalArgumentException("Items for sale cannot be null or empty.");
        }
        this.saleProducts = saleProducts;
        calculateTotals();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getOriginalTotalPrice() {
        return originalTotalPrice;
    }

    public void setOriginalTotalPrice(BigDecimal originalTotalPrice) {
        this.originalTotalPrice = originalTotalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
        this.totalPrice = originalTotalPrice.subtract(this.totalDiscount).setScale(2,RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        calculateChange();
    }

    public void setPaymentMethods(List<Payment> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            throw new IllegalArgumentException("Error to set payment: Sale items are null or empty");
        }
        this.paymentMethods = paymentMethods;
    }

    public List<Payment> getPaymentMethods() {
        return paymentMethods;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    private void calculateTotals(){
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountTotal = BigDecimal.ZERO.setScale(2,RoundingMode.HALF_UP);

        for(SaleProduct item : this.saleProducts){
            subtotal = subtotal.add(item.getOriginalSubtotal());
            discountTotal = discountTotal.add(item.getDiscount());
        }

        this.originalTotalPrice = subtotal;
        this.totalDiscount = discountTotal;
        this.totalPrice = subtotal.subtract(discountTotal).max(BigDecimal.ZERO).max(BigDecimal.ZERO);

    }

    public BigDecimal getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
        calculateChange();
    }

    private void calculateChange() {
        if(totalPrice.compareTo(totalAmountPaid) > 0){
            totalChange = BigDecimal.ZERO;
        } else {
            totalChange = totalAmountPaid.subtract(totalPrice).setScale(2,RoundingMode.HALF_UP);
        }
    }

    public BigDecimal getTotalChange(){
        calculateChange();
        return totalChange.max(BigDecimal.ZERO);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", dateSale=" + dateSale +
                ", dataCancel=" + dataCancel +
                ", saleProducts=" + saleProducts +
                ", totalChange=" + totalChange +
                ", totalAmountPaid=" + totalAmountPaid +
                ", originalTotalPrice=" + originalTotalPrice +
                ", totalPrice=" + totalPrice +
                ", totalDiscount=" + totalDiscount +
                ", paymentMethods=" + paymentMethods +
                ", status=" + status +
                '}';
    }
}
