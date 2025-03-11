package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.service.base.CheckoutService;
import com.gabrielosorio.gestor_inteligente.utils.TableViewUtils;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CheckoutMovementController implements Initializable, ShortcutHandler {

    private final Logger log = Logger.getLogger(getClass().getName());

    private TableView<CheckoutMovement> cMTableView;
    private CheckoutMovementDialogController checkoutMovementDialogController;
    private Node checkoutMovementDialog;
    private final CheckoutService checkoutService;
    private final Checkout checkout;

    @FXML private Label SalesAvg, canceled, cashMethod, cost, creditMethod, debitMethod, grossProfit,
            grossProfitMargin, inflow, initialCash, outflow, pixMethod, qtdSales, statusLbl, totalSale;

    @FXML private DatePicker startDate, endDate;
    @FXML private AnchorPane mainContent, tableContent;
    @FXML private ImageView statusView;

    public CheckoutMovementController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
        User user = createUser();
        this.checkout = checkoutService.openCheckout(user);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setTodayDate();
        initializeTableView();
        populateTable();
        initializeLabels();
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
    }

    private void configureTableViewLayout() {
        cMTableView.setLayoutX(4.0);
        cMTableView.setLayoutY(41.0);
        cMTableView.setPrefHeight(478.0);
        cMTableView.setPrefWidth(991.0);

        AnchorPane.setBottomAnchor(cMTableView, -3.0);
        AnchorPane.setLeftAnchor(cMTableView, 0.0);
        AnchorPane.setRightAnchor(cMTableView, 0.0);
    }

    private void configureColumnWidths() {
        TableViewUtils.getColumnById(cMTableView, "paymentProperty").setPrefWidth(143.01);
        TableViewUtils.getColumnById(cMTableView, "valueProperty").setPrefWidth(148.01);
        TableViewUtils.getColumnById(cMTableView, "timeProperty").setPrefWidth(61.84);
        TableViewUtils.getColumnById(cMTableView, "movementTypeProperty").setPrefWidth(197.22);
        TableViewUtils.getColumnById(cMTableView, "obsProperty").setPrefWidth(289.10);
    }

    private void populateTable() {
        List<CheckoutMovement> list = checkoutService.findCheckoutMovementsById(checkout.getId());
        cMTableView.getItems().addAll(list);

        cMTableView.getColumns().forEach(column -> column.widthProperty().addListener((obs, oldWidth, newWidth) ->
                System.out.println("Coluna: " + column.getId() + " | Largura: " + newWidth)));

        configurePaymentColumn();
        tableContent.getChildren().add(cMTableView);
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

    private void initializeLabels() {
        initialCash.setText(TextFieldUtils.formatText(checkout.getInitialCash().toPlainString()));
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
                var initialCash = checkoutMovementDialogController.getValue();
                checkoutService.setInitialCash(checkout.getId(), new Payment(PaymentMethod.DINHEIRO, initialCash), "");
                checkoutMovementDialogController.close();
            });

        } catch (Exception e) {
            log.severe("ERROR at load checkout movement dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String bigDecimalToMonetaryString(BigDecimal value) {
        return TextFieldUtils.formatText(value.toPlainString());
    }

    @Override
    public void handleShortcut(KeyCode keyCode) {
        if (keyCode.equals(KeyCode.F1)) {
            showCheckoutMovementDialog();
        }
    }
}
