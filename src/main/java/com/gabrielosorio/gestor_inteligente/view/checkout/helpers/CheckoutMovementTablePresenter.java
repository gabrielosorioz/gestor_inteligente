package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.utils.TableViewUtils;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Comparator;
import java.util.List;

/**
 * Classe responsável por criar e configurar a TableView de movimentos de caixa
 */
public class CheckoutMovementTablePresenter {

    /**
     * Cria e configura a TableView para exibir movimentos de caixa
     */
    public TableView<CheckoutMovement> createTableView() {
        TableViewFactory<CheckoutMovement> tableViewFactory = new TableViewFactory<>(CheckoutMovement.class);
        TableView<CheckoutMovement> tableView = tableViewFactory.createTableView("/com/gabrielosorio/gestor_inteligente/css/checkoutMovTableView.css");

        configureColumnWidths(tableView);
        configurePaymentColumn(tableView);
        configureObsColumn(tableView);

        tableView.getColumns().forEach(TableViewUtils::resetColumnProps);

        return tableView;
    }

    /**
     * Carrega e exibe os movimentos na tabela
     */
    public void displayMovements(TableView<CheckoutMovement> tableView,
                                 ObservableList<CheckoutMovement> movementsList,
                                 List<CheckoutMovement> movements) {
        // Ordena os movimentos por data (mais recentes primeiro)
        movements.sort(Comparator.comparing(CheckoutMovement::getDateTime).reversed());

        movementsList.clear();
        movementsList.addAll(movements);
        tableView.setItems(movementsList);
        tableView.refresh();
    }

    private void configureColumnWidths(TableView<CheckoutMovement> tableView) {
        TableViewUtils.getColumnById(tableView, "dateProperty").setPrefWidth(120.01);
        TableViewUtils.getColumnById(tableView, "paymentProperty").setPrefWidth(143.01);
        TableViewUtils.getColumnById(tableView, "valueProperty").setPrefWidth(148.01);
        TableViewUtils.getColumnById(tableView, "timeProperty").setPrefWidth(61.84);
        TableViewUtils.getColumnById(tableView, "movementTypeProperty").setPrefWidth(197.22);
        TableViewUtils.getColumnById(tableView, "obsProperty").setPrefWidth(289.10);
    }

    private void configurePaymentColumn(TableView<CheckoutMovement> tableView) {
        TableColumn<CheckoutMovement, String> paymentColumn =
                (TableColumn<CheckoutMovement, String>) TableViewUtils.getColumnById(tableView, "paymentProperty");

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

    private void configureObsColumn(TableView<CheckoutMovement> tableView) {
        TableColumn<CheckoutMovement, String> obsColumn =
                (TableColumn<CheckoutMovement, String>) TableViewUtils.getColumnById(tableView, "obsProperty");

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
}