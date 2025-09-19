package com.gabrielosorio.gestor_inteligente.events.listeners;

import com.gabrielosorio.gestor_inteligente.events.FindProductEvent;

public interface FindProductListener {
    void onSelectProduct(FindProductEvent findProductEvent);
}
