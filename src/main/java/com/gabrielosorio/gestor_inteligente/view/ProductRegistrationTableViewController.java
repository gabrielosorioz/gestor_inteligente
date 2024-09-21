package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.Repository;
import com.gabrielosorio.gestor_inteligente.repository.storage.H2DBProductStrategy;
import com.gabrielosorio.gestor_inteligente.repository.storage.PSQLProductStrategy;
import com.gabrielosorio.gestor_inteligente.utils.StockDataUtils;
import com.gabrielosorio.gestor_inteligente.utils.TableViewUtils;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ProductRegistrationTableViewController implements Initializable {

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, String> costPriceCol;

    @FXML
    private TableColumn<Product, String> descriptionCol;

    @FXML
    private TableColumn<Product, String> idCol;

    @FXML
    private TableColumn<Product, String> stockColumn;

    @FXML
    private TableColumn<Product, String> sellingPriceCol;

    @FXML
    private TableView<Product> productsTable;

    private Repository<Product> productRepository;

    private List<Product> allProducts;

    private FilteredList<Product> filteredTableProducts;

    private ObservableList<Product> productsList;

    private final Logger log = Logger.getLogger(getClass().getName());

    private void setUpColumns() {

        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory().map(Category::getDescription).orElse("N/A")));
        costPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(TextFieldUtils.formatText(cellData.getValue().getCostPrice().toPlainString())));
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getProductCode())));
        stockColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));
        sellingPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(TextFieldUtils.formatText(cellData.getValue().getSellingPrice().toPlainString())));

        // set monetary label
        monetaryLabel(costPriceCol);
        monetaryLabel(sellingPriceCol);

        // Remove resizable and reorderable properties
        TableViewUtils.resetColumnProps(idCol,descriptionCol,costPriceCol,sellingPriceCol,stockColumn,categoryColumn);
    }


    private void monetaryLabel(TableColumn<Product,String> currencyColumn){
        currencyColumn.setCellFactory(column -> {
            TableCell<Product, String> cell = new TableCell<>() {
                private final Label currencyLabel = new Label("R$");
                private final Text valueText = new Text();

                {
                    currencyLabel.setStyle(
                             "-fx-text-fill: black;"
                    );
                    currencyLabel.setPrefWidth(50);
                    valueText.setTextAlignment(TextAlignment.RIGHT);
                    HBox hbox = new HBox(5, currencyLabel, valueText);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);
                    setText(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        valueText.setText(item);
                        setGraphic(getGraphic());
                    }
                }
            };
            return cell;
        });
    }

    public void updateStockUI(Stock updatedStock) {
//        if (products != null && !products.isEmpty()) {
//            for (int i = 0; i < products.size(); i++) {
//                Product  = products.get(i);
//                if (stock.getId() == updatedStock.getId()) {
//                    products.set(i, updatedStock);
//                    stockTable.refresh();
//                    break;
//                }
//            }
//        } else {
//            log.warning("Stock list is empty or null.");
//        }
    }

    public void searchFilteredStock(String search){
        if(search == null || search.trim().isEmpty()){
            productsTable.setItems(productsList);
            productsTable.refresh();
            return;
        }

        final var searchLower = search.toLowerCase();

        productsTable.setItems(productsList.filtered(product -> {
            if(isNumeric(searchLower)){
                var searchNumber = Long.parseLong(searchLower);
                var pCode = product.getProductCode();
                var pBarCodeOpt = product.getBarCode();

                if(searchNumber == pCode){
                    return true;
                }

                return pBarCodeOpt
                        .filter(barCode -> isNumeric(barCode))
                        .map(barCode -> Long.parseLong(barCode))
                        .filter(barCodeLong -> barCodeLong == searchNumber)
                        .isPresent();
            }

            return product.getDescription().toLowerCase().contains(searchLower);

        }));
        productsTable.refresh();
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+"); // Verifica se a string contém apenas dígitos
    }

    private List<Product> fetchProducts(){
        var productStrategy = PSQLProductStrategy.getInstance();
        productRepository = new ProductRepository(productStrategy);
        productRepository.init(PSQLProductStrategy.getInstance());
        var allProducts = new ArrayList<>(productRepository.findAll());
        return allProducts;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allProducts = fetchProducts();
        productsList = FXCollections.observableArrayList(allProducts);
        productsTable.setItems(productsList);
        productsTable.setFocusTraversable(false);
        setUpColumns();
    }
}
