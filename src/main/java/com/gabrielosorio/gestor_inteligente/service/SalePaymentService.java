package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.SalePaymentRepository;

public class SalePaymentService {

    private final SalePaymentRepository sPaymentRepo;

    public SalePaymentService(SalePaymentRepository sPaymentRepo) {
        this.sPaymentRepo = sPaymentRepo;
    }

    public void save(SalePayment salePayment){
        if(salePayment.getPayment() == null){
            throw new IllegalArgumentException("The payment method is null.");
        }
        if(salePayment.getSale() == null){
            throw new IllegalArgumentException("The sale is null.");
        }

        sPaymentRepo.add(salePayment);

    }
}
