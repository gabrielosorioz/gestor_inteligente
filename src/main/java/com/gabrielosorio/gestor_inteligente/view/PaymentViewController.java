package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.events.PaymentEvent;
import com.gabrielosorio.gestor_inteligente.events.PaymentEventBus;
import com.gabrielosorio.gestor_inteligente.exception.SaleProcessingException;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.service.base.SaleService;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import static com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils.formatText;

public class PaymentViewController implements Initializable {

    private final Logger log = Logger.getLogger(PaymentViewController.class.getName());

    @FXML private TextField cashField, creditField, debitField, pixField, discountField;
    @FXML private HBox cashHbox, creditHbox, debitHbox, pixHbox;
    @FXML private Button btnCheckout;
    @FXML private Label totalPriceLbl, receiveLbl, paybackLbl, receiveValueLbl, paybackValueLbl,
            monetaryReceiveLbl, monetaryChangeLbl, originalPriceLbl, monetaryOriginalPriceLbl, originalPriceValueLbl;
    @FXML private ComboBox<Integer> installments;

    private final SaleService saleService;
    private final SaleTableViewController saleTableViewOp;
    private final User user;
    private final Map<HBox, PaymentMethod> paymentHboxMap = new HashMap<>();
    private final Map<PaymentMethod, Payment> paymentMethods = new HashMap<>();
    private final Set<TextField> paymentFieldSet = new HashSet<>();
    private final Map<PaymentMethod, TextField> paymentFieldMap = new HashMap<>();

    private Sale sale;

    public PaymentViewController(User user, Sale sale, SaleService saleService, SaleTableViewController saleTableViewOp) {
        validateSale(sale);
        this.sale = sale;
        this.saleService = saleService;
        this.saleTableViewOp = saleTableViewOp;
        this.user = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeView();
        initializePaymentMaps();
        setupPaymentFieldEvents();
        setupInstallmentsComboBox();

        initializePaymentFieldListeners();
        setupHboxEvents();
        setupCheckoutButton();
        setupDiscountField();
        setupInstallmentsComboBox();

        Platform.runLater(() -> simulateMouseClick(cashField));
    }

    private void setupInstallmentsComboBox() {
        installments.getItems().clear();
        for (int i = 1; i <= 12; i++) {
            installments.getItems().add(i);
        }

        installments.setValue(1);

        installments.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer number) {
                if (number == null) {
                    return "1x";
                }
                return number + "x";
            }

