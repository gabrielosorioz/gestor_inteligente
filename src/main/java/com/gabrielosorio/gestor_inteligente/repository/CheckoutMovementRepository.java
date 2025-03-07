package com.gabrielosorio.gestor_inteligente.repository;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindCheckoutMovementByCheckoutId;
import java.util.List;

public class CheckoutMovementRepository extends Repository<CheckoutMovement>{

    public List<CheckoutMovement> findByCheckoutId(long checkoutId) {
        return findBySpecification(new FindCheckoutMovementByCheckoutId(checkoutId));
    }

}
