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
    private BigDecimal totalAmountPaid = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private BigDecimal originalTotalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal itemsDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal saleDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal totalDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal totalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private List<Payment> paymentMethods;
    private SaleStatus status;

    public Sale(List<SaleProduct> saleProducts) {
        if(saleProducts == null || saleProducts.isEmpty()) {
            throw new IllegalArgumentException("Error at initializing Sale constructor: Product saleProducts for sale is null.");
        }

        this.saleProducts = new ArrayList<>(saleProducts);
        this.dateSale = Timestamp.from(Instant.now());
        this.paymentMethods = new ArrayList<>();

        calculateTotals();
    }

    public Sale() {
    }

    public long getId() {
        return id;
    }

    public Timestamp getDateSale() {
        return dateSale;
    }

    public Timestamp getDataCancel() {
        return dataCancel;
    }

    public List<SaleProduct> getSaleProducts() {
        return saleProducts;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getOriginalTotalPrice() {
        return originalTotalPrice;
    }

    /**
     * Retorna o desconto total aplicado aos produtos individuais
     * @return soma dos descontos dos SaleProducts
     */
    public BigDecimal getItemsDiscount() {
        return itemsDiscount;
    }

    /**
     * Retorna o desconto aplicado na venda como um todo
     * @return desconto da venda (negociação, cupom, etc)
     */
    public BigDecimal getSaleDiscount() {
        return saleDiscount;
    }

    /**
     * Retorna o desconto total (itens + venda)
     * @return itemsDiscount + saleDiscount
     */
    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public List<Payment> getPaymentMethods() {
        return paymentMethods;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public BigDecimal getTotalChange() {
        calculateChange();
        return totalChange.max(BigDecimal.ZERO);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDateSale(Timestamp dateSale) {
        this.dateSale = dateSale;
    }

    public void setDataCancel(Timestamp dataCancel) {
        this.dataCancel = dataCancel;
    }

    /**
     * Define os produtos da venda e recalcula os totais
     * Recalcula: originalTotalPrice, itemsDiscount, totalDiscount e totalPrice
     * Preserva: saleDiscount (deve ser setado separadamente)
     *
     * @param saleProducts Lista de produtos da venda
     */
    public void setSaleProducts(List<SaleProduct> saleProducts) {
        if(saleProducts == null || saleProducts.isEmpty()) {
            throw new IllegalArgumentException("Items for sale cannot be null or empty.");
        }
        this.saleProducts = saleProducts;
        calculateTotals();
    }

    /**
     * Define o preço original total (usado no mapeamento do banco)
     * @param originalTotalPrice Preço original sem descontos
     */
    public void setOriginalTotalPrice(BigDecimal originalTotalPrice) {
        this.originalTotalPrice = originalTotalPrice;
    }

    /**
     * Define o desconto dos itens (usado no mapeamento do banco)
     * @param itemsDiscount Soma dos descontos dos produtos
     */
    public void setItemsDiscount(BigDecimal itemsDiscount) {
        this.itemsDiscount = itemsDiscount;
    }

    /**
     * Define o desconto da venda e recalcula os totais
     * @param saleDiscount Desconto aplicado na venda como um todo
     */
    public void setSaleDiscount(BigDecimal saleDiscount) {
        this.saleDiscount = saleDiscount;
        recalculateTotals();
    }

    /**
     * Define o desconto total (usado no mapeamento do banco)
     * @param totalDiscount Desconto total da venda
     */
    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
        recalculateTotals();
    }

    /**
     * Define o preço total (usado no mapeamento do banco)
     * @param totalPrice Preço final da venda
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Define os métodos de pagamento
     * @param paymentMethods Lista de métodos de pagamento
     */
    public void setPaymentMethods(List<Payment> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            throw new IllegalArgumentException("Error to set payment: Sale items are null or empty");
        }
        this.paymentMethods = paymentMethods;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    /**
     * Define o valor pago pelo cliente e recalcula o troco
     * @param totalAmountPaid Valor total pago
     */
    public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
        calculateChange();
    }

    /**
     * Calcula os totais da venda baseado nos produtos
     * - originalTotalPrice: soma dos originalSubtotal dos produtos
     * - itemsDiscount: soma dos descontos dos produtos
     * - totalDiscount: itemsDiscount + saleDiscount
     * - totalPrice: originalTotalPrice - totalDiscount
     */
    private void calculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal itemsDiscountCalc = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for(SaleProduct item : this.saleProducts) {
            subtotal = subtotal.add(item.getOriginalSubtotal());
            itemsDiscountCalc = itemsDiscountCalc.add(item.getDiscount());
        }

        this.originalTotalPrice = subtotal;
        this.itemsDiscount = itemsDiscountCalc;

        recalculateTotals();
    }

    /**
     * Recalcula apenas totalDiscount e totalPrice
     * Usado quando saleDiscount é alterado
     */
    private void recalculateTotals() {
        this.totalDiscount = this.itemsDiscount.add(this.saleDiscount);
        this.totalPrice = this.originalTotalPrice
                .subtract(this.totalDiscount)
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }

    /**
     * Calcula o troco baseado no valor pago vs preço total
     */
    private void calculateChange() {
        if(totalPrice.compareTo(totalAmountPaid) > 0) {
            totalChange = BigDecimal.ZERO;
        } else {
            totalChange = totalAmountPaid.subtract(totalPrice).setScale(2, RoundingMode.HALF_UP);
        }
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
                ", itemsDiscount=" + itemsDiscount +
                ", saleDiscount=" + saleDiscount +
                ", totalDiscount=" + totalDiscount +
                ", totalPrice=" + totalPrice +
                ", paymentMethods=" + paymentMethods +
                ", status=" + status +
                '}';
    }
}