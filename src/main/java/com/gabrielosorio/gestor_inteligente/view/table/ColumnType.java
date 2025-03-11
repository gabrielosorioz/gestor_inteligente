package com.gabrielosorio.gestor_inteligente.view.table;

/**
 * Enum representing different column types for a TableView.
 *
 * <p>Used in conjunction with the {@link TableColumnConfig} annotation to define
 * how table columns should be displayed and formatted.</p>
 */
public enum ColumnType {
    /**
     * Default column type with standard text representation.
     */
    DEFAULT,

    /**
     * Monetary column type for displaying currency values, formatted accordingly.
     */
    MONETARY
}
