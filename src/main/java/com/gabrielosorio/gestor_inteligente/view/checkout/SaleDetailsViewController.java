package com.gabrielosorio.gestor_inteligente.view.checkout;
import com.gabrielosorio.gestor_inteligente.events.SaleUpdatedEvent;
import com.gabrielosorio.gestor_inteligente.events.SaleUpdatedEventBus;
import com.gabrielosorio.gestor_inteligente.exception.SalePaymentException;
import com.gabrielosorio.gestor_inteligente.exception.SaleProcessingException;
import com.gabrielosorio.gestor_inteligente.exception.SaleValidationException;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.service.base.NotificationService;
import com.gabrielosorio.gestor_inteligente.service.base.SaleService;

import com.gabrielosorio.gestor_inteligente.service.impl.NotificationServiceImpl;
import com.gabrielosorio.gestor_inteligente.view.shared.AlertMessageController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import com.gabrielosorio.gestor_inteligente.view.checkout.helpers.SaleProductTablePresenter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SaleDetailsViewController implements Initializable {

    @FXML
    private ImageView btnClose;

    @FXML
    private Button btnSave;

    @FXML
    private TextField cashMethodField,creditMethodField,debitMethodField,
            pixMethodField,saleDiscountField;

    @FXML
    private Label saleDate,saleDateTime,saleIdLbl,subtotalLbl,
    totalDiscountLbl,totalChangeLbl,clientName;

    @FXML
    private VBox tableContainer;

    @FXML
    private ComboBox<Integer> creditInstallments;



    private final SaleProductTablePresenter saleProductTablePresenter = new SaleProductTablePresenter();
    private TableView<SaleProduct> saleProductsTable;
    private Sale currentSale;
    private Checkout currentCheckout;
    private final EnumMap<PaymentMethod, Payment> paymentByMethod = new EnumMap<>(PaymentMethod.class);
    private boolean populatingPaymentFields = false;
    private final SaleService saleService;
    private final User user;
    private final NotificationService notificationService;
    private boolean populatingSaleData = false;
    private Runnable onCloseAction;
    private AnchorPane overlayHost;


    public SaleDetailsViewController(SaleService saleService, User user) {
        this.saleService = saleService;
        this.user = user;
        this.notificationService = new NotificationServiceImpl();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (tableContainer == null) return;

        // Modificação: Adicionado verificação !populatingSaleData
        saleProductTablePresenter.setAfterModelUpdateCallback((item, columnId, newValue) -> {
            if (!populatingSaleData) refreshSubtotals();
        });

        // Modificação: Adicionado verificação !populatingSaleData
        saleProductTablePresenter.setOnItemsChanged(() -> {
            if (!populatingSaleData) refreshSubtotals();
        });

        saleProductsTable = saleProductTablePresenter.createTableView();
        tableContainer.getChildren().clear();
        saleProductsTable.setMaxWidth(Double.MAX_VALUE);
        saleProductsTable.setMaxHeight(Double.MAX_VALUE);
        tableContainer.getChildren().add(saleProductsTable);

        if (saleDiscountField != null) {
            priceListener(saleDiscountField);
            // Modificação: Adicionado verificação !populatingSaleData
            saleDiscountField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (!populatingSaleData) refreshSubtotals();
            });
        }

        setupInstallmentsComboBox();
        setupPaymentFields();

        if (btnSave != null) {
            btnSave.setOnAction(e -> handleSave());
        }
        setupBtnClose();
    }


    private void handleSave(){
        confirmBeforeSave();
    }

    public void setOnClose(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }

    private void setupBtnClose(){
        btnClose.setStyle("-fx-cursor: hand;");
        btnClose.setOnMouseClicked(e -> {
            if(onCloseAction != null){
                onCloseAction.run();
            }
        });
    }


    public void save() {
        try {
            Sale savedSale = validateAndUpdateSale();

            // opcional: manter o model atualizado
            this.currentSale = savedSale;
            notificationService.showSuccess("Venda salva com sucesso!");
            SaleUpdatedEventBus.getInstance().publish(new SaleUpdatedEvent(savedSale));

            // opcional: feedback de sucesso (se quiser, posso te passar um Alert/Toast de sucesso)
        } catch (SaleProcessingException | SaleValidationException | SalePaymentException e) {
            showError("Erro ao salvar venda", e.getMessage());
        } catch (Exception e) {
            showError("Erro inesperado", "Ocorreu um erro inesperado ao salvar a venda.");
        }
    }

    public void setOverlayHost(AnchorPane overlayHost) {
        this.overlayHost = overlayHost;
    }

    private void confirmBeforeSave() {
        AnchorPane host = overlayHost != null
                ? overlayHost
                : (AnchorPane) btnSave.getScene().getRoot();

        Node dialog = loadEditSaleWarningDialog();
        if (dialog != null && !host.getChildren().contains(dialog)) {
            host.getChildren().add(dialog);
        }
    }

    private Node loadEditSaleWarningDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/com/gabrielosorio/gestor_inteligente/fxml/AlertMessage.fxml")
            );

            AlertMessageController alertController = new AlertMessageController();
            fxmlLoader.setController(alertController);

            Node alertNode = fxmlLoader.load();

            alertController.setText(
                    "Tem certeza que deseja editar a venda?\nEssa ação é irreversível."
            );

            alertController.setOnYesAction(v -> save());

            // opcional: posicionamento (ajuste conforme seu layout)
            alertNode.setLayoutX(450);
            alertNode.setLayoutY(250);

            return alertNode;

        } catch (Exception e) {
            showError("Erro ao abrir confirmação", "Não foi possível exibir a confirmação de edição.");
            return null;
        }
    }



    private void setupPaymentFields() {
        bindPaymentField(cashMethodField, PaymentMethod.DINHEIRO);
        bindPaymentField(debitMethodField, PaymentMethod.DEBITO);
        bindPaymentField(creditMethodField, PaymentMethod.CREDIT0);
        bindPaymentField(pixMethodField, PaymentMethod.PIX);
    }

    private void bindPaymentField(TextField field, PaymentMethod method) {
        if (field == null) return;

        priceListener(field);

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (populatingPaymentFields) return;
            if (currentSale == null) return;

            BigDecimal amount = parseCurrency(field);

            Payment pay = paymentByMethod.computeIfAbsent(method, Payment::new);
            pay.setValue(amount.setScale(2, RoundingMode.HALF_UP));


            if (method == PaymentMethod.CREDIT0 && creditInstallments != null && creditInstallments.getValue() != null) {
                try {
                    int inst = (int) creditInstallments.getValue();
                    pay.setInstallments(Math.max(1, inst));
                } catch (Exception ignored) {
                }
            }

            currentSale.setPaymentMethods(new ArrayList<>(paymentByMethod.values()));
            recalcTotalAmountPaidAndChange();
            System.out.println("[SaleDetails] payments(updated) => " + currentSale.getPaymentMethods());
        });


    }

    private BigDecimal parseCurrency(TextField field) {
        try {
            String raw = field.getText();
            if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
            return com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils.formatCurrency(raw);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }




    public void setSale(Sale sale) {
        if (sale == null) return;
        this.currentSale = sale;

        // Bloqueia atualizações de UI e listeners
        populatingSaleData = true;

        try {
            // 1. ORDEM CORRIGIDA: Definir o desconto ANTES dos produtos
            // Isso garante que o campo já tenha o valor "9,00" quando o cálculo for executado
            if (saleDiscountField != null) {
                BigDecimal disc = sale.getSaleDiscount() == null ? BigDecimal.ZERO : sale.getSaleDiscount();
                saleDiscountField.setText(
                        com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                                .formatText(disc.setScale(2, RoundingMode.HALF_UP).toPlainString())
                );
            }

            // 2. Produtos
            setProducts(sale.getSaleProducts());

            // 3. Métodos de pagamento
            setPaymentMethods(sale.getPaymentMethods());

        } finally {
            // Libera para atualizações normais
            populatingSaleData = false;
        }

        // 4. Força uma atualização única e limpa com todos os dados carregados
        refreshSubtotals();
        recalcTotalAmountPaidAndChange();

        // Logs para conferência (pode manter ou remover)
        System.out.println("========== Sale Details Loaded ==========");
        System.out.println("Total Esperado: " + sale.getTotalPrice());
        System.out.println("Desconto Venda: " + sale.getSaleDiscount());
    }


    public void setPaymentMethods(java.util.Collection payments) {

        populatingPaymentFields = true;
        try {
            paymentByMethod.clear();

            if (cashMethodField != null) cashMethodField.setText("0,00");
            if (debitMethodField != null) debitMethodField.setText("0,00");
            if (creditMethodField != null) creditMethodField.setText("0,00");
            if (pixMethodField != null) pixMethodField.setText("0,00");

            if (payments == null || payments.isEmpty()) {
                return;
            }

            for (var obj : payments) {
                if (obj == null) continue;

                Payment p = (Payment) obj;
                if (p.getPaymentMethod() == null) continue;

                paymentByMethod.put(p.getPaymentMethod(), p);

                BigDecimal value = p.getValue() == null ? BigDecimal.ZERO : p.getValue();
                String formatted = com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                        .formatText(value.setScale(2, RoundingMode.HALF_UP).toPlainString());

                switch (p.getPaymentMethod()) {
                    case DINHEIRO -> { if (cashMethodField != null) cashMethodField.setText(formatted); }
                    case DEBITO -> { if (debitMethodField != null) debitMethodField.setText(formatted); }
                    case CREDIT0 -> { if (creditMethodField != null) creditMethodField.setText(formatted); }
                    case PIX -> { if (pixMethodField != null) pixMethodField.setText(formatted); }
                }
            }

            // mantém seu bloco de installments (mas agora pode usar paymentByMethod)
            if (creditInstallments != null) {
                Payment credit = paymentByMethod.get(PaymentMethod.CREDIT0);

                boolean hasCredit = credit != null;
                int inst = hasCredit ? Math.max(1, credit.getInstallments()) : 1;

                creditInstallments.setVisible(hasCredit);
                creditInstallments.setManaged(hasCredit);
                creditInstallments.setDisable(true);

                if (hasCredit) {
                    creditInstallments.setValue(inst);
                }
            }

            System.out.println("[SaleDetails] payments(loaded) => " + payments);

        } finally {
            // solta a flag depois do JavaFX aplicar updates de texto
            Platform.runLater(() -> populatingPaymentFields = false);
        }
    }

    public void setProducts(java.util.Collection<SaleProduct> products) {
        var list = saleProductTablePresenter.getSaleProductList();
        list.clear();

        if (products != null && !products.isEmpty()) {
            for (var p : products) {
                if (p == null) continue;
                list.add(p);
            }
        }

        if (saleProductsTable != null) {
            saleProductsTable.refresh();
        }
        refreshSubtotals();
    }

    private void refreshSubtotals() {
        if (populatingSaleData) return;
        var items = saleProductTablePresenter.getSaleProductList();

        BigDecimal originalSubtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal itemsDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (items != null && !items.isEmpty()) {
            for (var it : items) {
                if (it == null) continue;

                var itemOriginal = it.getOriginalSubtotal();
                if (itemOriginal != null) originalSubtotal = originalSubtotal.add(itemOriginal);

                var itemDiscount = it.getDiscount();
                if (itemDiscount != null) itemsDiscount = itemsDiscount.add(itemDiscount);
            }
        }

        BigDecimal saleDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        try {
            if (saleDiscountField != null) {
                String raw = saleDiscountField.getText();
                saleDiscount = com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                        .formatCurrency(raw == null ? "" : raw)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception ignored) {
            // mantém saleDiscount = 0,00
        }

        // clamp para evitar desconto da venda maior que o permitido
        BigDecimal saleDiscountClamped = clampSaleDiscountToMax(saleDiscount, originalSubtotal, itemsDiscount);

        BigDecimal totalDiscountRaw = itemsDiscount.add(saleDiscountClamped)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netSubtotal = originalSubtotal.subtract(totalDiscountRaw)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDiscountToShow = totalDiscountRaw
                .min(originalSubtotal)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        if (subtotalLbl != null) {
            subtotalLbl.setText(
                    com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                            .formatText(netSubtotal.toPlainString())
            );
        }

        if (totalDiscountLbl != null) {
            totalDiscountLbl.setText(
                    com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                            .formatText(totalDiscountToShow.toPlainString())
            );
        }

        // ----------------------------
        // PONTO-CHAVE: sincronizar o MODEL com a UI
        // ----------------------------
        if (currentSale != null) {
            // (A) Atualiza produtos + aplica desconto da venda no model (recalcula totalPrice) [file:34]
            syncSaleTotalsFromUi(saleDiscountClamped);

            // (B) Atualiza totalAmountPaid a partir dos fields (recalcula totalChange) [file:34]
            updateTotalAmountPaidFromFields();

            // (C) Atualiza label do troco (se existir no seu FXML)
            if (totalChangeLbl != null) {
                BigDecimal change = currentSale.getTotalChange(); // totalAmountPaid - totalPrice [file:34]
                totalChangeLbl.setText(
                        com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                                .formatText(change.setScale(2, RoundingMode.HALF_UP).toPlainString())
                );
            }

            // Logs (para ver exatamente onde está divergindo)
            System.out.println("[SaleDetails][refreshSubtotals]"
                    + " uiOriginalSubtotal=" + originalSubtotal
                    + " uiItemsDiscount=" + itemsDiscount
                    + " uiSaleDiscount(raw)=" + saleDiscount
                    + " uiSaleDiscount(clamped)=" + saleDiscountClamped
                    + " uiNetSubtotal=" + netSubtotal
                    + " modelOriginalTotal=" + currentSale.getOriginalTotalPrice()
                    + " modelItemsDiscount=" + currentSale.getItemsDiscount()
                    + " modelSaleDiscount=" + currentSale.getSaleDiscount()
                    + " modelTotalPrice=" + currentSale.getTotalPrice()
                    + " modelTotalAmountPaid=" + currentSale.getTotalAmountPaid()
                    + " modelTotalChange=" + currentSale.getTotalChange());
        }
    }

    private void priceListener(TextField priceField) {
        if (priceField.getText() == null ||
                priceField.getText().isBlank() ||
                priceField.getText().isEmpty()) {
            priceField.setText("0,00");
        } else {
            String formattedValue =
                    com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                            .formatText(priceField.getText());
            priceField.setText(formattedValue);
        }

        priceField.setOnMouseClicked(mouseEvent ->
                priceField.positionCaret(priceField.getText().length())
        );

        priceField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            String formattedText =
                    com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                            .formatText(newValue);

            if (!newValue.equals(formattedText)) {
                Platform.runLater(() -> {
                    priceField.setText(formattedText);
                    priceField.positionCaret(priceField.getText().length());
                });
            }
        });
    }

    private BigDecimal clampSaleDiscountToMax(BigDecimal saleDiscount, BigDecimal originalSubtotal, BigDecimal itemsDiscount) {
        if (saleDiscount == null) saleDiscount = BigDecimal.ZERO;
        if (originalSubtotal == null) originalSubtotal = BigDecimal.ZERO;
        if (itemsDiscount == null) itemsDiscount = BigDecimal.ZERO;

        saleDiscount = saleDiscount.setScale(2, RoundingMode.HALF_UP);
        originalSubtotal = originalSubtotal.setScale(2, RoundingMode.HALF_UP);
        itemsDiscount = itemsDiscount.setScale(2, RoundingMode.HALF_UP);

        BigDecimal maxAllowed = originalSubtotal.subtract(itemsDiscount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        if (saleDiscount.compareTo(maxAllowed) > 0) {
            return maxAllowed;
        }
        if (saleDiscount.signum() < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return saleDiscount;
    }

    private void setupInstallmentsComboBox() {
        creditInstallments.getItems().clear();
        for (int i = 1; i <= 12; i++) {
            creditInstallments.getItems().add(i);
        }

        creditInstallments.setValue(1);

        creditInstallments.setConverter(new StringConverter<Integer>() {
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

        creditInstallments.getStylesheets().add(getClass().getResource("/com/gabrielosorio/gestor_inteligente/css/saleViewDetails.css").toExternalForm());

    }

    private void recalcTotalAmountPaidAndChange() {
        if (currentSale == null) return;

        BigDecimal totalPaid = BigDecimal.ZERO;
        for (var p : paymentByMethod.values()) {
            if (p == null) continue;
            BigDecimal v = p.getValue() == null ? BigDecimal.ZERO : p.getValue();
            totalPaid = totalPaid.add(v);
        }
        totalPaid = totalPaid.setScale(2, RoundingMode.HALF_UP);

        // Mesmo padrão do PaymentViewController: ele seta totalAmountPaid e o Sale fornece getTotalChange() [file:22]
        currentSale.setTotalAmountPaid(totalPaid);

        updateChangeLabel();

        System.out.println("[SaleDetails] totalAmountPaid=" + currentSale.getTotalAmountPaid()
                + " totalPrice=" + currentSale.getTotalPrice()
                + " totalChange=" + currentSale.getTotalChange());
    }

    private void updateChangeLabel() {
        if (totalChangeLbl == null || currentSale == null) return;

        BigDecimal change = currentSale.getTotalChange();
        if (change == null) change = BigDecimal.ZERO;

        totalChangeLbl.setText(
                com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                        .formatText(change.setScale(2, RoundingMode.HALF_UP).toPlainString())
        );
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Não foi possível salvar/finalizar a venda");
            alert.setContentText(message);
            alert.getButtonTypes().setAll(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
            alert.showAndWait();
        });
    }

    private Sale validateAndUpdateSale() throws SaleProcessingException, SaleValidationException, SalePaymentException {
        if (saleService == null) throw new IllegalStateException("SaleService não configurado no controller.");
        if (user == null) throw new IllegalStateException("Usuário não configurado no controller.");
        if (currentSale == null) throw new IllegalStateException("Venda atual (currentSale) está nula.");

        return saleService.updateSale(user, currentSale, currentCheckout);
    }

    private void syncSaleTotalsFromUi(BigDecimal uiSaleDiscount) {
        if (currentSale == null) return;

        // 1) Recalcula totals (originalTotalPrice/itemsDiscount/totalPrice) com base nos itens atuais [file:34]
        List<SaleProduct> uiItems = new ArrayList<>(saleProductTablePresenter.getSaleProductList());
        currentSale.setSaleProducts(uiItems); // dispara calculateTotals() -> recalculateTotals() [file:34]

        // 2) Aplica desconto da venda (clamp opcional) e recalcula totalPrice novamente [file:34]
        BigDecimal clamped = clampSaleDiscountToMax(
                uiSaleDiscount,
                currentSale.getOriginalTotalPrice(),
                currentSale.getItemsDiscount()
        );

        currentSale.setSaleDiscount(clamped); // recalculateTotals() [file:34]

        System.out.println("[SaleDetails][syncSaleTotalsFromUi] originalTotal=" + currentSale.getOriginalTotalPrice()
                + " itemsDiscount=" + currentSale.getItemsDiscount()
                + " saleDiscount=" + currentSale.getSaleDiscount()
                + " totalDiscount=" + currentSale.getTotalDiscount()
                + " totalPrice=" + currentSale.getTotalPrice()
                + " totalAmountPaid=" + currentSale.getTotalAmountPaid()
                + " totalChange=" + currentSale.getTotalChange());
    }

    private void updateTotalAmountPaidFromFields() {
        if (currentSale == null) return;

        BigDecimal totalPaid = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        try {
            if (cashMethodField != null) {
                totalPaid = totalPaid.add(
                        com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                                .formatCurrency(cashMethodField.getText() == null ? "" : cashMethodField.getText())
                );
            }
            if (debitMethodField != null) {
                totalPaid = totalPaid.add(
                        com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                                .formatCurrency(debitMethodField.getText() == null ? "" : debitMethodField.getText())
                );
            }
            if (creditMethodField != null) {
                totalPaid = totalPaid.add(
                        com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                                .formatCurrency(creditMethodField.getText() == null ? "" : creditMethodField.getText())
                );
            }
            if (pixMethodField != null) {
                totalPaid = totalPaid.add(
                        com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils
                                .formatCurrency(pixMethodField.getText() == null ? "" : pixMethodField.getText())
                );
            }
        } catch (Exception ignored) {
            // evita quebrar em estados intermediários de digitação
        }

        currentSale.setTotalAmountPaid(totalPaid.setScale(2, RoundingMode.HALF_UP)); // recalcula troco internamente [file:34]
    }

    public void setCheckout(Checkout currentCheckout) {
        this.currentCheckout = currentCheckout;
    }
}
