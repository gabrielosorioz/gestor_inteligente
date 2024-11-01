package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.SalePaymentRepository;
import com.gabrielosorio.gestor_inteligente.service.SalePaymentService;

import java.util.List;

public class SalePaymentServiceImpl implements SalePaymentService {

    private final SalePaymentRepository sPaymentRepo;

    public SalePaymentServiceImpl(SalePaymentRepository sPaymentRepo) {
        this.sPaymentRepo = sPaymentRepo;
    }

    @Override
    public void save(SalePayment salePayment){
        validateSalePayment(salePayment);
        sPaymentRepo.add(salePayment);
    }

    @Override
    public List<SalePayment> saveAll(List<SalePayment> salePayments){
        salePayments.forEach(this::validateSalePayment);
        return sPaymentRepo.addAll(salePayments);
    }

    private void validateSalePayment(SalePayment salePayment){
        if (salePayment.getPayment() == null) {
            throw new IllegalArgumentException("The payment method is null.");
        }

        if(salePayment.getPaymentId() <= 0){
            throw new IllegalArgumentException("The payment ID is invalid.");

        }

        if (salePayment.getSale() == null) {
            throw new IllegalArgumentException("The sale is null.");
        }

        if(salePayment.getSaleId() <= 0){
            throw new IllegalArgumentException("The sale ID is invalid.");
        }
    }

}


