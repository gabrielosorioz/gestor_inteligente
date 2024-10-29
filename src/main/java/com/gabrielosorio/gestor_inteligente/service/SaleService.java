package com.gabrielosorio.gestor_inteligente.service;
import com.gabrielosorio.gestor_inteligente.exception.SalePaymentException;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.repository.SaleRepository;
import com.gabrielosorio.gestor_inteligente.validation.SaleValidator;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleValidator saleValidator;

    public SaleService(SaleRepository saleRepository){
        this.saleRepository = saleRepository;
        this.saleValidator = new SaleValidator();

    }

    public Sale save(Sale sale) throws SalePaymentException {
        if(saleRepository.find(sale.getId()).isPresent()){
            throw new IllegalArgumentException("Sale already exists.");
        }

        saleValidator.validate(sale);
        processPayment(sale);

        return saleRepository.add(sale);
    }

    private void processPayment (Sale sale) throws SalePaymentException {
        BigDecimal totalAmount = BigDecimal.ZERO;

        var listPayment = new ArrayList<>(sale.getPaymentMethods());

        for (Payment payment : listPayment) {
            totalAmount = totalAmount.add(payment.getValue());
        }

        if (totalAmount.compareTo(sale.getTotalPrice()) < 0) {
            throw new SalePaymentException("Error processing payment: Total amount of payments is less than the sale total price. Total Payments: " + totalAmount + ", Sale Total: " + sale.getTotalPrice());
        }

    }

}
