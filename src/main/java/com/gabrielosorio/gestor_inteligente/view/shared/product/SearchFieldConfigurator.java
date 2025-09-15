package com.gabrielosorio.gestor_inteligente.view.shared.product;

import com.gabrielosorio.gestor_inteligente.view.shared.TextFieldUtils;
import javafx.scene.control.TextField;
import java.util.function.Consumer;

public class SearchFieldConfigurator {

    public static void configureSearchField(TextField searchField, Consumer<String> searchAction) {
        TextFieldUtils.setUpperCaseTextFormatter(searchField);
        searchField.textProperty().addListener((obsValue, oldValue, newValue) ->
                searchAction.accept(newValue)
        );
    }

    public static void configureSearchFieldWithShortcuts(TextField searchField,
                                                         Consumer<String> searchAction,
                                                         Consumer<javafx.scene.input.KeyCode> shortcutHandler) {
        configureSearchField(searchField, searchAction);
        searchField.setOnKeyPressed(keyEvent ->
                shortcutHandler.accept(keyEvent.getCode())
        );
    }
}