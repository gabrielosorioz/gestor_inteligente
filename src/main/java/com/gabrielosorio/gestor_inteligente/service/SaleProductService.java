package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.strategy.TransactionalStrategy;

import java.util.List;

public interface SaleProductService extends TransactionalService {
    SaleProduct save(SaleProduct sale);
    List<SaleProduct> saveAll(List<SaleProduct> saleProducts);
}
