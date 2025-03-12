package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.service.AbstractTransactionalService;
import com.gabrielosorio.gestor_inteligente.service.base.SalePaymentService;

import java.util.List;

public class SalePaymentServiceImpl extends AbstractTransactionalService<SalePayment> implements SalePaymentService {

    private final Repository<SalePayment> REPOSITORY;

    public SalePaymentServiceImpl(Repository<SalePayment> salePaymentRepository) {
        super(salePaymentRepository);
        REPOSITORY = getRepository();
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


