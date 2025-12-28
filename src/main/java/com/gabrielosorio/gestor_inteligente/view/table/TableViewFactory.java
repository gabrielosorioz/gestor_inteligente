package com.gabrielosorio.gestor_inteligente.view.table;

import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Factory class for creating a JavaFX TableView based on fields and methods annotated with {@link TableColumnConfig}.
 * It builds columns for a TableView and supports monetary formatting for columns.
 *
 * @param <T> The type of the data for the table rows.
 */
public class TableViewFactory<T> implements TableViewCreator<T> {

    private final Class<T> clazz;
    private final Map<String, List<TextField>> columnTextFields = new HashMap<>();
    private final Map<KeyCode, Runnable> globalKeyHandlers = new HashMap<>();
    private AfterModelUpdateCallback<T, Object> afterModelUpdateCallback;
    private BeforeModelUpdateCallback<T, Object> beforeModelUpdateCallback;


    /**
     * Constructs a TableViewFactory for a specific class type.
     *
     * @param clazz the class representing the data type for the TableView.
     * @throws IllegalArgumentException if the class is not annotated with {@link TableViewComponent}.
     */
    public TableViewFactory(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(TableViewComponent.class)) {
            throw new IllegalArgumentException("The class " + clazz + " is not marked as a TableView component.");
        }
        this.clazz = clazz;
    }

    /**
     * Creates a TableView with custom CSS styling.
     *
     * @param resourceCssPath path to the CSS file for styling.
     * @return a fully constructed TableView.
     */
    @Override
    public TableView<T> createTableView(String resourceCssPath) {
        TableView<T> tableView = new TableView<>();
        tableView.getStylesheets().add(Objects.requireNonNull(getClass().getResource(resourceCssPath)).toExternalForm());
        return createTableViewInternal(tableView);
    }

    /**
     * Creates a TableView without custom CSS styling.
     *
     * @return a fully constructed TableView.
     */
    @Override
    public TableView<T> createTableView() {
        return createTableViewInternal(new TableView<>());
    }

    /**
     * Internal method to create the TableView by processing annotated fields and methods.
     *
     * @param tableView the TableView to be populated.
     * @return a TableView with columns based on annotations.
     */
    private TableView<T> createTableViewInternal(TableView<T> tableView) {
        List<ColumnMember> annotatedMembers = new ArrayList<>();

        // Process fields annotated with @TableColumnConfig
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableColumnConfig.class)) {
                field.setAccessible(true);
                annotatedMembers.add(new ColumnMember(field.getAnnotation(TableColumnConfig.class), field));
            }
        }

        // Process methods annotated with @TableColumnConfig
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(TableColumnConfig.class)) {
                method.setAccessible(true);
                annotatedMembers.add(new ColumnMember(method.getAnnotation(TableColumnConfig.class), method));
            }
        }

        // Sort annotated members by the 'order' property
        annotatedMembers.sort(Comparator.comparingInt(cm -> cm.config.order()));

        // Create columns for each annotated member
        for (ColumnMember member : annotatedMembers) {
            TableColumnConfig config = member.config;
            String columnId = config.id().isEmpty() ? member.getName() : config.id();

            if (config.columnType() == ColumnType.MONETARY) {
                TableColumn<T, BigDecimal> column = new TableColumn<>(config.header());
                column.setCellValueFactory(cellData -> getCellValue(cellData.getValue(), member));
                applyMonetaryCellFactory(column, config.currencySymbol());
                column.setId(columnId);
                tableView.getColumns().add(column);
            } else {
                TableColumn<T, Object> column = new TableColumn<>(config.header());
                column.setCellValueFactory(cellData -> getCellValue(cellData.getValue(), member));
                column.setId(columnId);

                if (member.member.isAnnotationPresent(EditableColumn.class)) {
                    EditableColumn editConfig = member.member.getAnnotation(EditableColumn.class);
                    if (editConfig.editType() == EditType.INTEGER) {
                        applyEditableNumericCellFactory(column,member);
                    } else if (editConfig.editType() == EditType.MONETARY) {
                        applyEditableMonetaryCellFactory(column, member, editConfig.currencySymbol());
                    }
                }

                tableView.getColumns().add(column);
            }
        }
        return tableView;
    }

    /**
     * Registra um handler global para uma tecla específica que será aplicado
     * a todos os TextFields das colunas editáveis.
     *
     * @param keyCode a tecla a ser monitorada
     * @param action a ação a ser executada quando a tecla for pressionada
     */
    public void registerGlobalKeyHandler(KeyCode keyCode, Runnable action) {
        globalKeyHandlers.put(keyCode, action);
    }

    /**
     * Remove um handler global para uma tecla específica.
     *
     * @param keyCode a tecla cujo handler será removido
     */
    public void removeGlobalKeyHandler(KeyCode keyCode) {
        globalKeyHandlers.remove(keyCode);
    }

    /**
     * Limpa todos os handlers globais registrados.
     */
    public void clearGlobalKeyHandlers() {
        globalKeyHandlers.clear();
    }

    /**
     * Aplica os handlers de tecla globais a um TextField.
     *
     * @param textField o TextField que receberá os handlers
     */
    /**
     * Aplica os handlers de tecla globais a um TextField.
     * Usa addEventHandler para permitir múltiplos handlers.
     *
     * @param textField o TextField que receberá os handlers
     */
    private void applyGlobalKeyHandlers(TextField textField) {
        textField.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            KeyCode pressedKey = keyEvent.getCode();
            Runnable handler = globalKeyHandlers.get(pressedKey);

            if (handler != null) {
                handler.run();
                keyEvent.consume(); // Previne propagação do evento
            }
        });
    }


    /**
     * Returns the value for a cell by invoking the field or method annotated with @TableColumnConfig.
     *
     * @param item the data item for the row.
     * @param member the annotated member (field or method) representing the column.
     * @return the observable value for the cell.
     */
    @SuppressWarnings("unchecked")
    private <V> ObservableValue<V> getCellValue(T item, ColumnMember member) {
        try {
            Object value;
            if (member.isField()) {
                Field field = (Field) member.member;
                value = field.get(item);
            } else {
                Method method = (Method) member.member;
                value = method.invoke(item);
            }
            return (value instanceof ObservableValue)
                    ? (ObservableValue<V>) value
                    : new SimpleObjectProperty<>((V) value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Applies a custom cell factory for editable numeric columns.
     *
     * @param column the column to make editable.
     */
    private <S> void applyEditableNumericCellFactory(TableColumn<S, Object> column, ColumnMember member) {
        EditableColumn editConfig =
                (member.member instanceof Field)
                        ? ((Field) member.member).getAnnotation(EditableColumn.class)
                        : ((Method) member.member).getAnnotation(EditableColumn.class);

        String columnId = column.getId();
        columnTextFields.putIfAbsent(columnId, new ArrayList<>());

        column.setCellFactory(col -> new TableCell<S, Object>() {
            private final TextField textField = new TextField();

            {

                // Registrar o TextField
                columnTextFields.get(columnId).add(textField);

                applyGlobalKeyHandlers(textField);

                var stylesheets = col.getTableView().getStylesheets();
                textField.getStylesheets().addAll(stylesheets);
                textField.getStyleClass().add("editable-col-textfield");


                if (editConfig.fieldWidth() > 0){
                    textField.setPrefWidth(Math.max(95.0, editConfig.fieldWidth()));
                } else {
                    textField.setPrefWidth(95);
                };

                textField.setTextFormatter(new TextFormatter<>(change -> {
                    if (change.getControlNewText().matches("\\d*")) {
                        return change;
                    }
                    return null;
                }));

                textField.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (getTableRow() == null || getTableRow().getItem() == null) return;

                    EditableColumn editConfig =
                            (member.member instanceof Field)
                                    ? ((Field) member.member).getAnnotation(EditableColumn.class)
                                    : ((Method) member.member).getAnnotation(EditableColumn.class);

                    if (editConfig == null || editConfig.propertyUpdater().isEmpty()) return;

                    try {
                        String valueToUpdate =
                                (newValue == null || newValue.isEmpty() || newValue.matches("^0+$"))
                                        ? "1"
                                        : newValue;

                        // Atualiza o modelo para evitar subtotal inconsistente,
                        // mas o TextField continua vazio enquanto o usuário edita
                        updateModelValue(getTableRow().getItem(),
                                editConfig.propertyUpdater(),
                                valueToUpdate,
                                editConfig.editType(),
                                columnId);

                    } catch (NumberFormatException ignored) {
                    }
                });

                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        // Validação ao perder o foco
                        String text = textField.getText().trim();
                        if (text.isEmpty() || text.matches("^0+$")) {
                            textField.setText("1");
                            text = "1";
                        }

                        // Atualizar o modelo
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            S item = getTableRow().getItem();
                            EditableColumn editConfig =
                                    (member.member instanceof Field)
                                            ? ((Field) member.member).getAnnotation(EditableColumn.class)
                                            : ((Method) member.member).getAnnotation(EditableColumn.class);

                            if (editConfig != null && !editConfig.propertyUpdater().isEmpty()) {
                                updateModelValue(item, editConfig.propertyUpdater(), text, editConfig.editType(),columnId);
                            }
                        }
                    }
                });

            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Evita sobrescrever o que o usuário está digitando (e resetar o caret)
                if (!textField.isFocused()) {
                    textField.setText(item.toString());
                }

                setGraphic(textField);
            }

        });
    }

    /**
     * Applies a custom cell factory for editable monetary columns.
     *
     * @param column the column to make editable.
     * @param member the column member containing field/method information.
     * @param currencySymbol the currency symbol to display.
     */
    /**
     * Applies a custom cell factory for editable monetary columns.
     *
     * @param column the column to make editable.
     * @param member the column member containing field/method information.
     * @param currencySymbol the currency symbol to display.
     */
    private <S> void applyEditableMonetaryCellFactory(TableColumn<S, Object> column, ColumnMember member, String currencySymbol) {
        String columnId = column.getId();
        columnTextFields.putIfAbsent(columnId, new ArrayList<>());

        column.setCellFactory(col -> new TableCell<S, Object>() {
            EditableColumn editConfig =
                    (member.member instanceof Field)
                            ? ((Field) member.member).getAnnotation(EditableColumn.class)
                            : ((Method) member.member).getAnnotation(EditableColumn.class);

            private final TextField monetaryField = new TextField();
            private final Label currencyLabel = new Label(currencySymbol);
            private final HBox hbox = new HBox(2, currencyLabel, monetaryField);
            private boolean isUpdatingFromUser = false;


             {

                // Registrar o TextField
                columnTextFields.get(columnId).add(monetaryField);

                applyGlobalKeyHandlers(monetaryField);

                 // Adicionar folha de estilo
                var stylesheets = col.getTableView().getStylesheets();
                monetaryField.getStylesheets().addAll(stylesheets);
                monetaryField.getStyleClass().add("editable-col-textfield");

                currencyLabel.setStyle("-fx-text-fill: black;");
                currencyLabel.setPrefWidth(30);

                if (editConfig.fieldWidth() > 0){
                    monetaryField.setPrefWidth(Math.max(95.0, editConfig.fieldWidth()));
                } else {
                    monetaryField.setPrefWidth(95);
                }

                hbox.setAlignment(Pos.CENTER_LEFT);

                // Posicionar cursor no final ao pressionar setas
                 monetaryField.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                     KeyCode keyCodePressed = keyEvent.getCode();
                     if (keyCodePressed.isArrowKey()) {
                         monetaryField.positionCaret(monetaryField.getText().length());
                     }
                 });


                 // Posicionar cursor no final ao clicar
                monetaryField.setOnMouseClicked(mouseEvent -> {
                    monetaryField.positionCaret(monetaryField.getText().length());
                });

                // Listener para formatação automática enquanto digita
                monetaryField.textProperty().addListener((observableValue, oldValue, newValue) -> {
                    // Ignora mudanças que vêm do updateItem()
                    if (!isUpdatingFromUser) {
                        return;
                    }

                    String formattedText = TextFieldUtils.formatText(newValue);

                    if (!newValue.equals(formattedText)) {
                        Platform.runLater(() -> {
                            monetaryField.setText(formattedText);
                            monetaryField.positionCaret(formattedText.length());
                            // Atualizar o valor no modelo
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                S item = getTableRow().getItem();
                                EditableColumn editConfig =
                                        (member.member instanceof Field)
                                                ? ((Field) member.member).getAnnotation(EditableColumn.class)
                                                : ((Method) member.member).getAnnotation(EditableColumn.class);

                                if (editConfig != null && !editConfig.propertyUpdater().isEmpty()) {
                                    try {
                                        BigDecimal value = TextFieldUtils.formatCurrency(monetaryField.getText());
                                        updateModelValue(item, editConfig.propertyUpdater(), value.toPlainString(), editConfig.editType(),columnId);
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });

                // Marca que o usuário está editando quando ganha foco
                monetaryField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        isUpdatingFromUser = true;
                    } else {
                        isUpdatingFromUser = false;

                        // Validação ao perder o foco
                        String text = monetaryField.getText().trim();
                        if (text.isEmpty() || text.isBlank()) {
                            monetaryField.setText("0,00");
                        }
                    }
                });


            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (isUpdatingFromUser && monetaryField.isFocused()) {
                        setGraphic(hbox);
                        return;
                    }

                    String displayValue;
                    if (item instanceof BigDecimal) {
                        displayValue = ((BigDecimal) item).toPlainString();
                    } else {
                        displayValue = item.toString();
                    }

                    if (displayValue.isEmpty() || displayValue.isBlank()) {
                        monetaryField.setText("0,00");
                    } else {
                        String formattedValue = TextFieldUtils.formatText(displayValue);
                        monetaryField.setText(formattedValue);
                    }
                    setGraphic(hbox);
                }
            }
        });
    }  /**
     * Updates the model value using reflection to invoke the setter method.
     *
     * @param item the data item to update.
     * @param setterName the name of the setter method.
     * @param value the new value as a string.
     * @param editType the type of edit to determine how to parse the value.
     */
    private <S> void updateModelValue(S item, String setterName, String value, EditType editType, String columnId) {
        try {
            Method setter = null;
            Object parsedValue = null;
            Object oldValue = null;

            // Obter o valor antigo se houver callback de pré-atualização
            if (beforeModelUpdateCallback != null) {
                oldValue = getCurrentValue(item, setterName);
            }


            switch (editType) {
                case INTEGER:
                    parsedValue = Integer.parseInt(value);
                    try {
                        setter = item.getClass().getMethod(setterName, int.class);
                    } catch (NoSuchMethodException e) {
                        try {
                            setter = item.getClass().getMethod(setterName, long.class);
                            parsedValue = Long.parseLong(value);
                        } catch (NoSuchMethodException ex) {
                            setter = item.getClass().getMethod(setterName, Integer.class);
                        }
                    }
                    break;

                case MONETARY:
                    parsedValue = new BigDecimal(value);
                    setter = item.getClass().getMethod(setterName, BigDecimal.class);
                    break;
            }

            if (setter != null) {
                if (beforeModelUpdateCallback != null) {
                    @SuppressWarnings("unchecked")
                    boolean shouldProceed = beforeModelUpdateCallback.beforeUpdate(
                            (T) item,
                            columnId,
                            oldValue,
                            parsedValue
                    );

                    if (!shouldProceed) {
                        return; // Cancelar atualização
                    }
                }
                //atualiza o modelo
                setter.invoke(item, parsedValue);
                // Callback DEPOIS da atualização
                if (afterModelUpdateCallback != null) {
                    @SuppressWarnings("unchecked")
                    AfterModelUpdateCallback<T, Object> callback =
                            (AfterModelUpdateCallback<T, Object>) afterModelUpdateCallback;
                    callback.onUpdate((T) item, columnId, parsedValue);
                }

            }

        } catch (NoSuchMethodException e) {
            System.err.println("Setter method '" + setterName + "' not found in class " + item.getClass().getName());
            e.printStackTrace();
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("Error invoking setter '" + setterName + "'");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing value '" + value + "' for edit type " + editType);
            e.printStackTrace();
        }
    }

    /**
     * Obtém o valor atual de um campo através do getter correspondente.
     */
    private <S> Object getCurrentValue(S item, String setterName) {
        try {
            // Converter setXxx para getXxx
            String getterName = setterName.replaceFirst("set", "get");
            Method getter = item.getClass().getMethod(getterName);
            return getter.invoke(item);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Applies a custom cell factory to format monetary values in a column.
     *
     * @param currencyColumn the column for monetary values.
     * @param currencySymbol the symbol for the currency (e.g., "$").
     */
    private <S> void applyMonetaryCellFactory(TableColumn<S, BigDecimal> currencyColumn, String currencySymbol) {
        currencyColumn.setCellFactory(column -> new TableCell<S, BigDecimal>() {
            private final Label currencyLabel = new Label(currencySymbol);
            private final Text valueText = new Text();
            {
                currencyLabel.setStyle("-fx-text-fill: black;");
                currencyLabel.setPrefWidth(30);
                valueText.setTextAlignment(TextAlignment.CENTER);
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
                    valueText.setText(TextFieldUtils.formatText(
                            item.setScale(2, RoundingMode.HALF_DOWN).toPlainString()
                    ));
                    setGraphic(getGraphic());
                }
            }
        });
    }

    /**
     * Define o callback a ser executado após atualização do modelo.
     *
     * @param callback o callback
     */
    public void setAfterModelUpdateCallback(AfterModelUpdateCallback<T, Object> callback) {
        this.afterModelUpdateCallback = callback;
    }

    /**
     * Define o callback a ser executado antes da atualização do modelo.
     *
     * @param callback o callback
     */
    public void setBeforeModelUpdateCallback(BeforeModelUpdateCallback<T, Object> callback) {
        this.beforeModelUpdateCallback = callback;
    }

    /**
     * Retorna os TextFields de uma coluna editável específica.
     *
     * @param columnId o ID da coluna conforme definido em @TableColumnConfig
     * @return lista de TextFields da coluna, ou lista vazia se não encontrada
     */
    public List<TextField> getColumnTextFields(String columnId) {
        return columnTextFields.getOrDefault(columnId, Collections.emptyList());
    }

    /**
     * Cria um builder para configuração fluente da TableViewFactory.
     *
     * @param clazz a classe representando o tipo de dados da TableView
     * @param <T> o tipo de dados da TableView
     * @return uma nova instância do builder
     */
    public static <T> Builder<T> builder(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    /**
     * Retorna todos os TextFields de todas as colunas editáveis.
     *
     * @return Map com columnId como chave e lista de TextFields como valor
     */
    public Map<String, List<TextField>> getAllColumnTextFields() {
        return new HashMap<>(columnTextFields);
    }

    /**
     * Helper class to unify annotated members (fields and methods).
     */

    /**
     * Builder para configuração fluente da TableViewFactory.
     *
     * @param <T> o tipo de dados da TableView
     */
    public static class Builder<T> {
        private final Class<T> clazz;
        private String stylesheet;
        private final Map<KeyCode, Runnable> keyHandlers = new HashMap<>();
        private AfterModelUpdateCallback<T, Object> afterModelUpdateCallback;
        private BeforeModelUpdateCallback<T, Object> beforeModelUpdateCallback;

        private Builder(Class<T> clazz) {
            this.clazz = clazz;
        }

        /**
         * Define o arquivo CSS para estilização da TableView.
         *
         * @param resourceCssPath caminho do recurso CSS
         * @return esta instância do builder
         */
        public Builder<T> withStylesheet(String resourceCssPath) {
            this.stylesheet = resourceCssPath;
            return this;
        }

        /**
         * Registra um handler para uma tecla específica que será aplicado
         * a todos os campos editáveis da tabela.
         *
         * @param keyCode a tecla a ser monitorada
         * @param action a ação a ser executada quando a tecla for pressionada
         * @return esta instância do builder
         */
        public Builder<T> keyHandler(KeyCode keyCode, Runnable action) {
            this.keyHandlers.put(keyCode, action);
            return this;
        }

        /**
         * Registra um callback que será executado APÓS cada atualização de modelo
         * nos campos editáveis.
         *
         * @param callback o callback a ser executado
         * @return esta instância do builder
         */
        public Builder<T> afterModelUpdate(AfterModelUpdateCallback<T, Object> callback) {
            this.afterModelUpdateCallback = callback;
            return this;
        }

        /**
         * Registra um callback que será executado ANTES de cada atualização de modelo
         * nos campos editáveis. O callback pode cancelar a atualização retornando false.
         *
         * @param callback o callback a ser executado
         * @return esta instância do builder
         */
        public Builder<T> beforeModelUpdate(BeforeModelUpdateCallback<T, Object> callback) {
            this.beforeModelUpdateCallback = callback;
            return this;
        }

        /**
         * Constrói a TableView com todas as configurações definidas.
         *
         * @return a TableView configurada
         */
        public TableView<T> build() {
            TableViewFactory<T> factory = new TableViewFactory<>(clazz);

            // Registrar handlers ANTES de criar a TableView
            keyHandlers.forEach(factory::registerGlobalKeyHandler);

            // Registrar callbacks
            if (afterModelUpdateCallback != null) {
                factory.setAfterModelUpdateCallback(afterModelUpdateCallback);
            }

            if (beforeModelUpdateCallback != null) {
                factory.setBeforeModelUpdateCallback(beforeModelUpdateCallback);
            }

            // Criar a TableView com ou sem stylesheet
            if (stylesheet != null && !stylesheet.isEmpty()) {
                return factory.createTableView(stylesheet);
            } else {
                return factory.createTableView();
            }
        }
    }

    private class ColumnMember {
        final TableColumnConfig config;
        final AccessibleObject member; // Can be Field or Method

        ColumnMember(TableColumnConfig config, AccessibleObject member) {
            this.config = config;
            this.member = member;
        }

        boolean isField() {
            return member instanceof Field;
        }

        String getName() {
            if (isField()) {
                return ((Field) member).getName();
            } else {
                return ((Method) member).getName();
            }
        }
    }
}
