package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.exception.SaleProcessingException;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.User;

public interface SaleService {
    Sale processSale(User user,Sale sale) throws SaleProcessingException;
    Sale save(Sale sale);
}
