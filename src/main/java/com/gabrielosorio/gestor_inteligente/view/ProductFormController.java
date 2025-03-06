package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.exception.DuplicateProductException;
import com.gabrielosorio.gestor_inteligente.exception.ProductFormException;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.service.base.NotificationService;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;
import com.gabrielosorio.gestor_inteligente.service.impl.NotificationServiceImpl;
import com.gabrielosorio.gestor_inteligente.utils.AutoCompleteField;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class ProductFormController implements Initializable {

    private final Logger log = Logger.getLogger(getClass().getName());
   private Map<String,TextField> fieldMap;

    @FXML
    private TextField idField,barCodeField,descriptionField,costPriceField,sellingPriceField,
    markupField,quantityField,categoryField,supplierField;

    @FXML
    private ListView<String> categoryList,supplierList;

    @FXML
    private Button btnSave,btnCancel;


    private Optional<Product> product;

    private final ProductTbViewController pTbViewController;
    private final ProductManagerController pManagerController;
    private final NotificationService notificationService = new NotificationServiceImpl();
    private final ProductService pService;

    ArrayList<String> categories = new ArrayList<>();

    ArrayList<String> suppliers = new ArrayList<>();

    public ProductFormController(ProductTbViewController pTbViewController, ProductManagerController pManagerController, ProductService pService){
        this.pTbViewController = pTbViewController;
        this.pManagerController = pManagerController;
        this.pService = pService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeFields();
        loadCategoryAndSupplierData();
        setupButtonActions();
    }

    public void setProduct(Optional<Product> product){
        this.product = product;
        populateFields();
    }

    private void setUpNumericField(TextField field){
        field.textProperty().addListener(((observableValue, s, t1) -> {
            String plainText = t1.replaceAll("[^0-9]", "");
            field.setText(plainText);
        }));
    }


    private void calculateAndSetMarkup() {
        try {
            BigDecimal costPrice = BigDecimal.ZERO;
            BigDecimal sellingPrice = BigDecimal.ZERO;

            // Gets the values from the cost price and sales price fields
            if(!costPriceField.getText().isEmpty() && !sellingPriceField.getText().isEmpty()) {
                costPrice = TextFieldUtils.formatCurrency(costPriceField.getText());
                sellingPrice = TextFieldUtils.formatCurrency(sellingPriceField.getText());
            }


            // Checks that the cost price is greater than 0 to avoid division by zero
            if (costPrice.compareTo(BigDecimal.ZERO) > 0 && costPrice.compareTo(sellingPrice) < 0) {
                BigDecimal difference = sellingPrice.subtract(costPrice);
                BigDecimal markup = difference.divide(costPrice,4, RoundingMode.HALF_UP);
                markupField.setText(TextFieldUtils.formatText(markup.toPlainString()));
            } else {
                markupField.setText("0.00");
            }
        } catch (NumberFormatException e) {
            markupField.setText("0.00");
        } catch (ArithmeticException e) {
            markupField.setText("0.00");
        }

    }

    private void setUpFieldNavigation() {
        List<TextField> fields = Arrays.asList(
                barCodeField, descriptionField, quantityField, costPriceField,
                sellingPriceField, markupField, categoryField, supplierField
        );

        fields.forEach(field -> {
            int currentIndex = fields.indexOf(field);

            field.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.TAB) {
                    handleTabNavigation(fields, currentIndex, event.isShiftDown());
                    event.consume(); // Impede o comportamento padrão do Tab
                } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.DOWN) {
                    focusNextField(fields, currentIndex);
                    event.consume();
                } else if (event.getCode() == KeyCode.UP) {
                    focusPreviousField(fields, currentIndex);
                    event.consume();
                } else if (event.getCode() == KeyCode.F2) {
                    save();
                    event.consume();
                }
            });
        });
    }

    private void handleTabNavigation(List<TextField> fields, int currentIndex, boolean isShiftDown) {
        int targetIndex;
        if (isShiftDown) {
            targetIndex = (currentIndex - 1 + fields.size()) % fields.size();
        } else {
            targetIndex = (currentIndex + 1) % fields.size();
        }


        TextField targetField = fields.get(targetIndex);
        targetField.requestFocus();
        TextFieldUtils.lastPositionCursor(targetField);
    }

    // Move o foco para o próximo campo
    private void focusNextField(List<TextField> fields, int currentIndex) {
        int nextIndex = (currentIndex + 1) % fields.size();
        TextField nextField = fields.get(nextIndex);
        nextField.requestFocus();
        TextFieldUtils.lastPositionCursor(nextField);
    }

    // Move o foco para o campo anterior
    private void focusPreviousField(List<TextField> fields, int currentIndex) {
        int previousIndex = (currentIndex - 1 + fields.size()) % fields.size();
        TextField previousField = fields.get(previousIndex);
        previousField.requestFocus();
        TextFieldUtils.lastPositionCursor(previousField);
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
        priceListener(costPriceField,sellingPriceField,quantityField);
        priceListener(sellingPriceField,markupField,costPriceField);
        barCodeField.requestFocus();
        barCodeField.positionCaret(barCodeField.getText().length());
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

    public void cancel(){
        this.product = Optional.empty();
        pManagerController.toggleProductForm();
    }

    public void save() {

        try {
            if (product.isPresent()) {
                Product pToUpdate = ProductFormUtils.updateProduct(product.get(), fieldMap);
                pService.update(pToUpdate);
                showSuccess("Produto atualizado com sucesso.");
            } else {
                Product newProduct = ProductFormUtils.createProduct(fieldMap);
                pService.save(newProduct);
                showSuccess("Produto salvo com sucesso.");
                pTbViewController.refreshProducts();
            }

            pManagerController.toggleProductForm();

        } catch (DuplicateProductException e) {
            var oldProductCode = product.get().getProductCode();
            showError("Código do produto já existe: " + fieldMap.get("idField").getText());
            product.get().setProductCode(oldProductCode);
            idField.setText(String.valueOf(oldProductCode));

        } catch (ProductFormException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSuccess(String message){
        notificationService.showSuccess(message);
    }

    private void showError(String message){
        notificationService.showError(message);
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
        setUpFieldNavigation();
        idField.setOnKeyPressed(keyEvent -> {
            pManagerController.handleShortcut(keyEvent.getCode());
        });
        markupField.setEditable(false);
        markupField.setCursor(Cursor.DEFAULT);
        setUpNumericField(quantityField);
        setUpNumericField(idField);
        setUpNumericField(barCodeField);
    }

    private void priceListener(TextField priceField, TextField nextField, TextField previousField) {
        // Set default value if the field is empty
        if (priceField.getText().isBlank() || priceField.getText().isEmpty()) {
            priceField.setText("0,00");
        } else {
            String formattedValue = TextFieldUtils.formatText(priceField.getText());
            priceField.setText(formattedValue);
        }

        // Configure keyboard listeners
        priceField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.DOWN) {
                focusNextField(Arrays.asList(priceField, nextField), 0); // Move focus to the next field
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.UP) {
                focusPreviousField(Arrays.asList(previousField, priceField), 1); // Move focus to the previous field
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.F2) {
                save(); // Save the form
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.TAB) {
                handleTabNavigation(Arrays.asList(priceField, nextField, previousField), 0, keyEvent.isShiftDown());
                keyEvent.consume();
            }

            // Allow other shortcuts to be handled by the main controller
            pManagerController.handleShortcut(keyEvent.getCode());
        });

        // Position the cursor at the end when clicking on the field
        priceField.setOnMouseClicked(mouseEvent -> {
            priceField.positionCaret(priceField.getText().length());
        });

        // Format the text and recalculate the markup when the value changes
        priceField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            String formattedText = TextFieldUtils.formatText(newValue);
            calculateAndSetMarkup();

            if (!newValue.equals(formattedText)) {
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

    public void lockIDField(){
        lockField(idField);
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

}
