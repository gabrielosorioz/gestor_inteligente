package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;
import java.math.BigDecimal;

public class PaymentSummary {
    private final BigDecimal pixTotal;
    private final BigDecimal cashTotal;
    private final BigDecimal debitTotal;
    private final BigDecimal creditTotal;

    public PaymentSummary(BigDecimal pixTotal, BigDecimal cashTotal, BigDecimal debitTotal, BigDecimal creditTotal) {
        this.pixTotal = pixTotal;
        this.cashTotal = cashTotal;
        this.debitTotal = debitTotal;
        this.creditTotal = creditTotal;
    }

    public BigDecimal getPixTotal() {
        return pixTotal;
    }

    public BigDecimal getCashTotal() {
        return cashTotal;
    }

    public BigDecimal getDebitTotal() {
        return debitTotal;
    }

    public BigDecimal getCreditTotal() {
        return creditTotal;
    }
}