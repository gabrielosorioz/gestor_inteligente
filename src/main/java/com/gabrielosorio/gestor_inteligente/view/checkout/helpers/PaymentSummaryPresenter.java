package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaymentSummaryPresenter {

    public PaymentSummary calculatePaymentSummary(List<CheckoutMovement> movements) {
        if (movements.isEmpty()) {
            return new PaymentSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Agrupar por data para identificar o último dia
        Map<LocalDate, List<CheckoutMovement>> byDate = movements.stream()
                .collect(Collectors.groupingBy(m -> m.getDateTime().toLocalDate()));

        // Identificar o último dia
        LocalDate lastDay = byDate.keySet().stream()
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("Data inválida"));

        BigDecimal pixTotal = BigDecimal.ZERO;
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;

        for (CheckoutMovement m : movements) {
            CheckoutMovementType movementType = m.getMovementType();
            Payment p = m.getPayment();
            Long pid = p != null ? p.getId() : null;
            BigDecimal v = m.getValue();

            if (pid != null && movementType != null) {
                BigDecimal amount;
                // Lógica para PIX, Crédito e Débito: somente vendas
                if (movementType.getId() == CheckoutMovementTypeEnum.VENDA.getId()) {
                    amount = v;
                } else {
                    amount = BigDecimal.ZERO;
                }

                if (pid == 1L) { // PIX
                    pixTotal = pixTotal.add(amount);
                } else if (pid == 2L) { // DÉBITO
                    debitTotal = debitTotal.add(amount);
                } else if (pid == 3L) { // CRÉDITO
                    creditTotal = creditTotal.add(amount);
                }
            }
        }

        BigDecimal cashTotal = calculateCashTotalWithBusinessRules(movements, lastDay, byDate);

        return new PaymentSummary(pixTotal, cashTotal, debitTotal, creditTotal);
    }

    private BigDecimal calculateCashTotalWithBusinessRules(
            List<CheckoutMovement> allMovements,
            LocalDate lastDay,
            Map<LocalDate, List<CheckoutMovement>> byDate) {

        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal lastFundoDeCaixa = BigDecimal.ZERO;
        LocalDateTime lastFundoDateTime = null;

        for (CheckoutMovement m : allMovements) {
            CheckoutMovementType movementType = m.getMovementType();
            Payment p = m.getPayment();

            if (p != null && p.getId() == 4L && movementType != null) {
                BigDecimal v = m.getValue();
                long movementTypeId = movementType.getId();

                if (movementTypeId == CheckoutMovementTypeEnum.FUNDO_DE_CAIXA.getId()) {
                    if (lastFundoDateTime == null || m.getDateTime().isAfter(lastFundoDateTime)) {
                        lastFundoDateTime = m.getDateTime();
                        lastFundoDeCaixa = v;
                    }
                    continue;
                }

                if (isPositiveCashMovement(movementTypeId)) {
                    cashTotal = cashTotal.add(v);
                } else if (isNegativeCashMovement(movementTypeId)) {
                    cashTotal = cashTotal.subtract(v);
                }
            }
        }
        cashTotal = cashTotal.add(lastFundoDeCaixa);
        return cashTotal;
    }

    private boolean isPositiveCashMovement(long movementTypeId) {
        return movementTypeId == CheckoutMovementTypeEnum.ENTRADA.getId() ||
                movementTypeId == CheckoutMovementTypeEnum.VENDA.getId() ||
                movementTypeId == CheckoutMovementTypeEnum.AJUSTE_POSITIVO.getId();
    }

    private boolean isNegativeCashMovement(long movementTypeId) {
        return movementTypeId == CheckoutMovementTypeEnum.SAIDA.getId() ||
                movementTypeId == CheckoutMovementTypeEnum.AJUSTE_NEGATIVO.getId();
    }
}