package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.SaleProduct;

import java.util.List;

public interface SaleProductService {
    SaleProduct update(SaleProduct saleProduct);
    SaleProduct save(SaleProduct sale);
    List<SaleProduct> saveAll(List<SaleProduct> saleProducts);
    int deleteByIds(List<Long> ids);
    List<SaleProduct> updateAll(List<SaleProduct> saleProducts);

}
