package com.gabrielosorio.gestor_inteligente.view.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a class as a component suitable for generating a JavaFX TableView.
 *
 * <p>Classes annotated with {@code @TableViewComponent} can be processed dynamically to create
 * TableView structures based on their fields annotated with {@link TableColumnConfig}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * @TableViewComponent
 * public class Product {
 *     @TableColumnConfig(header = "Name", order = 1)
 *     private String name;
 *
 *     @TableColumnConfig(header = "Price", order = 2, columnType = ColumnType.MONETARY, currencySymbol = "$")
 *     private BigDecimal price;
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableViewComponent {

}
