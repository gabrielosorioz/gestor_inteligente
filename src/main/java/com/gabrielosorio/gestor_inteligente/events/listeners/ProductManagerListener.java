package com.gabrielosorio.gestor_inteligente.events.listeners;
import com.gabrielosorio.gestor_inteligente.events.ProductAddEvent;
import com.gabrielosorio.gestor_inteligente.events.ProductFormToggleEvent;
import com.gabrielosorio.gestor_inteligente.events.ProductSelectionEvent;

public interface ProductManagerListener {
    void onSelectProduct(ProductSelectionEvent productSelectionEvent);
    void onToggleProductForm(ProductFormToggleEvent productFormToggleEvent);
    void onAddNewProduct(ProductAddEvent productAddEvent);
    void onSaveProduct(ProductManagerSaveEvent productManagerSaveEvent);
    void onCancel(ProductManagerCancelEvent productManagerCancelEvent);
}
