package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.utils.ProductCalculationUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import com.gabrielosorio.gestor_inteligente.view.table.ColumnType;
import com.gabrielosorio.gestor_inteligente.view.table.TableColumnConfig;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewComponent;
import javafx.beans.property.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

@TableViewComponent
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


    /** observable properties */

    @TableColumnConfig(header = "Código", order = 1)
    private final LongProperty productCodeProp = new SimpleLongProperty();

    @TableColumnConfig(header = "Descrição", order = 2)
    private final StringProperty descriptionProp = new SimpleStringProperty();

    @TableColumnConfig(header = "Preço de Custo", order = 3, columnType = ColumnType.MONETARY, currencySymbol = "R$")
    private final ObjectProperty<BigDecimal> costPriceProp = new SimpleObjectProperty<>();

    @TableColumnConfig(header = "Preço de Venda", order = 4, columnType = ColumnType.MONETARY, currencySymbol = "R$")
    private final ObjectProperty<BigDecimal> sellingPriceProp = new SimpleObjectProperty<>();

    @TableColumnConfig(header = "Estoque", order = 5)
    private final LongProperty quantityProp = new SimpleLongProperty();

    @TableColumnConfig(header = "Categoria", order = 6)
    public StringProperty getCategoryDescription() {
        return new SimpleStringProperty(
                categoryProperty().get().map(Category::getDescription).orElse("N/A")
        );
    }

    private final LongProperty idProp = new SimpleLongProperty();
    private final StringProperty barCodeProp = new SimpleStringProperty();
    private final ObjectProperty<Optional<Supplier>> supplierProp = new SimpleObjectProperty<>();
    private final ObjectProperty<Optional<Category>> categoryProp = new SimpleObjectProperty<>();
    private final DoubleProperty profitMarginProp = new SimpleDoubleProperty();
    private final DoubleProperty markupPercentProp = new SimpleDoubleProperty();
    private final ObjectProperty<Status> statusProp = new SimpleObjectProperty<>();
    private final ObjectProperty<Timestamp> dateCreateProp = new SimpleObjectProperty<>();
    private final ObjectProperty<Timestamp> dateUpdateProp = new SimpleObjectProperty<>();
    private final ObjectProperty<Timestamp> dateDeleteProp = new SimpleObjectProperty<>();



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

        /** set observable properties */
        idProp.set(productBuilder.id);
        productCodeProp.set(productBuilder.productCode);
        barCodeProp.set(productBuilder.barCode.orElse(null));
        descriptionProp.set(productBuilder.description);
        costPriceProp.set(productBuilder.costPrice);
        sellingPriceProp.set(productBuilder.sellingPrice);
        profitMarginProp.set(productBuilder.profitMargin);
        statusProp.set(productBuilder.status);
        dateCreateProp.set(productBuilder.dateCreate);
        dateUpdateProp.set(productBuilder.dateUpdate);
        dateDeleteProp.set(productBuilder.dateDelete);
        supplierProp.set(productBuilder.supplier);
        categoryProp.set(productBuilder.category);
        markupPercentProp.set(productBuilder.markupPercent);
        quantityProp.set(productBuilder.quantity);

        costPriceProp.addListener((observable, oldValue, newValue) -> updateCalculationsProps());
        sellingPriceProp.addListener((observable, oldValue, newValue) -> updateCalculationsProps());

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

    private void updateCalculationsProps() {
        if (costPriceProp.get() != null && sellingPriceProp.get() != null) {
            BigDecimal costPrice = costPriceProp.get();
            BigDecimal sellingPrice = sellingPriceProp.get();

            if (costPrice.compareTo(BigDecimal.ZERO) > 0) {
                double markup = ProductCalculationUtils.calculateMarkup(costPrice, sellingPrice);
                double profit = ProductCalculationUtils.calculateProfitMargin(costPrice, sellingPrice);

                if (markup >= 0 && profit >= 0) {
                    markupPercentProp.set(markup);
                    profitMarginProp.set(profit);
                }
            }
        }
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
        idProp.set(id);
    }

    public void setProductCode(long productCode) {
        this.productCode = productCode;
        productCodeProp.set(productCode);
    }

    public void setBarCode(Optional<String> barCode) {
        this.barCode = barCode;
        barCodeProp.set(barCode.orElse(null));
    }

    public void setDescription(String description) {
        this.description = description;
        descriptionProp.set(description);
    }

    public void updatePrices(BigDecimal costPrice, BigDecimal sellingPrice) {
        ProductValidator.validatePrices(costPrice, sellingPrice);
        this.costPrice = costPrice;
        costPriceProp.set(costPrice);
        this.sellingPrice = sellingPrice;
        sellingPriceProp.set(sellingPrice);
        validate();
        updateCalculations();
    }


    public void setCostPrice(BigDecimal costPrice) {
        ProductValidator.validatePrices(costPrice, this.sellingPrice);
        this.costPrice = costPrice;
        costPriceProp.set(costPrice);
        validate();
        updateCalculations();
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        ProductValidator.validatePrices(this.costPrice, sellingPrice);
        this.sellingPrice = sellingPrice;
        sellingPriceProp.set(sellingPrice);
        updateCalculations();
        validate();
    }

    public void setSupplier(Optional<Supplier> supplier) {
        this.supplier = supplier;
        supplierProp.set(supplier);
    }

    public void setCategory(Optional<Category> category) {
        this.category = category;
        categoryProp.set(category);
    }

    public void setProfitPercent(double profitMargin) {
        this.profitMargin = profitMargin;
    }

    public void setMarkupPercent(double markupPercent) {
        this.markupPercent = markupPercent;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.statusProp.set(status);
    }

    public void setDateCreate(Timestamp dateCreate) {
        this.dateCreate = dateCreate;
    }

    public void setDateUpdate(Timestamp dateUpdate) {
        this.dateUpdate = dateUpdate;
        this.dateUpdateProp.set(dateUpdate);
    }

    public void setDateDelete(Timestamp dateDelete) {
        this.dateDelete = dateDelete;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
        quantityProp.set(quantity);
    }

    /** Getters for observable properties */
    public LongProperty idProperty() {
        return idProp;
    }

    public LongProperty productCodeProperty() {
        return productCodeProp;
    }

    public StringProperty barCodeProperty() {
        return barCodeProp;
    }

    public StringProperty descriptionProperty() {
        return descriptionProp;
    }

    public ObjectProperty<BigDecimal> costPriceProperty() {
        return costPriceProp;
    }

    public ObjectProperty<BigDecimal> sellingPriceProperty() {
        return sellingPriceProp;
    }

    public ObjectProperty<Optional<Supplier>> supplierProperty() {
        return supplierProp;
    }

    public ObjectProperty<Optional<Category>> categoryProperty() {
        return categoryProp;
    }

    public DoubleProperty profitMarginProperty() {
        return profitMarginProp;
    }

    public DoubleProperty markupPercentProperty() {
        return markupPercentProp;
    }

    public LongProperty quantityProperty() {
        return quantityProp;
    }

    public ObjectProperty<Status> statusProperty() {
        return statusProp;
    }

    public ObjectProperty<Timestamp> dateCreateProperty() {
        return dateCreateProp;
    }

    public ObjectProperty<Timestamp> dateUpdateProperty() {
        return dateUpdateProp;
    }

    public ObjectProperty<Timestamp> dateDeleteProperty() {
        return dateDeleteProp;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
