package com.gabrielosorio.gestor_inteligente.view.sale;

import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.math.BigDecimal;

public interface SaleTableViewController {

    void add(SaleProduct saleProduct);
    void remove();
    ObjectProperty<BigDecimal> getTotalPriceProperty();
    ObservableList<SaleProduct> getItems();
    void showPaymentScreen();
    void clearItems();

}
