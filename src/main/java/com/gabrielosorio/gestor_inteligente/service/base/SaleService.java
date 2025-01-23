package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.User;

public interface SaleService {
    void processSale(User user,Sale sale);
    Sale save(Sale sale);
}
