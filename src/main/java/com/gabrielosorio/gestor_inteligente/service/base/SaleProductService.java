package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.SaleProduct;

import java.util.List;

public interface SaleProductService extends TransactionalService {
    SaleProduct save(SaleProduct sale);
    List<SaleProduct> saveAll(List<SaleProduct> saleProducts);
}
