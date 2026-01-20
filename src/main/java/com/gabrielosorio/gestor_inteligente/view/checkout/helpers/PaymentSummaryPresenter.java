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
                BigDecimal amount = BigDecimal.ZERO;
                long typeId = movementType.getId();

                // Lógica para Meios Eletrônicos (PIX, Crédito, Débito)
                // VENDA: Soma
                if (typeId == CheckoutMovementTypeEnum.VENDA.getId()) {
                    amount = v;
                }
                // ESTORNO DE VENDA: Subtrai
                else if (typeId == CheckoutMovementTypeEnum.ESTORNO.getId()) {
                    amount = v.negate();
                }
                // ESTORNO DE TROCO: N/A para eletrônicos (geralmente troco é só dinheiro)
                // TROCO: N/A para eletrônicos

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

            // Verifica se é DINHEIRO (ID 4)
            if (p != null && p.getId() == 4L && movementType != null) {
                BigDecimal v = m.getValue();
                long movementTypeId = movementType.getId();

                // Lógica de Fundo de Caixa (mantida)
                if (movementTypeId == CheckoutMovementTypeEnum.FUNDO_DE_CAIXA.getId()) {
                    if (lastFundoDateTime == null || m.getDateTime().isAfter(lastFundoDateTime)) {
                        lastFundoDateTime = m.getDateTime();
                        lastFundoDeCaixa = v;
                    }
                    continue;
                }

                // Aplica Soma ou Subtração baseado no Tipo
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
                movementTypeId == CheckoutMovementTypeEnum.VENDA.getId() ||            // Venda entra dinheiro
                movementTypeId == CheckoutMovementTypeEnum.AJUSTE_POSITIVO.getId() ||
                movementTypeId == CheckoutMovementTypeEnum.ESTORNO_TROCO.getId();      // Estorno de troco = Devolve dinheiro pro caixa
    }

    private boolean isNegativeCashMovement(long movementTypeId) {
        return movementTypeId == CheckoutMovementTypeEnum.SAIDA.getId() ||
                movementTypeId == CheckoutMovementTypeEnum.AJUSTE_NEGATIVO.getId() ||
                movementTypeId == CheckoutMovementTypeEnum.ESTORNO.getId() ||          // Estorno de venda = Sai dinheiro (devolve pro cliente)
                movementTypeId == CheckoutMovementTypeEnum.TROCO.getId();              // Troco sai dinheiro
    }


}