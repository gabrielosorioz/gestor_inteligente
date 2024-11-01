package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;

public interface SaleService {
    void processSale(Sale sale);
    Sale save(Sale sale);
}
