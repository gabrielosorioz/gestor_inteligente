package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.exception.SalePaymentException;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.model.enums.SaleStatus;
import com.gabrielosorio.gestor_inteligente.repository.SaleRepository;
import com.gabrielosorio.gestor_inteligente.service.ProductService;
import com.gabrielosorio.gestor_inteligente.service.SalePaymentService;
import com.gabrielosorio.gestor_inteligente.service.SaleProductService;
import com.gabrielosorio.gestor_inteligente.service.SaleService;
import com.gabrielosorio.gestor_inteligente.validation.SaleValidator;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleProductService saleProductService;
    private final SalePaymentService salePaymentService;
    private final ProductService productService;

    public SaleServiceImpl(SaleRepository saleRepository, SaleProductService saleProductService, SalePaymentService salePaymentService, ProductService productService) {
        this.saleRepository = saleRepository;
        this.saleProductService = saleProductService;
        this.salePaymentService = salePaymentService;
        this.productService = productService;
    }

    @Override
    public void processSale(Sale sale) {
        save(sale);
    }

    public Sale save(Sale sale) throws SalePaymentException {
        SaleValidator.validate(sale);

        if(saleRepository.find(sale.getId()).isPresent()){
            throw new IllegalArgumentException("Sale already exists.");
        }

        validateTotalPayment(sale);
        sale.setStatus(SaleStatus.APPROVED);
        var savedSale = saleRepository.add(sale);
        saveSaleProduct(savedSale);
        saveSalePayment(savedSale);
        return savedSale;
    }

    private void validateTotalPayment (Sale sale) throws SalePaymentException {
        BigDecimal totalAmount = BigDecimal.ZERO;

        var listPayment = new ArrayList<>(sale.getPaymentMethods());

        for (Payment payment : listPayment) {
            totalAmount = totalAmount.add(payment.getValue());
        }

        if (totalAmount.compareTo(sale.getTotalPrice()) < 0) {
            throw new SalePaymentException("Error processing payment: Total amount of payments is less than the sale total price. Total Payments: " + totalAmount + ", Sale Total: " + sale.getTotalPrice());
        }

    }

    private void saveSaleProduct(Sale sale){
        var saleProducts = sale.getItems();
        saleProducts.forEach(saleProduct -> {
            saleProduct.setSale(sale);
            var prod = saleProduct.getProduct();
            var qtd = saleProduct.getQuantity();
            decreaseStockQtd(prod,qtd);
        });
        saleProductService.saveAll(saleProducts);
    }

    private void saveSalePayment(Sale sale){
        var salePayments = sale.getPaymentMethods().stream()
                .map(payment -> new SalePayment(payment,sale))
                .toList();
        salePaymentService.saveAll(salePayments);
    }

    private void decreaseStockQtd(Product product, long quantity){
        productService.decreaseQuantity(product.getId(), quantity);
    }


}
