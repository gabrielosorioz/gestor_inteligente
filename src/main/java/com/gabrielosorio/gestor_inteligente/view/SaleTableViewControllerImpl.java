package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.utils.TableViewUtils;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class SaleTableViewControllerImpl implements Initializable, SaleTableViewController {

    private Logger log = Logger.getLogger(getClass().getName());
    ObservableList<SaleProduct> saleItems;

    @FXML
    private TableView<SaleProduct> saleTable;

    @FXML
    private TableColumn<SaleProduct, String> descriptionCol;

    @FXML
    private TableColumn<SaleProduct, String> discountCol;

    @FXML
    private TableColumn<SaleProduct, String> codeCol;

    @FXML
    private TableColumn<SaleProduct, String> quantityCol;

    @FXML
    private TableColumn<SaleProduct, BigDecimal> sellingPriceCol;

    @FXML
    private TableColumn<SaleProduct, BigDecimal> subTotalCol;

    private ObjectProperty<BigDecimal> totalPriceProp = new SimpleObjectProperty<>(BigDecimal.ZERO);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saleItems = FXCollections.observableArrayList();
        saleTable.setItems(saleItems);
        totalPriceProp.set(BigDecimal.ZERO);
        setUpColumns();
    }

    private void setUpColumns() {
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getProduct().getProductCode())));
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        sellingPriceCol.setCellValueFactory(cellData ->  cellData.getValue().unitPriceProperty());
        quantityCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));
        discountCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiscount().toPlainString()));
        subTotalCol.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty());

        monetaryLabel(sellingPriceCol);
        monetaryLabel(subTotalCol);
        setUpQtdColumn();
        setUpDiscountColumn();
        TableViewUtils.resetColumnProps(codeCol,descriptionCol,sellingPriceCol,quantityCol,discountCol,subTotalCol);
        
    }

    private void monetaryLabel(TableColumn<SaleProduct, BigDecimal> currencyColumn){
        currencyColumn.setCellFactory(column -> new TableCell<SaleProduct, BigDecimal>() {
            private final Label currencyLabel = new Label("R$");
            private final Text valueText = new Text();

            {
                currencyLabel.setStyle(
                        "-fx-text-fill: black;"
                );
                currencyLabel.setPrefWidth(35);
                valueText.setTextAlignment(TextAlignment.RIGHT);
                HBox hbox = new HBox(5, currencyLabel, valueText);
                hbox.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hbox);
                setText(null);
            }

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    valueText.setText(TextFieldUtils.formatText(item.setScale(2, RoundingMode.HALF_DOWN).toPlainString()));
                    setGraphic(getGraphic());
                }
            }

        });
    }

    private void setUpQtdColumn() {
        quantityCol.setCellFactory(column -> new TableCell<SaleProduct, String>() {
            private final TextField textField = new TextField();
            {
                setUpTableFieldStyle(textField);
                textField.setTextFormatter(new TextFormatter<>(change -> {
                    if (change.getControlNewText().matches("\\d*")) {
                        return change;
                    }
                    return null;
                }));

                textField.focusedProperty().addListener((obsValue, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        String text = textField.getText().trim();
                        if (text.isEmpty() || text.matches("^0+$")) {
                            textField.setText("1");
                            text = "1";
                        }

                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            try {
                                int newValueInt = Integer.parseInt(text);
                                getTableRow().getItem().setQuantity(newValueInt);
                                refreshTotalPrice();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        try {
                            long newValueInt = Long.parseLong(newValue.isEmpty() || newValue.matches("^0+$") ? "1" : newValue);
                            getTableRow().getItem().setQuantity(newValueInt);
                            refreshTotalPrice();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                });

                textField.setOnKeyPressed(mouseEvent -> {
                    if(mouseEvent.getCode().equals(KeyCode.F3)){
                        showPaymentScreen();
                    }
                });

            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    textField.setText(item);
                    setGraphic(textField);
                }
            }
        });
    }

    private void setUpDiscountColumn() {
        discountCol.setCellFactory(column -> new TableCell<SaleProduct, String>() {
            private final TextField discountField = new TextField();
            private final Label currencyLabel = new Label("R$");
            HBox hbox = new HBox(2, currencyLabel, discountField);

            {
                currencyLabel.setStyle("-fx-text-fill: black; ");
                currencyLabel.setPrefWidth(35);
                discountField.setPrefWidth(95);
                setUpTableFieldStyle(discountField);
                hbox.setAlignment(Pos.CENTER_LEFT);

                if (discountField.getText().isBlank() || discountField.getText().isEmpty()) {
                    discountField.setText("0,00");
                } else {
                    String formattedValue = TextFieldUtils.formatText(discountField.getText());
                    discountField.setText(formattedValue);
                }

                discountField.setOnKeyPressed(keyEvent -> {
                    KeyCode keyCodePressed = keyEvent.getCode();
                    if (keyCodePressed.isArrowKey()) {
                        discountField.positionCaret(discountField.getText().length());
                    }
                });

                discountField.setOnMouseClicked(mouseEvent -> {
                    discountField.positionCaret(discountField.getText().length());
                });

                discountField.textProperty().addListener((observableValue, oldValue, newValue) -> {
                    String formattedText = TextFieldUtils.formatText(newValue);

                    if (!newValue.equals(formattedText)) {
                        Platform.runLater(() -> {
                            discountField.setText(formattedText);
                            discountField.positionCaret(discountField.getText().length());
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                try {
                                    getTableRow().getItem().setDiscount(TextFieldUtils.formatCurrency(discountField.getText()));
                                    refreshTotalPrice();
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

                discountField.setOnKeyPressed(mouseEvent -> {
                    if(mouseEvent.getCode().equals(KeyCode.F3)){
                        showPaymentScreen();
                    }
                });

            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    discountField.setText(item);
                    setGraphic(hbox);
                }
            }
        });
    }

    private void setUpTableFieldStyle(TextField textField) {
        textField.setStyle(
                "-fx-background-color: #eee;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-border-radius: 5px;"
        );

        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                textField.setStyle(
                        "-fx-background-color: #eee;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5px;" +
                                "-fx-border-radius: 5px;" +
                                "-fx-border-width: 1.7px;" +
                                "-fx-border-color: #999999;"
                );
            } else {
                textField.setStyle(
                        "-fx-background-color: #eee;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5px;" +
                                "-fx-border-radius: 5px;"
                );
            }

        });

        textField.setOnMouseExited(event -> {
            if (textField.isFocused()) {
                textField.setStyle(
                        "-fx-background-color: #eee;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5px;" +
                                "-fx-border-radius: 5px;" +
                                "-fx-border-width: 1.7px;" +
                                "-fx-border-color: #999999;"
                );
            } else {
                textField.setStyle(
                        "-fx-background-color: #eee;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5px;" +
                                "-fx-border-radius: 5px;"
                );

            }
        });

        textField.setOnMouseEntered(event -> {
            if (textField.isFocused()) {
                textField.setStyle(
                        "-fx-background-color: #d6d6d6;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5px;" +
                                "-fx-border-radius: 5px;" +
                                "-fx-border-width: 1.7px;" +
                                "-fx-border-color: #999999;"
                );
            } else {
                textField.setStyle(
                        "-fx-background-color: #d6d6d6;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5px;" +
                                "-fx-border-radius: 5px;"
                );

            }
        });

    }

    private void refreshTotalPrice(){
        var totalPrice = new Sale(saleItems).getTotalPrice();
        totalPriceProp.set(totalPrice);
    }

    @Override
    public void add(SaleProduct newItem){
        var searchCode = newItem.getProduct().getProductCode();
        boolean productFound = false;

        for(SaleProduct item: saleItems){
            if (item.getProduct().getProductCode() == searchCode){
                long newQtd = item.getQuantity() + newItem.getQuantity();
                item.setQuantity(newQtd);
                productFound = true;
                break;
            }
        }

        if(!productFound){
            saleItems.add(newItem);
        }

        saleTable.refresh();
        refreshTotalPrice();
        log.info("Product added to sale table: " + "\nID: " + newItem.getProduct().getProductCode() + " BarCode: " + newItem.getProduct().getBarCode().orElse("N/A"));
    }

    @Override
    public void remove(){

    }

    @Override
    public ObjectProperty<BigDecimal> getTotalPriceProperty() {
        return totalPriceProp;
    }

    @Override
    public void showPaymentScreen() {
        PaymentViewHelper.showPaymentScreen(this);
    }

    @Override
    public void clearItems() {
        saleItems.clear();
        saleTable.refresh();
        totalPriceProp.set(BigDecimal.ZERO);
        log.info("All items in the sales table have been removed. ");
    }

    @Override
    public ObservableList<SaleProduct> getItems(){
        return saleItems;
    }


}