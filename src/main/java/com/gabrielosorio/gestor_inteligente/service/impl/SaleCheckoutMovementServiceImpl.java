package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.Repository;
import com.gabrielosorio.gestor_inteligente.service.AbstractTransactionalService;
import com.gabrielosorio.gestor_inteligente.service.base.SaleCheckoutMovementService;

import java.util.List;

public class SaleCheckoutMovementServiceImpl extends AbstractTransactionalService<SaleCheckoutMovement> implements SaleCheckoutMovementService {

    private final Repository<SaleCheckoutMovement> REPOSITORY;

    public SaleCheckoutMovementServiceImpl(Repository<SaleCheckoutMovement> saleCheckoutMovementRepository) {
        super(saleCheckoutMovementRepository);
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
}
