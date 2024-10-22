package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.exception.ProductException;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.service.ProductService;
import com.gabrielosorio.gestor_inteligente.utils.AutoCompleteField;
import com.gabrielosorio.gestor_inteligente.utils.StockDataUtils;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class ProductFormController implements Initializable {

    private final Logger log = Logger.getLogger(getClass().getName());
   private Map<String,TextField> fieldMap;

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
    private Button btnSave,btnCancel;


    private Optional<Product> product;

    private final ProductTbViewController pTbViewController;
    private final ProductManagerController pManagerController;
    private final ProductService pService;

    ArrayList<String> categories = new ArrayList<>();

    ArrayList<String> suppliers = new ArrayList<>();

    public ProductFormController(ProductTbViewController pTbViewController, ProductManagerController pManagerController, ProductService pService){
        this.pTbViewController = pTbViewController;
        this.pManagerController = pManagerController;
        this.pService = pService;
    }

    public void setProduct(Optional<Product> product){
        this.product = product;
        populateFields();
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
        ProductFormUtils.populateProductFields(product,fieldMap);
        priceListener(costPriceField);
        priceListener(sellingPriceField);

    }

    private void mapFields(){
        this.fieldMap = new HashMap<>();
        fieldMap.put("idField",idField);
        fieldMap.put("barCodeField",barCodeField);
        fieldMap.put("descriptionField",descriptionField);
        fieldMap.put("costPriceField",costPriceField);
        fieldMap.put("sellingPriceField",sellingPriceField);
        fieldMap.put("markupField",markupField);
        fieldMap.put("quantityField",quantityField);
        fieldMap.put("categoryField",categoryField);
        fieldMap.put("supplierField",supplierField);
    }

    private void setUpAutoCompleteFields(){
        new AutoCompleteField(categoryField,categoryList);
        new AutoCompleteField(supplierField,supplierList);
    }

    private void cancel(){
        this.product = Optional.empty();
        pManagerController.toggleProductForm();
    }

    private void save() {
        product.ifPresent(product -> {
            var pToUpdate = ProductFormUtils.updateProduct(product,fieldMap);
            pService.update(pToUpdate);
        });

        if(product.isEmpty()){
            var newP = ProductFormUtils.createProduct(fieldMap);
            pService.save(newP);
        }

    }

    private void loadCategoryAndSupplierData(){
        fetchSupplierData();
        fetchCategoryData();
        categoryList.getItems().addAll(categories);
        supplierList.getItems().addAll(suppliers);
        setUpAutoCompleteFields();

    }

    private void initializeFields(){
        mapFields();
        setUpperCaseTextFormatter();
        lockField(idField);
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

    private void setUpperCaseTextFormatter(){
        List<TextField> fields = new ArrayList<>(Arrays.asList(idField,barCodeField,descriptionField,costPriceField,sellingPriceField,
                markupField,quantityField,categoryField,supplierField));
        fields.forEach(field -> {
            TextFieldUtils.setUpperCaseTextFormatter(field);
        });
    }

    private void setupButtonActions() {
        btnSave.setOnMouseClicked(mouseEvent -> {
            save();
            pManagerController.toggleProductForm();
        });
        btnCancel.setOnMouseClicked(mouseEvent -> cancel());
    }

    private void setLockFieldStyle(TextField field) {
        field.setStyle(
                "-fx-border-color: #e0e0e0;" +
                        "-fx-text-fill: #7f7f7f;"
        );
        field.setOnMouseEntered(mouseEvent -> {
            if (!field.isEditable()) {
                field.setStyle(
                        "-fx-cursor: hand;" +
                                "-fx-border-color: #e0e0e0;"
                );
            }
        });
    }

    private void lockField(TextField field) {
        field.setEditable(false);
        setLockFieldStyle(field);
        setupActionField(field);
    }

    private static void unlockField(TextField field) {
        field.setEditable(true);
        field.setStyle("");
        field.requestFocus();
    }

    private void setupActionField(TextField field) {
        field.setOnMouseClicked(click -> {
            if(!field.isEditable()){

                showCodeAlert();
            }
        });
    }

    public void showCodeAlert() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/AlertMessage.fxml"));
            fxmlLoader.setController(new AlertMessageController());
            Node codeAlert = fxmlLoader.load();
            AlertMessageController alertController = fxmlLoader.getController();

            alertController.setOnYesAction(v -> {
                unlockField(idField);
            });

            pManagerController.addContent(codeAlert);
            codeAlert.setLayoutX(450);
            codeAlert.setLayoutY(250);

        } catch (Exception e) {
            log.severe("ERROR at load code alert message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void lockIDField(){

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeFields();
        loadCategoryAndSupplierData();
        setupButtonActions();
    }


}
