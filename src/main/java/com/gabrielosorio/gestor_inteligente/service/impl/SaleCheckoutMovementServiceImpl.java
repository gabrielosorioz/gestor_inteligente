package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.SaleCheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.service.SaleCheckoutMovementService;

import java.util.List;

public class SaleCheckoutMovementServiceImpl implements SaleCheckoutMovementService {

    private final SaleCheckoutMovementRepository slCkMovementRepo;

    public SaleCheckoutMovementServiceImpl(SaleCheckoutMovementRepository slCkMovementRepo) {
        this.slCkMovementRepo = slCkMovementRepo;
    }


    @Override
    public List<SaleCheckoutMovement> saveAll(List<SaleCheckoutMovement> saleCheckoutMovements) {
        return slCkMovementRepo.addAll(saleCheckoutMovements);
    }

    @Override
    public SaleCheckoutMovement buildSaleCheckoutMovement(CheckoutMovement checkoutMovement, Sale sale) {
        return new SaleCheckoutMovement(checkoutMovement,sale);
    }
}