            @Override
            public Integer fromString(String string) {
                if (string == null) {
                    return 1;
                }
                return Integer.valueOf(string.replace("x", ""));
            }
        });

        installments.getStylesheets().add(getClass().getResource("/com/gabrielosorio/gestor_inteligente/css/salesReport.css").toExternalForm());

    }

    /**
     * Inicializa os listeners dos campos de pagamento, garantindo que
     * eles já estejam configurados (e com a cor de texto desejada) antes
     * de qualquer interação do usuário.
     */
    private void initializePaymentFieldListeners() {
        paymentFieldMap.forEach((paymentMethod, textField) -> {
            // Registra o listener de formatação e atualização do valor
            requestPayment(textField, paymentMethod);
            // Opcional: Altera a cor do texto para preto, sinalizando que o campo está ativo
            textField.setStyle("-fx-text-fill: black;");
        });
    }

    // Configura os valores iniciais e exibição dos rótulos
    private void initializeView() {
        refreshTotalPrice();
        refreshDiscountPrice();
        showOriginalPrice();
        setupHoverPaymentField();
        hideLabels();
    }

    // Associa cada PaymentMethod ao respectivo TextField e HBox
    private void initializePaymentMaps() {
        paymentFieldMap.put(PaymentMethod.DINHEIRO, cashField);
        paymentFieldMap.put(PaymentMethod.DEBITO, debitField);
        paymentFieldMap.put(PaymentMethod.CREDIT0, creditField);
        paymentFieldMap.put(PaymentMethod.PIX, pixField);

        paymentHboxMap.put(cashHbox, PaymentMethod.DINHEIRO);
        paymentHboxMap.put(debitHbox, PaymentMethod.DEBITO);
        paymentHboxMap.put(creditHbox, PaymentMethod.CREDIT0);
        paymentHboxMap.put(pixHbox, PaymentMethod.PIX);
    }

    // Esconde os rótulos de recebimento e troco inicialmente
    private void hideLabels() {
        receiveLbl.setVisible(false);
        receiveValueLbl.setVisible(false);
        monetaryReceiveLbl.setVisible(false);
        paybackLbl.setVisible(false);
        paybackValueLbl.setVisible(false);
        monetaryChangeLbl.setVisible(false);
    }

    // Configura o botão de finalização da venda
    private void setupCheckoutButton() {
        btnCheckout.setOnMouseClicked(e -> finalizeSale(user, sale));
    }

    // Configura a mudança de estilo ao focar/desfocar os campos monetários
    private void setupHoverPaymentField() {
        addFocusListener(cashField, cashHbox, "#E2EFDD", "#fff");
        addFocusListener(pixField, pixHbox, "#E2EFDD", "transparent");
        addFocusListener(creditField, creditHbox, "#E2EFDD", "transparent");
        addFocusListener(debitField, debitHbox, "#E2EFDD", "transparent");
    }

    private void addFocusListener(TextField field, HBox hbox, String focusStyle, String unfocusStyle) {
        field.focusedProperty().addListener((obs, oldVal, isFocused) ->
                hbox.setStyle(isFocused ? "-fx-background-color: " + focusStyle + ";" : "-fx-background-color: " + unfocusStyle + ";")
        );
    }

    // Configura os eventos dos TextFields, incluindo navegação com as setas e finalização com F2
    private void setupPaymentFieldEvents() {
        List<TextField> paymentFields = Arrays.asList(cashField, pixField, debitField, creditField);

        paymentFieldMap.forEach((paymentMethod, field) -> {
            // Evento de clique: seleciona o campo e posiciona o cursor no final
            field.setOnMouseClicked(e -> {
                requestPayment(field, paymentMethod);
                focusAndMoveCaretToEnd(field);
            });

            // Evento de tecla: F2 para finalizar, setas para navegar entre campos (com loop)
            field.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F2) {
                    finalizeSale(user, sale);
                } else if (e.getCode() == KeyCode.DOWN) {
                    navigateToNextField(paymentFields, field);
                } else if (e.getCode() == KeyCode.UP) {
                    navigateToPreviousField(paymentFields, field);
                }
            });
        });
    }

    // Configura os eventos dos HBox para repassar o clique para o TextField correspondente
    private void setupHboxEvents() {
        paymentHboxMap.forEach((hbox, paymentMethod) -> {
            hbox.setOnMouseClicked(e -> {
                TextField field = paymentFieldMap.get(paymentMethod);
                requestPayment(field, paymentMethod);
                focusAndMoveCaretToEnd(field);
            });
        });
    }

    // Navega para o próximo campo da lista (loop circular)
    private void navigateToNextField(List<TextField> fields, TextField currentField) {
        int index = fields.indexOf(currentField);
        int nextIndex = (index + 1) % fields.size();
        TextField nextField = fields.get(nextIndex);
        focusAndMoveCaretToEnd(nextField);
        PaymentMethod method = getPaymentMethodByField(nextField);
        if (method != null) {
            requestPayment(nextField, method);
        }
    }

    // Navega para o campo anterior da lista (loop circular)
    private void navigateToPreviousField(List<TextField> fields, TextField currentField) {
        int index = fields.indexOf(currentField);
        int previousIndex = (index - 1 + fields.size()) % fields.size();
        TextField previousField = fields.get(previousIndex);
        focusAndMoveCaretToEnd(previousField);
        PaymentMethod method = getPaymentMethodByField(previousField);
        if (method != null) {
            requestPayment(previousField, method);
        }
    }

    // Retorna o PaymentMethod associado a um TextField
    private PaymentMethod getPaymentMethodByField(TextField field) {
        return paymentFieldMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(field))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    // Reposiciona o cursor para o final do campo e solicita o pagamento
    private void focusAndMoveCaretToEnd(TextField field) {
        field.requestFocus();
        field.positionCaret(field.getText().length());
    }

    // Simula um clique no TextField para disparar seus eventos
    private void simulateMouseClick(TextField field) {
        EventHandler<MouseEvent> handler = (EventHandler<MouseEvent>) field.getOnMouseClicked();
        if (handler != null) {
            handler.handle(new MouseEvent(
                    MouseEvent.MOUSE_CLICKED,
                    0, 0, 0, 0,
                    MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true,
                    true, true, true, null
            ));
        }
    }

    // Configura o campo de pagamento para formatação e atualização de valor
    private void requestPayment(TextField field, PaymentMethod method) {
        if (field.getText().isBlank()) {
            field.setText("0,00");
            setPaymentValue(method, "0,00");
        }
        if (!paymentFieldSet.contains(field)) {
            field.textProperty().addListener((obs, oldText, newText) -> {
                String formatted = formatText(newText);
                setPaymentValue(method, formatted);
                if (!newText.equals(formatted)) {
                    Platform.runLater(() -> {
                        field.setText(formatted);
                        focusAndMoveCaretToEnd(field);
                    });
                }
            });
            paymentFieldSet.add(field);
        }
    }

    // Atualiza o valor do pagamento para o método selecionado
    private void setPaymentValue(PaymentMethod method, String value) {
        String formattedValue = formatText(value);
        BigDecimal amount = TextFieldUtils.formatCurrency(formattedValue);
        Payment payment = paymentMethods.getOrDefault(method, new Payment(method));
        payment.setValue(amount);
        paymentMethods.put(method, payment);
        calculateTotalAmountPaid();
    }


    // Calcula e atualiza o total pago
    private void calculateTotalAmountPaid() {
        BigDecimal total = paymentMethods.values().stream()
                .map(Payment::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        sale.setTotalAmountPaid(total);
        refreshValuesLabel();
    }

    // Atualiza os rótulos de recebimento e troco
    private void refreshValuesLabel() {
        BigDecimal receiveValue = sale.getTotalPrice()
                .subtract(sale.getTotalAmountPaid())
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal changeAmount = sale.getTotalChange();

        boolean hasReceive = receiveValue.compareTo(BigDecimal.ZERO) > 0;
        receiveLbl.setVisible(hasReceive);
        receiveValueLbl.setVisible(hasReceive);
        monetaryReceiveLbl.setVisible(hasReceive);
        if (hasReceive) {
            receiveValueLbl.setText(TextFieldUtils.formatText(receiveValue.toPlainString()));
        }

        boolean hasChange = changeAmount.compareTo(BigDecimal.ZERO) > 0;
        paybackLbl.setVisible(hasChange);
        paybackValueLbl.setVisible(hasChange);
        monetaryChangeLbl.setVisible(hasChange);
        if (hasChange) {
            paybackValueLbl.setText(TextFieldUtils.formatText(changeAmount.toPlainString()));
        }
    }

    private void refreshTotalPrice() {
        totalPriceLbl.setText(TextFieldUtils.formatText(sale.getTotalPrice().toPlainString()));
    }

    private void refreshDiscountPrice() {
        discountField.setText(TextFieldUtils.formatText(sale.getTotalDiscount().toPlainString()));
    }

    private void showOriginalPrice() {
        boolean hasDiscount = sale.getTotalDiscount().compareTo(BigDecimal.ZERO) > 0;
        originalPriceLbl.setVisible(hasDiscount);
        monetaryOriginalPriceLbl.setVisible(hasDiscount);
        originalPriceValueLbl.setVisible(hasDiscount);
        if (hasDiscount) {
            originalPriceValueLbl.setText(TextFieldUtils.formatText(sale.getOriginalTotalPrice().toPlainString()));
        }
    }

    // Configura o campo de desconto com formatação e posicionamento do cursor
    private void setupDiscountField() {
        if (discountField.getText().isBlank()) {
            discountField.setText("0,00");
        } else {
            discountField.setText(formatText(discountField.getText()));
        }
        discountField.setOnKeyPressed(e -> {
            if (e.getCode().isArrowKey()) {
                discountField.positionCaret(discountField.getText().length());
            }
        });
        discountField.setOnMouseClicked(e -> discountField.positionCaret(discountField.getText().length()));
        discountField.textProperty().addListener((obs, oldVal, newVal) -> {
            String formatted = formatText(newVal);
            if (!newVal.equals(formatted)) {
                Platform.runLater(() -> {
                    discountField.setText(formatted);
                    discountField.positionCaret(formatted.length());
                    sale.setTotalDiscount(TextFieldUtils.formatCurrency(formatted));
                    refreshValuesLabel();
                    refreshTotalPrice();
                    showOriginalPrice();
                });
            }
        });
    }

    // Finaliza a venda, processa os pagamentos e fecha a janela
    private void finalizeSale(User user, Sale sale) {
        confirmPayment(sale);
        try {
            var savedSale = saleService.processSale(user, sale);
            PaymentEvent paymentEvent = new PaymentEvent(savedSale);
            PaymentEventBus.getInstance().publish(paymentEvent);
        } catch (SaleProcessingException e) {
            throw new RuntimeException(e);
        }
        closeWindow();
        clearItems();
    }

    // Confirma os pagamentos, removendo os de valor zero
    private void confirmPayment(Sale sale) {
        List<Payment> payments = new ArrayList<>(new HashSet<>(paymentMethods.values()));
        payments.forEach(payment -> {
            if(payment.getPaymentMethod().equals(PaymentMethod.CREDIT0)){
                payment.setInstallments(installments.getValue());
            }
        });
        payments.removeIf(payment -> payment.getValue().compareTo(BigDecimal.ZERO) == 0);
        sale.setPaymentMethods(payments);
    }

    private void closeWindow() {
        Stage stage = (Stage) totalPriceLbl.getScene().getWindow();
        stage.close();
    }

    private void clearItems() {
        saleTableViewOp.clearItems();
    }

    // Valida se a venda está correta
    private void validateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("Erro ao iniciar Payment View Controller: Sale é nula");
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new IllegalArgumentException("Erro ao iniciar Payment View Controller: Itens da venda são nulos ou vazios");
        }
        if (sale.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Erro ao iniciar Payment View Controller: Preço total é <= 0");
        }
    }
}
