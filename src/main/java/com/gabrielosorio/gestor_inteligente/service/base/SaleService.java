package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.exception.SaleProcessingException;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface SaleService {
    Sale processSale(User user,Sale sale) throws SaleProcessingException;
    Sale save(Sale sale);
    BigDecimal calculateTotalProfit(List<Sale> sales);
    BigDecimal calculateTotalCost(List<Sale> sales);
    BigDecimal calculateTotalSales(List<Sale> sales);

    BigDecimal calculateAverageSale(List<Sale> sales);

    long countSales(List<Sale> sales);
}
