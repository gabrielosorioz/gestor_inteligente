package com.gabrielosorio.gestor_inteligente.service.base;


import com.gabrielosorio.gestor_inteligente.model.*;

import java.util.List;

public interface SaleCheckoutMovementService {
    List<SaleCheckoutMovement> saveAll(List<SaleCheckoutMovement> saleCheckoutMovements);
    SaleCheckoutMovement buildSaleCheckoutMovement(CheckoutMovement checkoutMovement, Sale sale);

}
