package com.gabrielosorio.gestor_inteligente.controller;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.utils.StockDataUtils;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class CheckoutTabController implements Initializable {

    private HashMap<String, Product> productData = new HashMap<>();

    private HashMap<String, SaleProduct> cartProduct = new HashMap<>();
    private ObservableList<SaleProduct> cartProductObsList = FXCollections.observableArrayList();
    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private TableView<SaleProduct> cartTable;

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

    @FXML
    private Tab checkoutTab;

    @FXML
    private HBox btnAddNewCheckoutTab;

    @FXML
    private TextField searchField, qtdField;

    @FXML
    private AnchorPane content;

    @FXML
    private Label priceLbl;

    private final CheckoutTabPaneController checkoutTabPaneController;

    public CheckoutTabController(CheckoutTabPaneController checkoutTabPaneController) {
        this.checkoutTabPaneController = checkoutTabPaneController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fetchProductsData();
        setUpColumns();
        setUpEvents();
        setDropShadowToBody();
        cartTable.setItems(cartProductObsList);
        cartTable.setFocusTraversable(false);
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


        codeCol.setResizable(false);
        descriptionCol.setResizable(false);
        sellingPriceCol.setResizable(false);
        quantityCol.setResizable(false);
        discountCol.setResizable(false);
        subTotalCol.setResizable(false);

        codeCol.setReorderable(false);
        descriptionCol.setReorderable(false);
        sellingPriceCol.setReorderable(false);
        discountCol.setReorderable(false);
        subTotalCol.setReorderable(false);
        quantityCol.setReorderable(false);

        codeCol.setSortable(false);
        descriptionCol.setSortable(false);
        sellingPriceCol.setSortable(false);
        subTotalCol.setSortable(false);
        discountCol.setSortable(false);
        quantityCol.setSortable(false);
    }

    private void setUpEvents() {
        setFocusOnSearchField();
        setUpTabActions();
        setUpEventSearchField();
        setUpEventQtdField();
        setUpEventBtnAddCheckout();
    }

    private void setFocusOnSearchField() {
        Platform.runLater(() -> {
            if (checkoutTab.isSelected()) {
                searchField.requestFocus();
            }
        });
    }

    private void setUpSelectTabListener() {
        if (checkoutTab != null) {
            checkoutTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    Platform.runLater(() -> {
                        searchField.requestFocus();
                    });
                }
            });
        }
    }

    private void setUpEventCloseTab() {
        Platform.runLater(() -> {
            checkoutTab.setOnCloseRequest(event -> {
                if (checkoutTabPaneController.getListTabLength() == 1) {
                    event.consume();
                    Platform.runLater(() -> {
                        searchField.requestFocus();
                    });
                }
            });
        });
    }

    private void setUpTabActions() {
        setUpSelectTabListener();
        setUpEventCloseTab();
    }

    private void setUpEventSearchField() {
        searchField.setOnKeyPressed(keyEvent -> {
            KeyCode pressedKey = keyEvent.getCode();
            String search = searchField.getText().trim();
            String qtdStr = qtdField.getText().trim();
            boolean isCodeFieldEmpty = search.isEmpty() || search.isBlank();

            if (pressedKey.equals(KeyCode.F3)) {
                createSale();
            }

            if (pressedKey.equals(KeyCode.ENTER)) {
                if (isCodeFieldEmpty) {
                    qtdField.requestFocus();
                }

                if (!isCodeFieldEmpty) {
                    if (cartProduct.containsKey(search)) {
                        increaseProductToCart(search, Long.parseLong(qtdStr));
                    } else {
                        insertProductToCart(search, Long.parseLong(qtdStr));
                    }
                    searchField.clear();
                    qtdField.setText("1");
                }
            }
        });
    }

    private void setDropShadowToBody() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#b7b7b7"));
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        content.setEffect(shadow);
    }

    private void setUpEventQtdField() {
        qtdField.setText("1");


        qtdField.textProperty().addListener(((observableValue, s, t1) -> {
            String plainText = t1.replaceAll("[^0-9]", "");
            qtdField.setText(plainText);
        }));

        qtdField.focusedProperty().addListener((obsValue, oldValue, newValue) -> {
            if (qtdField.getText().isEmpty() || qtdField.getText().matches("^0+$")) {
                qtdField.setText("1");
            }
        });

        qtdField.setOnKeyPressed(event -> {
            KeyCode pressedKey = event.getCode();

            if (KeyCode.F3 == event.getCode()) {
                createSale();
            }

            if (pressedKey.equals(KeyCode.ENTER)) {
                searchField.requestFocus();
                if (qtdField.getText().isBlank()) {
                    qtdField.setText("1");
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
                        createSale();
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
                        createSale();
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

    private void setUpEventBtnAddCheckout() {
        btnAddNewCheckoutTab.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 1) {
                checkoutTabPaneController.addNewCheckoutTab();
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

    private Product getProductData(String id) {
        Product product = productData.get(id);
        if (product != null) {
            return product;
        } else {
            log.severe("Product not found for id " + id);
        }
        throw new IllegalArgumentException("Invalid Product ID: " + id);
    }

    private void insertProductToCart(String id, long quantity) {
        Product product = getProductData(id);
        ProductValidator.validate(product);
        final SaleProduct newSaleItem = new SaleProduct(product);
        newSaleItem.setQuantity(quantity);

        cartProduct.put(String.valueOf(newSaleItem.getProduct().getProductCode()), newSaleItem);
        if (newSaleItem.getProduct().hasBarCode()) {
            cartProduct.put(newSaleItem.getProduct().getBarCode(), newSaleItem);
        }

        cartProductObsList.add(newSaleItem);
        cartTable.refresh();
        refreshTotalPrice();
        log.info("Product added to the list of products for sale." + " BarCode: " + cartProduct.get(newSaleItem.getProduct().getBarCode()).getProduct().getBarCode() + " ID: " + cartProduct.get(String.valueOf(newSaleItem.getProduct().getProductCode())).getProduct().getProductCode());
    }

    private void increaseProductToCart(String id, long quantity) {
        SaleProduct itemSale = searchProductInCart(id);
        itemSale.setQuantity(itemSale.getQuantity() + quantity);
        cartTable.refresh();
    }

    private SaleProduct searchProductInCart(String search) {
        SaleProduct saleProduct = cartProduct.get(search);
        if (saleProduct != null) {
            return saleProduct;
        } else {
            log.severe("Cart product not found: " + search);
        }
        throw new IllegalArgumentException("Invalid cart product ID" + search);
    }

    private void fetchProductsData() {
        StockDataUtils.fetchStockData().forEach(item -> {
            Product product = item.getProduct();
            String productCode = String.valueOf(product.getProductCode());
            String barCode = product.getBarCode();
            productData.put(productCode, product);
            productData.put(barCode, product);
        });
    }

    private void createSale() {
        HashSet<SaleProduct> itemSet = new HashSet<>(cartProduct.values());
        ArrayList<SaleProduct> items = new ArrayList<>(itemSet);
        final Sale sale = new Sale(items);
        showPaymentView(sale);
    }

    private void refreshTotalPrice() {
        HashSet<SaleProduct> itemSet = new HashSet<>(cartProduct.values());
        ArrayList<SaleProduct> items = new ArrayList<>(itemSet);
        Sale sale = new Sale(items);
        final String finalPrice = TextFieldUtils.formatText(sale.getTotalPrice().toPlainString());
        priceLbl.setText(finalPrice);

    }

    private void showPaymentView(Sale sale) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/PaymentView.fxml"));
            Stage paymentRoot = new Stage();
            PaymentViewController controller = new PaymentViewController(sale);
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load());
            paymentRoot.setScene(scene);
            paymentRoot.show();

        } catch (Exception e) {
            log.severe("ERROR at load payment view: " + e.getMessage());
            e.printStackTrace();
        }
    }


}