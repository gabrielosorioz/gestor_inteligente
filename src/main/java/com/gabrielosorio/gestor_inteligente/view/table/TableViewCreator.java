package com.gabrielosorio.gestor_inteligente.view.table;

import javafx.scene.control.TableView;

/**
 * Interface defining the contract for creating JavaFX TableView instances.
 *
 * <p>Implementations of this interface are responsible for dynamically generating
 * TableView components based on a specified data model.</p>
 *
 * @param <T> The type of objects displayed in the TableView.
 */
public interface TableViewCreator<T> {

    /**
     * Creates a TableView instance with the specified CSS styling.
     *
     * @param resourceCssPath The path to the CSS file for styling the TableView.
     * @return A configured TableView instance.
     */
    TableView<T> createTableView(String resourceCssPath);

    /**
     * Creates a TableView instance with default styling.
     *
     * @return A configured TableView instance.
     */
    TableView<T> createTableView();
}
