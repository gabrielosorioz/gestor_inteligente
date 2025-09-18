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
        loadMovementsData();
        updateInitialCashLabel();
    }

    private void setupDatePickers() {
        BrazilianDatePicker.configure(startDate);
        BrazilianDatePicker.configure(endDate);

        LocalDate today = LocalDate.now();
        startDate.setValue(today);
        endDate.setValue(today);

        startDate.setOnAction(event -> searchMovementsByDateRange());
        endDate.setOnAction(event -> searchMovementsByDateRange());
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

    private void loadMovementsData() {
        List<CheckoutMovement> movements = checkoutService.findCheckoutMovementsById(checkout.getId());
        tablePresenter.displayMovements(movementsTableView, movementsList, movements);
        updatePaymentSummary(movements);
    }

    private void searchMovementsByDateRange() {
        try {
            LocalDate startLocalDate = startDate.getValue();
            LocalDate endLocalDate = endDate.getValue();

            if (startLocalDate == null || endLocalDate == null) {
                showAlert("Data inválida", "Por favor, selecione as datas inicial e final.");
                return;
            }

            LocalDateTime startDateTime = LocalDateTime.of(startLocalDate, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(endLocalDate, LocalTime.MAX);

            List<CheckoutMovement> movements = checkoutMovementService.findByDateRange(startDateTime, endDateTime);
            tablePresenter.displayMovements(movementsTableView, movementsList, movements);
            updateInitialCashLabel();
            updatePaymentSummary(movements);
        } catch (Exception e) {
            log.severe("Erro ao buscar movimentos por data: " + e.getMessage());
            showAlert("Erro na pesquisa", "Ocorreu um erro ao buscar os movimentos: " + e.getMessage());
        }
    }

    private void updatePaymentSummary(List<CheckoutMovement> movements) {
        PaymentSummary summary = paymentPresenter.calculatePaymentSummary(movements);

        // Atualiza os rótulos de método de pagamento
        pixMethod.setText(TextFieldUtils.formatText(summary.getPixTotal().toPlainString()));
        cashMethod.setText(TextFieldUtils.formatText(summary.getCashTotal().toPlainString()));
        debitMethod.setText(TextFieldUtils.formatText(summary.getDebitTotal().toPlainString()));
        creditMethod.setText(TextFieldUtils.formatText(summary.getCreditTotal().toPlainString()));

        // Atualiza os rótulos de estatísticas de vendas
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
        initialCash.setText(TextFieldUtils.formatText(checkout.getInitialCash().toPlainString()));
    }

    private void updateInitialCashLabel(BigDecimal value) {
        initialCash.setText(TextFieldUtils.formatText(value.toPlainString()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void showCheckoutMovementDialog() {
        try {
            if (checkoutMovementDialog == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/CheckoutMovementDialog.fxml"));
                checkoutMovementDialog = fxmlLoader.load();
                checkoutMovementDialogController = fxmlLoader.getController();
            }

            if (!mainContent.getChildren().contains(checkoutMovementDialog)) {
                mainContent.getChildren().add(checkoutMovementDialog);
                checkoutMovementDialogController.requestFocusOnField();
            }

            checkoutMovementDialogController.getBtnOk().setOnMouseClicked(mouseEvent -> {
                BigDecimal initialCash = checkoutMovementDialogController.getValue();
                String obs = checkoutMovementDialogController.getObs();
                setInitialCash(initialCash, obs);
                checkoutMovementDialogController.close();
            });
        } catch (Exception e) {
            log.severe("ERROR at load checkout movement dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void setInitialCash(BigDecimal initialCash, String obs) {
        if (obs.isBlank()) {
            obs = "F. Caixa";
        }

        checkoutService.setInitialCash(checkout.getId(), new Payment(PaymentMethod.DINHEIRO, initialCash), obs.strip());
        updateInitialCashLabel(initialCash);
        refreshData();
    }

    private void refreshData() {
        List<CheckoutMovement> movements = checkoutService.findCheckoutMovementsById(checkout.getId());
        tablePresenter.displayMovements(movementsTableView, movementsList, movements);
        updatePaymentSummary(movements);
    }

    @Override
    public void handleShortcut(KeyCode keyCode) {
        if (keyCode.equals(KeyCode.F1)) {
            showCheckoutMovementDialog();
        }
    }

    @Override
    public void onPaymentFinalized(PaymentEvent event) {
        refreshData();
    }

}