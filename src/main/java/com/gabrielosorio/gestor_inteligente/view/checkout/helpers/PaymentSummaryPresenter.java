package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PaymentSummaryPresenter {

    public PaymentSummary calculatePaymentSummary(List<CheckoutMovement> movements) {
        if (movements.isEmpty()) {
            return new PaymentSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Agrupar por data
        Map<LocalDate, List<CheckoutMovement>> byDate = movements.stream()
                .collect(Collectors.groupingBy(m -> m.getDateTime().toLocalDate()));

        // Identificar o último dia
        LocalDate lastDay = byDate.keySet().stream()
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("Data inválida"));

        // Calcular TOTAIS GERAIS (todos os dias) — para relatórios
        BigDecimal pixTotal = BigDecimal.ZERO;
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;
        BigDecimal cashTotal = BigDecimal.ZERO; // ← será substituído pelo saldo do último dia

        for (CheckoutMovement m : movements) {
            Long tid = m.getMovementType().getId();
            Payment p = m.getPayment();
            Long pid = p != null ? p.getId() : null;
            BigDecimal v = m.getValue();

            if (pid != null) {
                BigDecimal amount = tid == CheckoutMovementTypeEnum.SAIDA.getId() ? v.negate() : v;
                switch (pid.intValue()) {
                    case 1 -> pixTotal = pixTotal.add(amount);
                    case 2 -> debitTotal = debitTotal.add(amount);
                    case 3 -> creditTotal = creditTotal.add(amount);
                    // case 4 → não soma aqui! Será calculado só para o último dia
                }
            }
        }

        cashTotal = calculateCashBalanceForDay(byDate.get(lastDay));

        return new PaymentSummary(pixTotal, cashTotal, debitTotal, creditTotal);
    }

    private BigDecimal calculateCashBalanceForDay(List<CheckoutMovement> dayMovements) {
        if (dayMovements == null) return BigDecimal.ZERO;
        BigDecimal balance = BigDecimal.ZERO;

        for (CheckoutMovement m : dayMovements) {
            Long tid = m.getMovementType().getId();
            Payment p = m.getPayment();
            Long pid = p != null ? p.getId() : null;
            BigDecimal v = m.getValue();
            boolean isCash = pid != null && pid == 4L;

            if (!isCash) continue;

            if (tid == CheckoutMovementTypeEnum.FUNDO_DE_CAIXA.getId() ||
                    tid == CheckoutMovementTypeEnum.AJUSTE_POSITIVO.getId() ||
                    tid == CheckoutMovementTypeEnum.VENDA.getId() ||
                    tid == CheckoutMovementTypeEnum.ENTRADA.getId()) {
                balance = balance.add(v);
            } else if (tid == CheckoutMovementTypeEnum.AJUSTE_NEGATIVO.getId() ||
                    tid == CheckoutMovementTypeEnum.SAIDA.getId()) {
                balance = balance.subtract(v);
            }
        }

        return balance;
    }
}