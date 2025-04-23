package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.events.PaymentEvent;
import com.gabrielosorio.gestor_inteligente.events.PaymentEventBus;
import com.gabrielosorio.gestor_inteligente.events.listeners.PaymentListener;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutMovementTypeEnum;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutMovementService;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutService;
import com.gabrielosorio.gestor_inteligente.service.base.SaleCheckoutMovementService;
import com.gabrielosorio.gestor_inteligente.service.base.SaleService;
import com.gabrielosorio.gestor_inteligente.utils.TableViewUtils;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewFactory;
import com.gabrielosorio.gestor_inteligente.view.util.BrazilianDatePicker;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CheckoutMovementController implements Initializable, ShortcutHandler, PaymentListener {

    private final Logger log = Logger.getLogger(getClass().getName());

    private TableView<CheckoutMovement> cMTableView;
    private CheckoutMovementDialogController checkoutMovementDialogController;
    private ObservableList<CheckoutMovement> movementsList;
    private Node checkoutMovementDialog;
    private final CheckoutService checkoutService;
    private final SaleCheckoutMovementService slCheckoutMovementService;
    private final SaleService saleService;
    private final Checkout checkout;
    private final CheckoutMovementService checkoutMovementService;

    @FXML private Label salesAvg, canceled, cashMethod, cost, creditMethod, debitMethod, grossProfit,
            grossProfitMargin, inflow, initialCash, outflow, pixMethod, qtdSales, statusLbl, totalSale;

    @FXML private DatePicker startDate, endDate;
    @FXML private AnchorPane mainContent, tableContent;
    @FXML private ImageView statusView;

    public CheckoutMovementController(CheckoutService checkoutService, SaleCheckoutMovementService slCheckoutMovementService, SaleService saleService, CheckoutMovementService checkoutMovementService) {
        this.checkoutService = checkoutService;
        this.slCheckoutMovementService = slCheckoutMovementService;
        this.saleService = saleService;
        User user = createUser();
        this.checkout = checkoutService.openCheckout(user);
        this.checkoutMovementService = checkoutMovementService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PaymentEventBus.getInstance().register(this);
        setTodayDate();
        initializeTableView();
        setupDateSearch();
        populateTable();
        this.updateInitialCashLabel();
        updatePaymentMethods(checkoutService.findCheckoutMovementsById(checkout.getId()));
    }

    // binds date pickers to the search logic
    private void setupDateSearch() {
        BrazilianDatePicker.configure(startDate);
        BrazilianDatePicker.configure(endDate);
        startDate.setOnAction(event -> searchMovementsByDateRange());
        endDate.setOnAction(event -> searchMovementsByDateRange());
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
            List<CheckoutMovement> movementsByDateRange =
                    checkoutMovementService.findByDateRange(startDateTime, endDateTime);
            movementsByDateRange.sort(Comparator.comparing(CheckoutMovement::getDateTime).reversed());

            movementsList.clear();
            movementsList.addAll(movementsByDateRange);
            cMTableView.refresh();

            this.updateInitialCashLabel();
            updatePaymentMethods(movementsByDateRange);
        } catch (Exception e) {
            log.severe("Erro ao buscar movimentos por data: " + e.getMessage());
            showAlert("Erro na pesquisa", "Ocorreu um erro ao buscar os movimentos: " + e.getMessage());
        }

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updatePaymentMethods(List<CheckoutMovement> movements) {
        BigDecimal pixTotal = BigDecimal.ZERO;
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;

        movements.sort(Comparator.comparing(CheckoutMovement::getDateTime));

        BigDecimal lastInitialCash = BigDecimal.ZERO;

        for (CheckoutMovement m : movements) {
            Payment p = m.getPayment();
            if (p != null && p.getValue() != null) {
                PaymentMethod method = p.getPaymentMethod();
                BigDecimal value = p.getValue();

                boolean isCashAdjustment = m.getMovementType().getId() == 4;

                if (isCashAdjustment) {
                    BigDecimal delta = value.subtract(lastInitialCash);
                    cashTotal = cashTotal.add(delta);
                    lastInitialCash = value;
                } else {
                    if (method != null) {
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
        }

        pixMethod.setText(TextFieldUtils.formatText(pixTotal.toPlainString()));
        cashMethod.setText(TextFieldUtils.formatText(cashTotal.toPlainString()));
        debitMethod.setText(TextFieldUtils.formatText(debitTotal.toPlainString()));
        creditMethod.setText(TextFieldUtils.formatText(creditTotal.toPlainString()));

        List<Sale> sales = slCheckoutMovementService.findSalesInCheckoutMovements(movements);
        BigDecimal grossProfit = saleService.calculateTotalProfit(sales);
        BigDecimal cost        = saleService.calculateTotalCost(sales);
        BigDecimal totalSales  = saleService.calculateTotalSales(sales);
        BigDecimal salesAvg    = saleService.calculateAverageSale(sales);

        this.grossProfit.setText(TextFieldUtils.formatText(grossProfit.toPlainString()));
        this.cost.setText(TextFieldUtils.formatText(cost.toPlainString()));
        this.salesAvg.setText(TextFieldUtils.formatText(salesAvg.toPlainString()));
        this.totalSale.setText(TextFieldUtils.formatText(totalSales.toPlainString()));
        this.qtdSales.setText(String.valueOf(saleService.countSales(sales)));
    }

    private void updatePaymentMethods() {
    BigDecimal pixTotal = BigDecimal.ZERO;
    BigDecimal cashTotal = BigDecimal.ZERO;
    BigDecimal debitTotal = BigDecimal.ZERO;
    BigDecimal creditTotal = BigDecimal.ZERO;

    List<CheckoutMovement> movements = checkoutService.findCheckoutMovementsById(checkout.getId());

    // Ordena os movimentos por data (ou ID se for sequencial)
    movements.sort(Comparator.comparing(CheckoutMovement::getDateTime)); // Ajuste conforme sua modelagem

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
            } else {
                if (method != null) {
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
    }

    pixMethod.setText(TextFieldUtils.formatText(pixTotal.toPlainString()));
    cashMethod.setText(TextFieldUtils.formatText(cashTotal.toPlainString()));
    debitMethod.setText(TextFieldUtils.formatText(debitTotal.toPlainString()));
    creditMethod.setText(TextFieldUtils.formatText(creditTotal.toPlainString()));

    var sales = slCheckoutMovementService.findSalesInCheckoutMovements(movements);
    BigDecimal grossProfit = saleService.calculateTotalProfit(sales);
    BigDecimal cost = saleService.calculateTotalCost(sales);
    BigDecimal totalSales = saleService.calculateTotalSales(sales);

    this.grossProfit.setText(TextFieldUtils.formatText(grossProfit.toPlainString()));
    this.cost.setText(TextFieldUtils.formatText(cost.toPlainString()));
    this.totalSale.setText(TextFieldUtils.formatText(totalSales.toPlainString()));
    qtdSales.setText(String.valueOf(saleService.countSales(sales)));
}

    private User createUser() {
        User user = new User();
        user.setFirstName("Gabriel");
        user.setLastName("Osório");
        return user;
    }

    private void initializeTableView() {
        TableViewFactory<CheckoutMovement> tableViewFactory = new TableViewFactory<>(CheckoutMovement.class);
        this.cMTableView = tableViewFactory.createTableView("/com/gabrielosorio/gestor_inteligente/css/checkoutMovTableView.css");

        configureTableViewLayout();
        configureColumnWidths();
        configurePaymentColumn();
        configureObsColumn();
        tableContent.getChildren().add(cMTableView);
    }

    private void configureTableViewLayout() {
        cMTableView.setLayoutX(4.0);
        cMTableView.setLayoutY(41.0);
        cMTableView.setPrefHeight(478.0);
        cMTableView.setPrefWidth(991.0);

        AnchorPane.setBottomAnchor(cMTableView, -3.0);
        AnchorPane.setLeftAnchor(cMTableView, 0.0);
        AnchorPane.setRightAnchor(cMTableView, 0.0);

        cMTableView.getColumns().forEach(TableViewUtils::resetColumnProps);
    }

    private void configureColumnWidths() {
        TableViewUtils.getColumnById(cMTableView, "dateProperty").setPrefWidth(120.01);
        TableViewUtils.getColumnById(cMTableView, "paymentProperty").setPrefWidth(143.01);
        TableViewUtils.getColumnById(cMTableView, "valueProperty").setPrefWidth(148.01);
        TableViewUtils.getColumnById(cMTableView, "timeProperty").setPrefWidth(61.84);
        TableViewUtils.getColumnById(cMTableView, "movementTypeProperty").setPrefWidth(197.22);
        TableViewUtils.getColumnById(cMTableView, "obsProperty").setPrefWidth(289.10);
    }

    private void populateTable() {
        movementsList = FXCollections.observableArrayList();
        List<CheckoutMovement> list = checkoutService.findCheckoutMovementsById(checkout.getId());
        list.sort(Comparator.comparing(CheckoutMovement::getDateTime).reversed());
        movementsList.addAll(list);
        cMTableView.setItems(movementsList);

    }

    private void refreshTable() {
        List<CheckoutMovement> allMovements = checkoutService.findCheckoutMovementsById(checkout.getId());
        allMovements.sort(Comparator.comparing(CheckoutMovement::getDateTime).reversed());

        movementsList.clear();
        movementsList.addAll(allMovements);
        cMTableView.refresh();

    }

    private void configurePaymentColumn() {
        TableColumn<CheckoutMovement, String> paymentColumn = (TableColumn<CheckoutMovement, String>) TableViewUtils.getColumnById(cMTableView, "paymentProperty");
        paymentColumn.setCellFactory(col -> new TableCell<CheckoutMovement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                setStyle("-fx-alignment: CENTER-LEFT;");
            }
        });
    }

    private void configureObsColumn(){
        TableColumn<CheckoutMovement, String> obsColumn =
                (TableColumn<CheckoutMovement, String>) TableViewUtils.getColumnById(cMTableView, "obsProperty");

        obsColumn.setCellFactory(col -> new TableCell<CheckoutMovement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String[] parts = item.split(" - ", 2);

                    Text boldPart = new Text(parts[0]); // "Venda #ID"
                    boldPart.setStyle("-fx-font-weight: bold;");

                    Text normalPart = parts.length > 1 ? new Text(" - " + parts[1]) : new Text("");


                    TextFlow textFlow = new TextFlow(boldPart, normalPart);
                    setGraphic(textFlow);
                    setText(null);
                }
            }
        });
    }

    private void updateInitialCashLabel() {
        initialCash.setText(TextFieldUtils.formatText(checkout.getInitialCash().toPlainString()));
    }

    private void updateInitialCashLabel(BigDecimal value) {
        initialCash.setText(TextFieldUtils.formatText(value.toPlainString()));
    }

    private void setTodayDate() {
        LocalDate today = LocalDate.now();
        startDate.setValue(today);
        endDate.setValue(today);
    }

    protected void showCheckoutMovementDialog() {
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
                setInitialCash(initialCash,obs);
                checkoutMovementDialogController.close();
            });


        } catch (Exception e) {
            log.severe("ERROR at load checkout movement dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void setInitialCash(BigDecimal initialCash, String obs) {
        if(obs.isBlank()){
            obs = "F. Caixa";
        }

        checkoutService.setInitialCash(checkout.getId(), new Payment(PaymentMethod.DINHEIRO, initialCash), obs.strip());
        updatePaymentMethods();
        updateInitialCashLabel(initialCash);
        refreshTable();
    }

    @Override
    public void handleShortcut(KeyCode keyCode) {
        if (keyCode.equals(KeyCode.F1)) {
            showCheckoutMovementDialog();
        }
    }

    @Override
    public void onPaymentFinalized(PaymentEvent event) {
        refreshTable();
        updatePaymentMethods();
    }
}
