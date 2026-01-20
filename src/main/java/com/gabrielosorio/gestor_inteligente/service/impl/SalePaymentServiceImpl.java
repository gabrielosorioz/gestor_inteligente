package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.base.SalePaymentRepository;
import com.gabrielosorio.gestor_inteligente.service.base.SalePaymentService;

import java.util.List;

public class SalePaymentServiceImpl implements SalePaymentService {

    private final SalePaymentRepository REPOSITORY;

    public SalePaymentServiceImpl(SalePaymentRepository salePaymentRepository) {
        REPOSITORY = salePaymentRepository;
    }

    @Override
    public void save(SalePayment salePayment){
        validateSalePayment(salePayment);
        REPOSITORY.add(salePayment);
    }

    @Override
    public List<SalePayment> saveAll(List<SalePayment> salePayments){
        salePayments.forEach(this::validateSalePayment);
        return REPOSITORY.addAll(salePayments);
    }

    @Override
    public List<SalePayment> findBySaleId(Long saleId) {
        return REPOSITORY.findBySaleId(saleId);
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

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return REPOSITORY.deleteAll(ids);
    }

    @Override
    public List<SalePayment> updateAll(List<SalePayment> salePayments) {
        if (salePayments == null || salePayments.isEmpty()) return salePayments;

        salePayments.forEach(this::validateSalePayment);
        return REPOSITORY.updateAll(salePayments);
    }



}


