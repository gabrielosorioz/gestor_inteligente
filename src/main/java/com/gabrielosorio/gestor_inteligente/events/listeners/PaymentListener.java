package com.gabrielosorio.gestor_inteligente.events.listeners;
import com.gabrielosorio.gestor_inteligente.events.PaymentEvent;

public interface PaymentListener {
    void onPaymentFinalized(PaymentEvent event);
}

