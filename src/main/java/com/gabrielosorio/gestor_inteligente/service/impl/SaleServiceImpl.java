package com.gabrielosorio.gestor_inteligente.service.impl;
import com.gabrielosorio.gestor_inteligente.exception.SalePaymentException;
import com.gabrielosorio.gestor_inteligente.exception.TransactionException;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.model.enums.SaleStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;
import com.gabrielosorio.gestor_inteligente.repository.SaleRepository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionManager;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionManagerImpl;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalStrategy;
import com.gabrielosorio.gestor_inteligente.service.base.*;
import com.gabrielosorio.gestor_inteligente.validation.SaleValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleProductService saleProductService;
    private final SalePaymentService salePaymentService;
    private final CheckoutMovementService checkoutMovementService;
    private final CheckoutService checkoutService;
    private final ProductService productService;
    private final SaleCheckoutMovementService saleCheckoutMovementService;

    public SaleServiceImpl(SaleRepository saleRepository, SaleProductService saleProductService, SalePaymentService salePaymentService, CheckoutMovementService checkoutMovementService, CheckoutService checkoutService, ProductService productService, SaleCheckoutMovementService saleCheckoutMovementService) {
        this.saleRepository = saleRepository;
        this.saleProductService = saleProductService;
        this.salePaymentService = salePaymentService;
        this.checkoutMovementService = checkoutMovementService;
        this.checkoutService = checkoutService;
        this.productService = productService;
        this.saleCheckoutMovementService = saleCheckoutMovementService;
    }

    @Override
    public void processSale(User user, Sale sale) {
        var checkout = checkoutService.openCheckout(user);

        List<TransactionalStrategy<?>> transactionalStrategies = List.of(
                saleRepository.getTransactionalStrategy(),
                saleProductService.getTransactionalStrategy(),
                salePaymentService.getTransactionalStrategy(),
                checkoutMovementService.getTransactionalStrategy(),
                productService.getTransactionalStrategy(),
                saleCheckoutMovementService.getTransactionalStrategy()
        );

        TransactionManager transactionManager = new TransactionManagerImpl(transactionalStrategies);

        try {
            transactionManager.beginTransaction();

            save(sale);

            var checkoutMovementType = new CheckoutMovementType(CheckoutMovementTypeEnum.VENDA);

            String saleObservation = sale.getPaymentMethods().size() > 1
                    ? "Venda #" + sale.getId() + " - Pagamento dividido"
                    : "Venda #" + sale.getId();

            List<CheckoutMovement> checkoutMovements = sale.getPaymentMethods().stream()
                    .map(paymentMethod -> checkoutMovementService
                            .buildCheckoutMovement(checkout, paymentMethod, saleObservation, checkoutMovementType))
                    .toList();

            var checkoutMovementsWithGenKeys = checkoutMovementService.saveAll(checkoutMovements);

            List<SaleCheckoutMovement> saleCheckoutMovements = checkoutMovementsWithGenKeys.stream()
                    .map(checkoutMovement -> saleCheckoutMovementService
                            .buildSaleCheckoutMovement(checkoutMovement, sale))
                    .toList();

            saleCheckoutMovementService.saveAll(saleCheckoutMovements);

            transactionManager.commitTransaction();
        } catch (Exception e) {
            try {
                transactionManager.rollbackTransaction();
            } catch (TransactionException rollbackEx) {
                throw new RuntimeException("Failed to rollback transaction after error.", rollbackEx);
            }
            throw new RuntimeException("Failed to process sale.", e);
        }
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
