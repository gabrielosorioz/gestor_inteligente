package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.SalePayment;

import java.util.List;

public interface SalePaymentService extends TransactionalService {
    void save(SalePayment salePayment);
    List<SalePayment> saveAll(List<SalePayment> salePayments);

}
