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
    public Optional<Sale> findSaleDetailsByCheckoutMovement(CheckoutMovement checkoutMovement) {
        if (checkoutMovement == null) {
            return Optional.empty();
        }

        List<SaleCheckoutMovement> scms = REPOSITORY.findSaleDetailsByCheckoutMovement(checkoutMovement);

        if (scms == null || scms.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(scms.get(0).getSale());

    }

    @Override
    public Optional<Sale> findSaleDetailsBySaleId(Long saleId) {
        if (saleId == null) return Optional.empty();

        List<SaleCheckoutMovement> scms = REPOSITORY.findSaleDetailsBySaleId(saleId);

        if (scms == null || scms.isEmpty()) return Optional.empty();

        SaleCheckoutMovement scm = (SaleCheckoutMovement) scms.get(0);
        return Optional.ofNullable(scm.getSale());
    }

    @Override
    public List<SaleCheckoutMovement> findBySaleId(Long saleId) {
        return REPOSITORY.findBySaleId(saleId);
    }

    @Override
    public void removeAllBySaleId(Long saleId) {
        REPOSITORY.removeAllBySaleId(saleId);
    }

}
