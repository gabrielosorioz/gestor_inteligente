package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.events.*;
import com.gabrielosorio.gestor_inteligente.events.listeners.ProductManagerCancelEvent;
import com.gabrielosorio.gestor_inteligente.events.listeners.ProductManagerListener;
import com.gabrielosorio.gestor_inteligente.events.listeners.ProductManagerSaveEvent;
import com.gabrielosorio.gestor_inteligente.exception.DuplicateProductException;
import com.gabrielosorio.gestor_inteligente.exception.ProductFormException;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.service.base.NotificationService;
import com.gabrielosorio.gestor_inteligente.service.base.ProductService;
import com.gabrielosorio.gestor_inteligente.service.impl.NotificationServiceImpl;
import com.gabrielosorio.gestor_inteligente.utils.AutoCompleteField;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import com.gabrielosorio.gestor_inteligente.validation.ProductValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class ProductFormController implements Initializable {

    private final Logger log = Logger.getLogger(getClass().getName());

    private ProductFormEventBus formEventBus;

    @FXML
    private TextField idField,barCodeField,descriptionField,costPriceField,sellingPriceField,
    markupField,quantityField,categoryField,supplierField;

    @FXML
    private ListView<String> categoryList,supplierList;

    @FXML
    private Button btnSave,btnCancel;

    private Optional<Product> product;
    private final NotificationService notificationService = new NotificationServiceImpl();
    private final ProductService pService;

    public ProductFormController(ProductService pService){

        this.pService = pService;
        formEventBus = ProductFormEventBus.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new UIFormManager().initializeFields();
        new EventHandler().init();
        setupButtons();

    }

    private void setupButtons(){
        setupBtnSave();
        setupBtnCancel();
    }

    private void setupBtnSave(){
        btnSave.setOnMouseClicked(mouseEvent -> {
            new ProductFormServices().save();
        });
    }

    private void setupBtnCancel(){
        btnCancel.setOnMouseClicked(mouseEvent -> new ProductFormServices().save());
    }


    private static void unlockField(TextField field) {
        field.setEditable(true);
        field.setStyle("");
        field.requestFocus();
    }

    /** Lógica relacionada a notificação no sistema */
    private void showSuccess(String message){
        notificationService.showSuccess(message);
    }

    private void showError(String message){
        notificationService.showError(message);
    }

    /** Logica relacionada ao formulario */

    private void loadProductIntoForm(Product prod) {
        product = Optional.of(prod);
        idField.setText(prod != null ? String.valueOf(prod.getProductCode()) : "");
        barCodeField.setText(prod != null ? prod.getBarCode().orElse("") : "");
        descriptionField.setText(prod != null ? prod.getDescription() : "");
        costPriceField.setText(prod != null ? prod.getCostPrice().toPlainString() : "");
        sellingPriceField.setText(prod != null ? prod.getSellingPrice().toPlainString() : "");
        quantityField.setText(prod != null ? String.valueOf(prod.getQuantity()) : "");
        markupField.setText(prod != null ? String.valueOf(prod.getMarkupPercent()) : "");
        categoryField.setText(prod != null ? prod.getCategory().map(Category::getDescription).orElse("") : "");
        supplierField.setText(prod != null ? prod.getSupplier().map(Supplier::getName).orElse("") : "");


        barCodeField.requestFocus();
        barCodeField.positionCaret(barCodeField.getText().length());
    }

    private void loadCleanForm() {
        List<TextField> fields = Arrays.asList(
                idField, barCodeField,
                descriptionField, costPriceField,
                sellingPriceField, markupField,
                quantityField, categoryField,
                supplierField);

        fields.forEach(TextInputControl::clear);
        idField.setPromptText("Novo Código");
    }


    private Node loadChangeCodeWarning() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/AlertMessage.fxml"));
            fxmlLoader.setController(new AlertMessageController());
            Node codeAlert = fxmlLoader.load();
            AlertMessageController alertController = fxmlLoader.getController();

            alertController.setOnYesAction(v -> {
                unlockField(idField);
            });

            codeAlert.setLayoutX(450);
            codeAlert.setLayoutY(250);
            return codeAlert;

        } catch (Exception e) {
            log.severe("ERROR at load code alert message: " + e.getMessage());
        }
        return null;
    }

    private class EventHandler implements ProductManagerListener {

        void init(){
            ProductManagerEventBus.getInstance().register(this);
        }

        @Override
        public void onSelectProduct(ProductSelectionEvent productSelectionEvent) {
            Product product = productSelectionEvent.getProduct();
            loadProductIntoForm(product);
        }

        @Override
        public void onToggleProductForm(ProductFormToggleEvent productFormToggleEvent) {
            if (!productFormToggleEvent.isFormVisible()) {
                new UIFormManager().lockField(idField);
            }
        }

        @Override
        public void onAddNewProduct(ProductAddEvent productAddEvent) {
            loadCleanForm();
        }

        @Override
        public void onSaveProduct(ProductManagerSaveEvent productManagerSaveEvent) {
            new ProductFormServices().save();
        }

        @Override
        public void onCancel(ProductManagerCancelEvent productManagerCancelEvent) {
            new ProductFormServices().cancel();
        }
    }

    private class DataLoader {

        private List<String> loadCategoriesDescription() {
            String filePath = "src/main/resources/com/gabrielosorio/gestor_inteligente/data/categories.json";
            try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                StringBuilder jsonString = new StringBuilder();
                List<String> categories = new ArrayList<>();
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
                return categories;

            } catch (IOException e) {
                System.out.println("Arquivo json das categorias não foi encontrado " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        private List<String> loadSuppliersDescription(){
            String filePath = "src/main/resources/com/gabrielosorio/gestor_inteligente/data/suppliers.json";
            try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                StringBuilder jsonString = new StringBuilder();
                String line;
                List<String> suppliers = new ArrayList<>();

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

                return suppliers;
            } catch (IOException e) {
                log.severe("Error loading suppliers json" + e.getMessage());
                throw new RuntimeException(e);
            }


        }
    }

    private class UIFormManager {

        private void initializeFields(){
            setUpperCaseTextFormatter();
            setUpAutoCompleteFields();
            setUpFieldNavigation();
            lockField(idField);
            addEventOnIdField(idField);
            setUpEventOnIdField();
            setUpMarkupField();
            setUpNumericField();
            priceListener(costPriceField,sellingPriceField,quantityField);
            priceListener(sellingPriceField,markupField,costPriceField);
        }

        private void setUpMarkupField(){
            markupField.setEditable(false);
            markupField.setCursor(Cursor.DEFAULT);
        }

        private void setUpEventOnIdField(){
            idField.setOnKeyPressed(keyEvent -> {
                formEventBus.publish(new ProductFormShortcutEvent(keyEvent.getCode()));
            });

            addEventOnIdField(idField);
        }

        private void lockField(TextField field) {
            field.setEditable(false);
            setLockFieldStyle(field);
        }

        private void setUpAutoCompleteFields() {
            var dataLoader = new DataLoader();
            var categoryData = dataLoader.loadCategoriesDescription();
            var suppliersData = dataLoader.loadSuppliersDescription();
            categoryList.getItems().addAll(categoryData);
            supplierList.getItems().addAll(suppliersData);
            new AutoCompleteField(categoryField,categoryList);
            new AutoCompleteField(supplierField,supplierList);
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
                        new ProductFormServices().save();
                        event.consume();
                    }
                });
            });
        }

        private void setUpperCaseTextFormatter(){
            List<TextField> fields = new ArrayList<>(Arrays.asList(idField,barCodeField,descriptionField,costPriceField,sellingPriceField,
                    markupField,quantityField,categoryField,supplierField));
            fields.forEach(field -> {
                TextFieldUtils.setUpperCaseTextFormatter(field);
            });
        }

        private void setUpNumericField() {
            var numericFields = Arrays.asList(quantityField,idField,barCodeField);
            numericFields.forEach(nc -> {
                nc.textProperty().addListener(((observableValue, s, t1) -> {
                    String plainText = t1.replaceAll("[^0-9]", "");
                    nc.setText(plainText);
                }));
            });
        }

        private void addEventOnIdField(TextField field) {
            field.setOnMouseClicked(click -> {
                if(!field.isEditable()){
                    Node changeCodeWarningNode = loadChangeCodeWarning();
                    formEventBus.publish(new ProductCodeEditAttemptEvent(changeCodeWarningNode));
                }
            });
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
                    new ProductFormServices().save(); // Save the form
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.TAB) {
                    handleTabNavigation(Arrays.asList(priceField, nextField, previousField), 0, keyEvent.isShiftDown());
                    keyEvent.consume();
                }

                formEventBus.publish(new ProductFormShortcutEvent(keyEvent.getCode()));
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
            } catch (NumberFormatException | ArithmeticException e) {
                markupField.setText("0.00");
            }
        }

        private void focusNextField(List<TextField> fields, int currentIndex) {
            int nextIndex = (currentIndex + 1) % fields.size();
            TextField nextField = fields.get(nextIndex);
            nextField.requestFocus();
            TextFieldUtils.lastPositionCursor(nextField);
        }

        private void focusPreviousField(List<TextField> fields, int currentIndex) {
            int previousIndex = (currentIndex - 1 + fields.size()) % fields.size();
            TextField previousField = fields.get(previousIndex);
            previousField.requestFocus();
            TextFieldUtils.lastPositionCursor(previousField);
        }


    }

    private class ProductFormServices {

        protected void save() {

            try {
                if (product.isPresent()) {
                    Product pToUpdate = updateProduct();
                    pService.update(pToUpdate);
                    showSuccess("Produto atualizado com sucesso.");
                } else {
                    Product newProduct = createProduct();
                    pService.save(newProduct);
                    showSuccess("Produto salvo com sucesso.");
                }

                formEventBus.publish(new ProductFormSaveEvent());

            } catch (DuplicateProductException e) {
                var oldProductCode = product.get().getProductCode();
                showError(e.getMessage());
                product.get().setProductCode(oldProductCode);
                idField.setText(String.valueOf(oldProductCode));

            } catch (ProductFormException e) {
                showError(e.getMessage());
            } catch (RuntimeException e) {
                showError("Erro inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }

        protected void cancel() {
            product = Optional.empty();
            formEventBus.publish(new ProductFormCancelEvent());
        }

        private Product updateProduct() throws ProductFormException {
            if (product.isEmpty()) {
                throw new ProductFormException(
                        "[%s][ERROR] The current product is empty".formatted(ProductFormController.class.getSimpleName())
                );
            }

            Product original = product.get();
            validateProductFields();

            Product.ProductBuilder builder = new Product.ProductBuilder()
                    .id(original.getId())
                    .productCode(original.getProductCode())
                    .barCode(original.getBarCode())
                    .description(original.getDescription())
                    .costPrice(original.getCostPrice())
                    .sellingPrice(original.getSellingPrice())
                    .quantity(original.getQuantity())
                    .supplier(original.getSupplier())
                    .category(original.getCategory())
                    .status(original.getStatus())
                    .dateCreate(original.getDateCreate())
                    .dateUpdate(original.getDateUpdate())
                    .dateDelete(original.getDateDelete());

            String barCodeText = barCodeField.getText().trim();
            builder.barCode(barCodeText.isEmpty() ? Optional.empty() : Optional.of(barCodeText));

            builder.description(descriptionField.getText().trim());

            BigDecimal costPrice = TextFieldUtils.formatCurrency(costPriceField.getText());
            BigDecimal sellingPrice = TextFieldUtils.formatCurrency(sellingPriceField.getText());
            builder.costPrice(costPrice).sellingPrice(sellingPrice);

            int quantity = Integer.parseInt(quantityField.getText());
            builder.quantity(quantity);

            long productCode = Long.parseLong(idField.getText());
            builder.productCode(productCode);

            // Cria o novo objeto sem alterar o original
            return builder.build();
        }

        private Product createProduct() throws ProductFormException {
            validateProductFields();
            var id = idField.getText().trim();
            var pCode = id.isEmpty() ? 0 : Integer.parseInt(id);
            String description = descriptionField.getText().trim();
            String barCodeText = barCodeField.getText().trim();
            Optional<String> barcode = barCodeText.isEmpty() ? Optional.empty() : Optional.of(barCodeText);
            BigDecimal costPrice = TextFieldUtils.formatCurrency(costPriceField.getText());
            BigDecimal sellingPrice = TextFieldUtils.formatCurrency(sellingPriceField.getText());
            var quantityText =  quantityField.getText().trim();
            int quantity;

            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                throw new ProductFormException("A quantidade deve ser um número válido.");
            }

            return Product.builder()
                    .productCode(pCode)
                    .barCode(barcode)
                    .description(description)
                    .costPrice(costPrice)
                    .sellingPrice(sellingPrice)
                    .dateCreate(Timestamp.from(Instant.now()))
                    .status(Status.ACTIVE)
                    .supplier(Optional.empty())
                    .category(Optional.empty())
                    .quantity(quantity)
                    .build();
        }

        private void validateProductFields() throws ProductFormException {
            String description = descriptionField.getText().trim();
            BigDecimal costPrice = TextFieldUtils.formatCurrency(costPriceField.getText());
            BigDecimal sellingPrice = TextFieldUtils.formatCurrency(sellingPriceField.getText());

            if(product.isPresent()){
                validateProductCode(product.get());
            }

            if(description.isEmpty()){
                descriptionField.clear();
                descriptionField.requestFocus();
                throw new ProductFormException("O campo de descrição do produto está vazio.");
            }

            if (!ProductValidator.costPriceLowerThanSellingPrice(costPrice,sellingPrice)) {
                throw new ProductFormException("O preço de custo deve ser menor do que o preço de venda.");
            }

            if(!ProductValidator.pricesGreaterThanZero(costPrice,sellingPrice)){
                throw new ProductFormException("Preço de custo e preço de venda devem ser maiores que zero.");
            }

        }
        private void validateProductCode(Product p) throws ProductFormException {
            String pCode = idField.getText().trim();
            if (pCode.isEmpty()) {
                idField.setText(String.valueOf(p.getProductCode()));
                throw new ProductFormException("O campo ID do produto está vazio.");
            }
        }

    }


}
