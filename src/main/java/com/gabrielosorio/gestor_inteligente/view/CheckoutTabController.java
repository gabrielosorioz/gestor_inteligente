package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.Repository;
import com.gabrielosorio.gestor_inteligente.repository.storage.PSQLProductStrategy;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class CheckoutTabController implements Initializable, ShortcutHandler{

    private HashMap<String, Product> productData = new HashMap<>();

    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private Tab checkoutTab;

    @FXML
    private HBox btnAddNewCheckoutTab,btnNext,btnPrevious;

    @FXML
    private TextField searchField, qtdField;

    @FXML
    private AnchorPane mainContent,content;

    @FXML
    private Label totalPriceLbl;

    private SaleTableViewController saleTableOp;

    private Node removeItemsAlert;
    private Node productNotFoundAlert;
    private final CheckoutTabPaneController checkoutTabPaneController;
    private InfoMessageController infoController;
    private AlertMessageController alertController;



    public CheckoutTabController(CheckoutTabPaneController checkoutTabPaneController) {
        this.checkoutTabPaneController = checkoutTabPaneController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            loadTableView();
            fetchProductsData();
            setUpEvents();
            setDropShadowToBody();
            showTotalPrice();
        });
    }

    @Override
    public void handleShortcut(KeyCode keyCode) {
        if (keyCode == KeyCode.F4) {
            showRemoveItemsAlert();
        }

        if (keyCode == KeyCode.F3) {
            finalizeSale();
        }
    }

    private void setUpSwitchTabEvent(){

        var tabPane = checkoutTabPaneController.getTabPane();

        btnPrevious.setOnMouseClicked(mouseEvent -> {
            int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
            if(currentIndex > 0){
                tabPane.getSelectionModel().select(currentIndex - 1);
            }
        });

        btnNext.setOnMouseClicked(mouseEvent -> {
            int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
            if(currentIndex < tabPane.getTabs().size() - 1){
                tabPane.getSelectionModel().select(currentIndex + 1);
            }
        });

    }

    private void removeItems(){
        saleTableOp.clearItems();
    }

    private void setUpEvents() {
        setUpSwitchTabEvent();
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

        mainContent.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                searchField.requestFocus();
            }
        });

        checkoutTab.getTabPane().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
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
            var qtdStr = qtdField.getText().trim();
            boolean isCodeFieldEmpty = search.isEmpty() || search.isBlank();

            if (pressedKey.equals(KeyCode.F3)) {
                finalizeSale();
            }

            if(keyEvent.getCode().equals(KeyCode.F4)){
                showRemoveItemsAlert();
            }

            if (pressedKey.equals(KeyCode.ENTER)) {
                if (isCodeFieldEmpty) {
                    qtdField.requestFocus();
                }

                if (!isCodeFieldEmpty) {
                    addItem(search,Integer.parseInt(qtdStr));
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
                finalizeSale();
            }

            if(event.getCode().equals(KeyCode.F4)){
                showRemoveItemsAlert();
            }

            if (pressedKey.equals(KeyCode.ENTER)) {
                searchField.requestFocus();
                if (qtdField.getText().isBlank()) {
                    qtdField.setText("1");
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

    private Optional<Product> getProductData(String id) {
        Product product = productData.get(id);
        return Optional.ofNullable(product);
    }

    private void fetchProductsData() {
        Repository<Product> prodRepository = new ProductRepository(PSQLProductStrategy.getInstance());
        prodRepository.init(PSQLProductStrategy.getInstance());
        prodRepository.findAll().forEach(product -> {
           String productCode = String.valueOf(product.getProductCode());
           Optional<String> barCode = product.getBarCode();

           productData.put(productCode,product);
           barCode.ifPresent(bc -> productData.put(bc,product));
        });

    }

    private void showTotalPrice(){
        saleTableOp.getTotalPriceProperty().addListener((obsVal, oldVal, newVal) -> {
            if(newVal != null){
                totalPriceLbl.setText(TextFieldUtils.formatText(newVal.setScale(2,RoundingMode.HALF_UP).toPlainString()));
                log.info("Total Price Label Updated: " + newVal);
            }
        });
    }

    protected void showRemoveItemsAlert() {
        try {
            if (removeItemsAlert == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/AlertMessage.fxml"));
                alertController = new AlertMessageController();
                fxmlLoader.setController(alertController);
                removeItemsAlert = fxmlLoader.load();

                AlertMessageController alertController = fxmlLoader.getController();
                alertController.setText("Deseja limpar o caixa ?");

                alertController.setOnYesAction(v -> {
                    removeItems();
                    removeItemsAlert = null;
                });

                removeItemsAlert.setLayoutX(450);
                removeItemsAlert.setLayoutY(250);
            }

            if (!mainContent.getChildren().contains(removeItemsAlert)) {
                mainContent.getChildren().add(removeItemsAlert);
                alertController.getYesButton().setOnKeyPressed(e ->{
                    if(e.getCode().equals(KeyCode.S)){
                        removeItems();
                        removeItemsAlert = null;
                        alertController.close();
                    } else if (e.getCode().equals(KeyCode.N)){
                        alertController.close();
                    }
                });
                alertController.getYesButton().requestFocus();

            }

        } catch (Exception e) {
            log.severe("ERROR at load code alert message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfoMessageAlert(String message) {
        try {
            if (productNotFoundAlert == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/InfoMessage.fxml"));
                infoController = new InfoMessageController(); // Create the controller only once
                fxmlLoader.setController(infoController);
                productNotFoundAlert = fxmlLoader.load();

                infoController.setText(message);

                productNotFoundAlert.setLayoutX(450);
                productNotFoundAlert.setLayoutY(250);
            }

            if (!mainContent.getChildren().contains(productNotFoundAlert)) {
                mainContent.getChildren().add(productNotFoundAlert);
                infoController.setText(message);


                // Focus and button event, even on subsequent displays
                infoController.getBtnOk().requestFocus();
                infoController.getBtnOk().setOnKeyPressed(e -> {
                    if (e.getCode().equals(KeyCode.ENTER)) {
                        infoController.close();
                    }
                });
            }
        } catch (Exception e) {
            log.severe("ERROR at load code alert message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTableView() {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/SaleTableView.fxml"));
            SaleTableViewControllerImpl saleTableViewControllerImpl = new SaleTableViewControllerImpl(this);
            loader.setController(saleTableViewControllerImpl);
            TableView tableView = loader.load();
            configureTableViewLayout(tableView);
            content.getChildren().add(0, tableView);
            this.saleTableOp = loader.getController();
        } catch (IOException e) {
            log.severe("Error loading SaleTableView :" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void configureTableViewLayout(TableView<SaleProduct> tableView){
        AnchorPane.setRightAnchor(tableView,5.0);
        AnchorPane.setBottomAnchor(tableView,54.0);
        AnchorPane.setLeftAnchor(tableView,5.0);
    }

    private void addItem(String search, long quantity){
        Optional<Product> productOptional = getProductData(search);

        if(productOptional.isEmpty()){
            showInfoMessageAlert("Produto não encontrado.");
            searchField.clear();
            qtdField.setText("1");
            return;
        }

        var product = productOptional.get();
        final var newItem = new SaleProduct(product,quantity);
        saleTableOp.add(newItem);
        searchField.clear();
        qtdField.setText("1");

    }

    private void finalizeSale(){
        if(saleTableOp.getItems().isEmpty()){
            showInfoMessageAlert("Não foi possível finalizar a venda. Caixa vazio");
            return;
        }
        saleTableOp.showPaymentScreen();
    }

}