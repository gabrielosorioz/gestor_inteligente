package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.base.SaleCheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.service.base.SaleCheckoutMovementService;
import java.util.List;
import java.util.Optional;

public class SaleCheckoutMovementServiceImpl implements SaleCheckoutMovementService {

    private final SaleCheckoutMovementRepository REPOSITORY;

    public SaleCheckoutMovementServiceImpl(SaleCheckoutMovementRepository saleCheckoutMovementRepository) {
        REPOSITORY = saleCheckoutMovementRepository;
    }


    @Override
    public List<SaleCheckoutMovement> saveAll(List<SaleCheckoutMovement> saleCheckoutMovements) {
        return REPOSITORY.addAll(saleCheckoutMovements);
    }

    @Override
    public SaleCheckoutMovement buildSaleCheckoutMovement(CheckoutMovement checkoutMovement, Sale sale) {
        return new SaleCheckoutMovement(checkoutMovement,sale);
    }

    @Override
    public List<Sale> findSalesInCheckoutMovements(List<CheckoutMovement> checkoutMovements) {
        var saleCheckoutMovements =  REPOSITORY.findSalesInCheckoutMovements(checkoutMovements);

        return saleCheckoutMovements.stream()
                .map(SaleCheckoutMovement::getSale)
                .toList();
    }

    @Override
    public Optional<Sale> findSaleByCheckoutMovement(CheckoutMovement checkoutMovement) {
        if (checkoutMovement == null) {
            return Optional.empty();
        }

        List<CheckoutMovement> singleMovement = List.of(checkoutMovement);
        List<Sale> sales = findSalesInCheckoutMovements(singleMovement);

        if (sales.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(sales.get(0));
    }
}
