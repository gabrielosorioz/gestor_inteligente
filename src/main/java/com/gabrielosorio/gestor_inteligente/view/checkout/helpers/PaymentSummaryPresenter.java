package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;
import java.math.BigDecimal;
import java.util.List;

/**
 * Classe responsável por calcular e formatar as informações de pagamento para exibição
 */
public class PaymentSummaryPresenter {

    public PaymentSummary calculatePaymentSummary(List<CheckoutMovement> movements) {
        BigDecimal pixTotal = BigDecimal.ZERO;
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;

        for (CheckoutMovement movement : movements) {
            Payment payment = movement.getPayment();
            BigDecimal amount = payment.getValue();

            if (movement.getMovementType().getName().equalsIgnoreCase(CheckoutMovementTypeEnum.SAIDA.getName())) {
                amount = amount.negate();
            }

            switch (payment.getPaymentMethod()) {
                case PIX -> pixTotal = pixTotal.add(amount);
                case DINHEIRO -> cashTotal = cashTotal.add(amount);
                case DEBITO -> debitTotal = debitTotal.add(amount);
                case CREDIT0 -> creditTotal = creditTotal.add(amount);
            }
        }

        return new PaymentSummary(pixTotal, cashTotal, debitTotal, creditTotal);
    }
}