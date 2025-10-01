package com.gabrielosorio.gestor_inteligente.view.checkout;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.events.PaymentEvent;
import com.gabrielosorio.gestor_inteligente.events.PaymentEventBus;
import com.gabrielosorio.gestor_inteligente.events.listeners.PaymentListener;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutMovementService;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutService;
import com.gabrielosorio.gestor_inteligente.service.base.SaleCheckoutMovementService;
import com.gabrielosorio.gestor_inteligente.service.base.SaleService;
import com.gabrielosorio.gestor_inteligente.view.checkout.helpers.*;
import com.gabrielosorio.gestor_inteligente.view.shared.ShortcutHandler;
import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.view.shared.util.BrazilianDatePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class CheckoutMovementController implements Initializable, ShortcutHandler, PaymentListener {

    private final Logger log = Logger.getLogger(getClass().getName());

    // Componentes da UI
    @FXML private Label salesAvg, canceled, cashMethod, cost, creditMethod, debitMethod, grossProfit,
            grossProfitMargin, inflow, initialCash, outflow, pixMethod, qtdSales, statusLbl, totalSale, roleAndNameLbl;
    @FXML private DatePicker startDate, endDate;
    @FXML private AnchorPane mainContent, tableContent;
    @FXML private ImageView statusView;

    // Dependências
    private final CheckoutService checkoutService;
    private final SaleCheckoutMovementService saleCheckoutMovementService;
    private final SaleService saleService;
    private final CheckoutMovementService checkoutMovementService;
    private final Checkout checkout;
    private final User user;

    // Componentes de UI e estado
    private TableView<CheckoutMovement> movementsTableView;
    private CheckoutMovementDialogController checkoutMovementDialogController;
    private ObservableList<CheckoutMovement> movementsList;
    private Node checkoutMovementDialog;

    // Apresentadores para separar lógica de visualização
    private final CheckoutMovementTablePresenter tablePresenter;
    private final PaymentSummaryPresenter paymentPresenter;

    public CheckoutMovementController(CheckoutService checkoutService,
                                      SaleCheckoutMovementService saleCheckoutMovementService,
                                      SaleService saleService,
                                      CheckoutMovementService checkoutMovementService, User user) {
        this.checkoutService = checkoutService;
        this.saleCheckoutMovementService = saleCheckoutMovementService;
        this.saleService = saleService;
        this.checkoutMovementService = checkoutMovementService;
        this.user = user;
        this.checkout = checkoutService.openCheckout(user);
        this.tablePresenter = new CheckoutMovementTablePresenter();
        this.paymentPresenter = new PaymentSummaryPresenter();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PaymentEventBus.getInstance().register(this);
        roleAndNameLbl.setText(user.getRole().getName() + ": " +user.getFullName());
        setupDatePickers();
        initializeTableView();
        loadMovements();
    }

    private void setupDatePickers() {
        BrazilianDatePicker.configure(startDate);
        BrazilianDatePicker.configure(endDate);

        LocalDate today = LocalDate.now();
        startDate.setValue(today);
        endDate.setValue(today);

        startDate.setOnAction(event -> {
            if (isValidDateRange()) {
                loadMovements();
            }
        });

        endDate.setOnAction(event -> {
            if (isValidDateRange()) {
                loadMovements();
            }
        });
    }

    private boolean isValidDateRange() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        if (start == null || end == null) {
            return false;
        }

        if (start.isAfter(end)) {
            showAlert("Data inválida", "A data inicial não pode ser posterior à data final.");
            return false;
        }

        return true;
    }


    private void initializeTableView() {
        movementsList = FXCollections.observableArrayList();
        movementsTableView = tablePresenter.createTableView();
        configureTableViewLayout();
        tableContent.getChildren().add(movementsTableView);
    }

    private void configureTableViewLayout() {
        movementsTableView.setLayoutX(4.0);
        movementsTableView.setLayoutY(41.0);
        movementsTableView.setPrefHeight(478.0);
        movementsTableView.setPrefWidth(991.0);

        AnchorPane.setBottomAnchor(movementsTableView, -3.0);
        AnchorPane.setLeftAnchor(movementsTableView, 0.0);
        AnchorPane.setRightAnchor(movementsTableView, 0.0);
    }

    private void loadMovements() {
        try {
            List<CheckoutMovement> movements;
            LocalDate startLocalDate = startDate.getValue();
            LocalDate endLocalDate = endDate.getValue();

            if (startLocalDate != null && endLocalDate != null) {
                LocalDateTime startDateTime = LocalDateTime.of(startLocalDate, LocalTime.MIN);
                LocalDateTime endDateTime = LocalDateTime.of(endLocalDate, LocalTime.MAX);
                movements = checkoutMovementService.findByDateRange(startDateTime, endDateTime);
            } else {
                movements = checkoutService.findCheckoutMovementsById(checkout.getId());
            }

            tablePresenter.displayMovements(movementsTableView, movementsList, movements);
            updatePaymentSummary(movements);
            updateInitialCashLabel();
        } catch (Exception e) {
            log.severe("Erro ao carregar movimentos: " + e.getMessage());
            showAlert("Erro", "Ocorreu um erro ao carregar os movimentos: " + e.getMessage());
        }
    }

    private void searchMovementsByDateRange() {
        if (startDate.getValue() == null || endDate.getValue() == null) {
            showAlert("Data inválida", "Por favor, selecione as datas inicial e final.");
            return;
        }
        loadMovements();
    }

    private void updatePaymentSummary(List<CheckoutMovement> movements) {
        PaymentSummary summary = paymentPresenter.calculatePaymentSummary(movements);

        pixMethod.setText(TextFieldUtils.formatText(summary.getPixTotal().toPlainString()));
        cashMethod.setText(TextFieldUtils.formatText(summary.getCashTotal().toPlainString()));
        debitMethod.setText(TextFieldUtils.formatText(summary.getDebitTotal().toPlainString()));
        creditMethod.setText(TextFieldUtils.formatText(summary.getCreditTotal().toPlainString()));

        List<Sale> sales = saleCheckoutMovementService.findSalesInCheckoutMovements(movements);
        SalesSummary salesSummary = calculateSalesSummary(sales);

        grossProfit.setText(TextFieldUtils.formatText(salesSummary.getGrossProfit().toPlainString()));
        cost.setText(TextFieldUtils.formatText(salesSummary.getCost().toPlainString()));
        salesAvg.setText(TextFieldUtils.formatText(salesSummary.getSalesAvg().toPlainString()));
        totalSale.setText(TextFieldUtils.formatText(salesSummary.getTotalSales().toPlainString()));
        qtdSales.setText(String.valueOf(salesSummary.getSalesCount()));
    }

    private SalesSummary calculateSalesSummary(List<Sale> sales) {
        BigDecimal grossProfit = saleService.calculateTotalProfit(sales);
        BigDecimal cost = saleService.calculateTotalCost(sales);
        BigDecimal totalSales = saleService.calculateTotalSales(sales);
        BigDecimal salesAvg = saleService.calculateAverageSale(sales);
        long salesCount = saleService.countSales(sales);

        return new SalesSummary(grossProfit, cost, totalSales, salesAvg, salesCount);
    }

    private void updateInitialCashLabel() {
        try {
            Checkout currentCheckout = checkoutService.findById(checkout.getId()).get();
            if (currentCheckout != null) {
                BigDecimal currentInitialCash = currentCheckout.getInitialCash();
                initialCash.setText(TextFieldUtils.formatText(currentInitialCash.toPlainString()));

                checkout.setInitialCash(currentInitialCash);
            } else {
                initialCash.setText(TextFieldUtils.formatText(checkout.getInitialCash().toPlainString()));
            }
        } catch (Exception e) {
            log.warning("Erro ao atualizar label do fundo de caixa: " + e.getMessage());
            initialCash.setText(TextFieldUtils.formatText(checkout.getInitialCash().toPlainString()));
        }
    }

    private void updateInflowLabel() {
        try {
            Checkout currentCheckout = checkoutService.findById(checkout.getId()).get();
            if (currentCheckout != null) {
                BigDecimal currentTotalEntry = currentCheckout.getTotalEntry();
                inflow.setText(TextFieldUtils.formatText(currentTotalEntry.toPlainString()));

                checkout.setTotalEntry(currentTotalEntry);
            } else {
                inflow.setText(TextFieldUtils.formatText(checkout.getTotalEntry().toPlainString()));
            }
        } catch (Exception e) {
            log.warning("Erro ao atualizar label de entrada de caixa: " + e.getMessage());
            inflow.setText(TextFieldUtils.formatText(checkout.getTotalEntry().toPlainString()));
        }
    }

    private void updateOutflowLabel() {
        try {
            Checkout currentCheckout = checkoutService.findById(checkout.getId()).get();
            if (currentCheckout != null) {
                BigDecimal currentTotalExit = currentCheckout.getTotalExit();
                outflow.setText(TextFieldUtils.formatText(currentTotalExit.toPlainString()));

                checkout.setTotalExit(currentTotalExit);
            } else {
                outflow.setText(TextFieldUtils.formatText(checkout.getTotalExit().toPlainString()));
            }
        } catch (Exception e) {
            log.warning("Erro ao atualizar label de saída de caixa: " + e.getMessage());
            outflow.setText(TextFieldUtils.formatText(checkout.getTotalExit().toPlainString()));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Node loadCheckoutMovementDialogView() {
        try {
            if (checkoutMovementDialog == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(
                        GestorInteligenteApp.class.getResource("fxml/CheckoutMovementDialog.fxml")
                );
                checkoutMovementDialog = fxmlLoader.load();
                checkoutMovementDialogController = fxmlLoader.getController();
            }
            return checkoutMovementDialog;
        } catch (Exception e) {
            log.severe("ERROR at load checkout movement dialog: " + e.getMessage());
            throw new RuntimeException("Failed to load CheckoutMovementDialog", e);
        }
    }

    private void showDialog(Node dialogView,String title) {
        if (!mainContent.getChildren().contains(dialogView)) {
            mainContent.getChildren().add(dialogView);
            checkoutMovementDialogController.setTitle(title);
            checkoutMovementDialogController.requestFocusOnField();
        }
    }

    public void showCheckoutMovementDialog(BiConsumer<BigDecimal, String> consumer, String title) {
        Node dialogView = loadCheckoutMovementDialogView();
        showDialog(dialogView,title);

        checkoutMovementDialogController.setOnConfirm((value, obs) -> {
            consumer.accept(value, obs);
            checkoutMovementDialogController.close();
        });
    }

    private void setInitialCash(BigDecimal initialCash, String obs) {
        if (obs.isBlank()) {
            obs = "F. Caixa";
        }

        try {
            checkoutService.setInitialCash(checkout.getId(), new Payment(PaymentMethod.DINHEIRO, initialCash), obs.strip());
            checkout.setInitialCash(initialCash);
            refreshData();

            log.info("Fundo de caixa atualizado com sucesso: " + initialCash);

        } catch (Exception e) {
            log.severe("Erro ao definir fundo de caixa: " + e.getMessage());
            showAlert("Erro", "Falha ao atualizar o fundo de caixa: " + e.getMessage());
            updateInitialCashLabel();
        }
    }

    private void setCashInflow(BigDecimal cashInflow, String obs) {
        if (obs.isBlank()) {
            obs = "Entrada";
        }
        try {
            checkoutService.addCashInflow(checkout.getId(), new Payment(PaymentMethod.DINHEIRO, cashInflow),
                    obs.strip());
            refreshData();
            updateInflowLabel();

            log.info("Entrada de caixa adicionada com sucesso: " + cashInflow);

        } catch (Exception e) {
            log.severe("Erro ao adicionar entrada de caixa: " + e.getMessage());
            showAlert("Erro", "Falha ao adicionar entrada de caixa: " + e.getMessage());
            updateInflowLabel();
        }
    }

    private void setCashOutflow(BigDecimal cashOutflow, String obs) {
        if (obs.isBlank()) {
            obs = "Saída";
        }

        BigDecimal currentCashTotal = getCurrentCashTotal();

        if (currentCashTotal.compareTo(cashOutflow) < 0) {
            showAlert("Saldo Insuficiente",
                    "Não há dinheiro suficiente no caixa para esta saída.");
            return;
        }

        try {
            checkoutService.addCashOutflow(checkout.getId(),
                    new Payment(PaymentMethod.DINHEIRO, cashOutflow), obs.strip());
            refreshData();
            updateOutflowLabel();
            logCurrentCashStatus();

            log.info("Saída de caixa adicionada com sucesso: " + cashOutflow);

        } catch (Exception e) {
            log.severe("Erro ao adicionar saída de caixa: " + e.getMessage());
            showAlert("Erro", "Falha ao adicionar saída de caixa: " + e.getMessage());
            updateOutflowLabel();
        }
    }

    private void logCurrentCashStatus() {
        try {
            Checkout currentCheckout = checkoutService.findById(checkout.getId())
                    .orElse(checkout);

            log.info(String.format("Status do Caixa - ID: %d", currentCheckout.getId()));
            log.info(String.format("Fundo Inicial: %s", currentCheckout.getInitialCash()));
            log.info(String.format("Total Entradas: %s", currentCheckout.getTotalEntry()));
            log.info(String.format("Total Saídas: %s", currentCheckout.getTotalExit()));
            log.info(String.format("Saldo Atual: %s", getCurrentCashTotal()));
        } catch (Exception e) {
            log.warning("Erro ao exibir status do caixa: " + e.getMessage());
        }
    }



    private BigDecimal getCurrentCashTotal() {
        return checkout.getInitialCash()
                .add(checkout.getTotalEntry())
                .subtract(checkout.getTotalExit());
    }

    private void refreshData() {
       loadMovements();
    }

    @Override
    public void handleShortcut(KeyCode keyCode) {
        switch (keyCode) {
            case F1 -> showCheckoutMovementDialog(this::setInitialCash,"Fundo de Caixa");
            case F2 -> showCheckoutMovementDialog(this::setCashInflow,"Entrada");
            case F3 -> showCheckoutMovementDialog(this::setCashOutflow,"Saída");
        }
        logCurrentCashStatus();
    }

    @Override
    public void onPaymentFinalized(PaymentEvent event) {
        refreshData();
    }

}