package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Classe responsável por calcular e formatar as informações de pagamento para exibição
 */
public class PaymentSummaryPresenter {

    /**
     * Calcula o resumo dos pagamentos por método
     */
    public PaymentSummary calculatePaymentSummary(List<CheckoutMovement> movements) {
        BigDecimal pixTotal = BigDecimal.ZERO;
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;

        // Ordena os movimentos por data
        movements.sort(Comparator.comparing(CheckoutMovement::getDateTime));

        BigDecimal lastInitialCash = BigDecimal.ZERO;

        for (CheckoutMovement movement : movements) {
            Payment payment = movement.getPayment();
            if (payment != null && payment.getValue() != null) {
                PaymentMethod method = payment.getPaymentMethod();
                BigDecimal value = payment.getValue();

                boolean isCashAdjustment = movement.getMovementType().getId() == 4;

                if (isCashAdjustment) {
                    // Calcula o delta entre este ajuste e o anterior
                    BigDecimal delta = value.subtract(lastInitialCash);
                    cashTotal = cashTotal.add(delta);
                    lastInitialCash = value;
                } else if (method != null) {
                    switch (method) {
                        case DINHEIRO:
                            cashTotal = cashTotal.add(value);
                            break;
                        case PIX:
                            pixTotal = pixTotal.add(value);
                            break;
                        case DEBITO:
                            debitTotal = debitTotal.add(value);
                            break;
                        case CREDIT0:
                            creditTotal = creditTotal.add(value);
                            break;
                    }
                }
            }
        }

        return new PaymentSummary(pixTotal, cashTotal, debitTotal, creditTotal);
    }
}