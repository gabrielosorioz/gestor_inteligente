package com.gabrielosorio.gestor_inteligente.view.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to configure columns in JavaFX TableView components.
 * It can now be applied to both fields and methods.
 *
 * Usage example:
 * <pre>
 * {@code
 * public class Product {
 *     @TableColumnConfig(header = "Name", order = 1)
 *     private String name;
 *
 *     @TableColumnConfig(header = "Price", order = 2, columnType = ColumnType.MONETARY, currencySymbol = "$")
 *     public BigDecimal getFormattedPrice() {
 *         // returns a calculated or processed value
 *         return price.setScale(2, RoundingMode.HALF_DOWN);
 *     }
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TableColumnConfig {

    /**
     * Optional unique identifier for the column.
     *
     * @return the column's ID (default is an empty string).
     */
    String id() default "";

    /**
     * The header label to be displayed for the column.
     *
     * @return the header text for the column.
     */
    String header();

    /**
     * The order in which the column will appear in the table.
     *
     * @return the order of the column (default is 0).
     */
    int order() default 0;

    /**
     * The type of the column, which defines how the data is presented.
     * Defaults to {@link ColumnType#DEFAULT}.
     *
     * @return the column type.
     */
    ColumnType columnType() default ColumnType.DEFAULT;

    /**
     * The symbol to be used for currency columns. Default is "R$".
     * This is relevant when the column type is monetary.
     *
     * @return the currency symbol.
     */
    String currencySymbol() default "R$";
}
