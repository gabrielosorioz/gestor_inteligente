package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.base.SaleCheckoutMovementRepository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindSaleCheckoutMovementsBySaleId;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindSaleDetailsByCheckoutMovementId;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindSaleDetailsBySaleId;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindSalesByCheckoutMovementIds;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import java.util.List;

public class PSQLSaleCheckoutMovementRepository extends Repository<SaleCheckoutMovement,Long> implements SaleCheckoutMovementRepository {
    public PSQLSaleCheckoutMovementRepository(RepositoryStrategy<SaleCheckoutMovement,Long> strategy){
        init(strategy);
    }

    @Override
    public List<SaleCheckoutMovement> findSalesInCheckoutMovements(List<CheckoutMovement> checkoutMovements) {
        List<Long> checkoutMovementIds = checkoutMovements.stream()
                .map(CheckoutMovement::getId)
                .toList();

       return strategy.findBySpecification(new FindSalesByCheckoutMovementIds(checkoutMovementIds));
    }

    @Override
    public List<SaleCheckoutMovement> findSaleDetailsByCheckoutMovement(CheckoutMovement checkoutMovement) {
        return strategy.findBySpecification(
                new FindSaleDetailsByCheckoutMovementId(checkoutMovement.getId())
        );
    }

    @Override
    public List<SaleCheckoutMovement> findSaleDetailsBySaleId(Long saleId) {
        return strategy.findBySpecification(
                new FindSaleDetailsBySaleId(saleId)
        );
    }

    @Override
    public List<SaleCheckoutMovement> findBySaleId(Long saleId) {
        return strategy.findBySpecification(new FindSaleCheckoutMovementsBySaleId(saleId));
    }

    @Override
    public void removeAllBySaleId(Long saleId) {
        var links = findBySaleId(saleId);
        if (links == null || links.isEmpty()) return;

        for (SaleCheckoutMovement link : links) {
            this.remove(link.getId());
        }
    }

}
