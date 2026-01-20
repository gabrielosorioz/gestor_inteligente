package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;

import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;

/**
 * Gerencia os campos de pagamento e sincronização com o modelo Sale.
 */
public class PaymentFieldsManager {

    private final EnumMap<PaymentMethod, Payment> paymentByMethod;
    private boolean isUpdating = false;

    private TextField cashField;
    private TextField debitField;
    private TextField creditField;
    private TextField pixField;
    private ComboBox<Integer> creditInstallments;

    public PaymentFieldsManager() {
        this.paymentByMethod = new EnumMap<>(PaymentMethod.class);
    }

    /**
     * Configura as referências dos campos de pagamento.
     */
    public void setFields(TextField cashField,
                          TextField debitField,
                          TextField creditField,
                          TextField pixField,
                          ComboBox<Integer> creditInstallments) {
        this.cashField = cashField;
        this.debitField = debitField;
        this.creditField = creditField;
        this.pixField = pixField;
        this.creditInstallments = creditInstallments;
    }

    /**
     * Vincula um campo de pagamento a um método de pagamento.
     */
    public void bindField(TextField field, PaymentMethod method, Sale currentSale) {
        if (field == null) return;

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating || currentSale == null) return;

            BigDecimal amount = parseCurrency(field);
            Payment payment = paymentByMethod.computeIfAbsent(method, Payment::new);
            payment.setValue(amount.setScale(2, RoundingMode.HALF_UP));

            // Atualiza parcelas se for crédito
            if (method == PaymentMethod.CREDIT0 && creditInstallments != null) {
                updateInstallments(payment);
            }

            currentSale.setPaymentMethods(new ArrayList<>(paymentByMethod.values()));
        });
    }

    /**
     * Carrega métodos de pagamento nos campos.
     */
    public void loadPayments(Collection<Payment> payments) {
        isUpdating = true;
        try {
            paymentByMethod.clear();
            clearAllFields();

            if (payments == null || payments.isEmpty()) {
                return;
            }

            for (Payment payment : payments) {
                if (payment == null || payment.getPaymentMethod() == null) continue;

                paymentByMethod.put(payment.getPaymentMethod(), payment);
                updateFieldValue(payment);
            }

            updateInstallmentsVisibility();

        } finally {
            Platform.runLater(() -> isUpdating = false);
        }
    }

    /**
     * Calcula o total pago somando todos os campos.
     */
    public BigDecimal calculateTotalPaid() {
        BigDecimal total = BigDecimal.ZERO;

        total = total.add(getFieldValue(cashField));
        total = total.add(getFieldValue(debitField));
        total = total.add(getFieldValue(creditField));
        total = total.add(getFieldValue(pixField));

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retorna os pagamentos atuais.
     */
    public Collection<Payment> getPayments() {
        return new ArrayList<>(paymentByMethod.values());
    }

    // ===== Métodos Privados =====

    private void clearAllFields() {
        setFieldText(cashField, "0,00");
        setFieldText(debitField, "0,00");
        setFieldText(creditField, "0,00");
        setFieldText(pixField, "0,00");
    }

    private void updateFieldValue(Payment payment) {
        BigDecimal value = payment.getValue() != null ?
                payment.getValue() : BigDecimal.ZERO;
        String formatted = TextFieldUtils.formatText(
                value.setScale(2, RoundingMode.HALF_UP).toPlainString()
        );

        switch (payment.getPaymentMethod()) {
            case DINHEIRO -> setFieldText(cashField, formatted);
            case DEBITO -> setFieldText(debitField, formatted);
            case CREDIT0 -> setFieldText(creditField, formatted);
            case PIX -> setFieldText(pixField, formatted);
        }
    }

    private void updateInstallments(Payment payment) {
        if (creditInstallments == null || creditInstallments.getValue() == null) return;

        try {
            int installments = creditInstallments.getValue();
            payment.setInstallments(Math.max(1, installments));
        } catch (Exception ignored) {
            payment.setInstallments(1);
        }
    }

    private void updateInstallmentsVisibility() {
        if (creditInstallments == null) return;

        Payment credit = paymentByMethod.get(PaymentMethod.CREDIT0);
        boolean hasCredit = credit != null && credit.getValue() != null &&
                credit.getValue().compareTo(BigDecimal.ZERO) > 0;

        creditInstallments.setVisible(hasCredit);
        creditInstallments.setManaged(hasCredit);
        creditInstallments.setDisable(!hasCredit);

        if (hasCredit) {
            int installments = Math.max(1, credit.getInstallments());
            creditInstallments.setValue(installments);
        }
    }

    private BigDecimal parseCurrency(TextField field) {
        try {
            String raw = field.getText();
            if (raw == null || raw.isBlank()) {
                return BigDecimal.ZERO;
            }
            return TextFieldUtils.formatCurrency(raw);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getFieldValue(TextField field) {
        if (field == null) return BigDecimal.ZERO;
        return parseCurrency(field);
    }

    private void setFieldText(TextField field, String text) {
        if (field != null) {
            field.setText(text);
        }
    }
}
