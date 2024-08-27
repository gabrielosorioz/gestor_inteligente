package com.gabrielosorio.gestor_inteligente.controller;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.utils.AutoCompleteField;
import com.gabrielosorio.gestor_inteligente.utils.StockDataUtils;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class StockRegisterFormController implements Initializable {

    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private TextField idField,barCodeField,descriptionField,costPriceField,sellingPriceField,
    markupField,quantityField;

    @FXML
    private TextField categoryField,supplierField;

    @FXML
    private ListView<String> categoryList;

    @FXML
    private ListView<String> supplierList;

    @FXML
    private Button btnSave;

    private Stock stock;

    private StockTableViewController stockTableViewController;

    ArrayList<String> categories = new ArrayList<>();

    ArrayList<String> suppliers = new ArrayList<>();

    public void setStock(Stock stock){
        this.stock = stock;
        populateFields();
    }

    public void setStockTableViewController(StockTableViewController stockTableViewController){
        this.stockTableViewController = stockTableViewController;
    }

    private void fetchCategoryData(){
        String filePath = "src/main/resources/com/gabrielosorio/gestor_inteligente/data/categories.json";
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            StringBuilder jsonString = new StringBuilder();

            String line;

            while((line = reader.readLine()) != null){
                jsonString.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonString.toString());

            jsonArray.forEach(jsonObject -> {
                JSONObject categoryJsonObject = (JSONObject) jsonObject;
                String description = categoryJsonObject.getString("category");
                int id = categoryJsonObject.getInt("id");
                final Category category = new Category(id,description);

                categories.add(category.getDescription());

            });

        } catch (FileNotFoundException e) {
            System.out.println("Arquivo json das categorias não foi encontrado " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Arquivo json das categorias não foi encontrado " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void fetchSupplierData(){
        String filePath = "src/main/resources/com/gabrielosorio/gestor_inteligente/data/suppliers.json";
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonString = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                jsonString.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonString.toString());

            jsonArray.forEach(e ->{
                JSONObject supplierJsonObject = (JSONObject) e;
                String description = supplierJsonObject.getString("name");
                int id = supplierJsonObject.getInt("id");
                final Supplier supplier = new Supplier(id,description);
                suppliers.add(supplier.getName());
            });

        } catch (FileNotFoundException e) {
            log.severe("Error loading suppliers json, file not founded" + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.severe("Error loading suppliers json" + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void populateFields() {

        final Stock productStock = stock;

        final String id = String.valueOf(productStock.getProduct().getProductCode());
        final String barCode = productStock.getProduct().getBarCode();
        final String description = productStock.getProduct().getDescription();
        final String costPrice = productStock.getProduct().getCostPrice().toPlainString();
        final String sellingPrice = productStock.getProduct().getSellingPrice().toPlainString();
        final String markupPercent = String.valueOf(productStock.getProduct().getMarkupPercent());
        final String quantity = String.valueOf(productStock.getQuantity());
        final String category = String.valueOf(productStock.getProduct().getCategory().getDescription());
        final String supplier = String.valueOf(productStock.getProduct().getSupplier().getName());

        this.idField.setText(id);
        this.barCodeField.setText(barCode);
        this.descriptionField.setText(description);
        this.costPriceField.setText(costPrice);
        this.sellingPriceField.setText(sellingPrice);
        this.markupField.setText(markupPercent);
        this.quantityField.setText(quantity);
        this.categoryField.setText(category);
        this.supplierField.setText(supplier);

        priceListener(costPriceField);
        priceListener(sellingPriceField);

    }

    private void saveProduct(){
        Stock newStockRegister = createUpdatedStock();
        StockDataUtils.updateStock(newStockRegister);
        stockTableViewController.updateStockUI(newStockRegister);
    }

    private Product createUpdatedProduct(){
        return Product.builder()
                .id(stock.getProduct().getId())
                .productCode(Integer.parseInt(idField.getText()))
                .barCode(barCodeField.getText())
                .description(descriptionField.getText())
                .costPrice(TextFieldUtils.formatCurrency(costPriceField.getText()))
                .sellingPrice(TextFieldUtils.formatCurrency(sellingPriceField.getText()))
                .supplier(new Supplier(stock.getProduct().getSupplier().getId(), stock.getProduct().getSupplier().getName()))
                .category(new Category(stock.getProduct().getCategory().getId(), stock.getProduct().getCategory().getDescription()))
                .status(stock.getProduct().getStatus())
                .dateCreate(stock.getProduct().getDateCreate())
                .dateUpdate(Timestamp.valueOf(LocalDateTime.now()))
                .dateDelete(null)
                .build();
    }

    private Stock createUpdatedStock(){
        Product updatedProduct = createUpdatedProduct();
        Stock updatedStock = new Stock(updatedProduct,  Integer.parseInt(quantityField.getText()));
        updatedStock.setId(stock.getId());
        updatedStock.setLastUpdate(Timestamp.valueOf(LocalDateTime.now()));
        return updatedStock;
    }

    private void priceListener(TextField priceField){
        if(priceField.getText().isBlank() || priceField.getText().isEmpty()){
            priceField.setText("0,00");
        } else {
            String formattedValue = TextFieldUtils.formatText(priceField.getText());
            priceField.setText(formattedValue);
        }

        priceField.setOnKeyPressed(keyEvent -> {
            KeyCode keyCodePressed = keyEvent.getCode();
            if(keyCodePressed.isArrowKey()){
                priceField.positionCaret(priceField.getText().length());
            }
        });

        priceField.setOnMouseClicked(mouseEvent -> {
            priceField.positionCaret(priceField.getText().length());
        });

        priceField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            String formattedText  = TextFieldUtils.formatText(newValue);

            if(!newValue.equals(formattedText)) {
                Platform.runLater(() -> {
                    priceField.setText(formattedText);
                    priceField.positionCaret(priceField.getText().length());
                });
            }
        });
    }

    private void setUpperCaseField(List<TextField> fields){
        fields.forEach(field -> {
            TextFieldUtils.setUpperCaseTextFormatter(field);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fetchSupplierData();
        fetchCategoryData();
        List<TextField> fields = new ArrayList<>(Arrays.asList(
                idField,
                barCodeField,descriptionField,
                costPriceField,sellingPriceField,
                markupField,quantityField)
        );

        setUpperCaseField(fields);
        categoryList.getItems().addAll(categories);
        supplierList.getItems().addAll(suppliers);
        AutoCompleteField auto = new AutoCompleteField(categoryField,categoryList);
        AutoCompleteField auto2 = new AutoCompleteField(supplierField,supplierList);

        btnSave.setOnMouseClicked(mouseEvent -> {
            saveProduct();
        });

    }


}
