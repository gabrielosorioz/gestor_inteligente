package com.gabrielosorio.gestor_inteligente.view.table;

import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Factory class for creating a JavaFX TableView based on fields and methods annotated with {@link TableColumnConfig}.
 * It builds columns for a TableView and supports monetary formatting for columns.
 *
 * @param <T> The type of the data for the table rows.
 */
public class TableViewFactory<T> implements TableViewCreator<T> {
    private final Class<T> clazz;

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
                tableView.getColumns().add(column);
            }
        }
        return tableView;
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
                currencyLabel.setPrefWidth(35);
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
     * Helper class to unify annotated members (fields and methods).
     */
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
