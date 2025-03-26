package com.gabrielosorio.gestor_inteligente.events.listeners;

import com.gabrielosorio.gestor_inteligente.events.ProductCodeEditAttemptEvent;
import com.gabrielosorio.gestor_inteligente.events.ProductFormCancelEvent;
import com.gabrielosorio.gestor_inteligente.events.ProductFormShortcutEvent;
import com.gabrielosorio.gestor_inteligente.events.ProductFormSaveEvent;

public interface ProductFormListener {
    void onProductCodeEditAttempt(ProductCodeEditAttemptEvent attemptEvent);
    void onSave(ProductFormSaveEvent productFormSaveEvent);
    void onCancel(ProductFormCancelEvent productFormCancelEvent);
    void onHandleShortcutEvent(ProductFormShortcutEvent productFormShortcutEvent);

}
