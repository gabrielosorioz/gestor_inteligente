package com.gabrielosorio.gestor_inteligente.view.shared;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class AutoCompleteField {

    private final TextField textField;
    private final ListView<String> listView;
    private final List<String> items;

    public AutoCompleteField(TextField textField, ListView<String> listView) {
        this.textField = textField;
        this.listView = listView;
        this.items = new ArrayList<>(listView.getItems());
        setUpperCaseTextFormatter(textField);
        addEvent(listView, textField);
        adjustListViewHeight(listView);
    }

    private List<String> searchList(String searchWords, List<String> items) {
        List<String> separatedSearchWords = Arrays.asList(searchWords.trim().split(" "));
        return items.stream()
                .filter(item -> separatedSearchWords.stream()
                        .allMatch(searchedWord -> item.toLowerCase().contains(searchedWord.toLowerCase())))
                .collect(Collectors.toList());
    }

    private void addEvent(ListView<String> listView, TextField textField) {

        textField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            List<String> filteredItems = searchList(newValue, items);
            listView.getItems().setAll(filteredItems); // Usa setAll para atualizar a lista
            adjustListViewHeight(listView);
        });

        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> listView) {
                ListCell<String> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };

                cell.setOnMouseClicked(event -> {
                    if (!cell.isEmpty()) {
                        String item = cell.getItem();
                        textField.setText(item);
                        System.out.println("Item clicado: " + item);
                    }
                });

                // Measure the height of the cell after it is rendered
                cell.heightProperty().addListener((observable, oldValue, newValue) -> {
                    adjustListViewHeight(listView);
                });

                return cell;
            }
        });
    }

    private void adjustListViewHeight(ListView<String> listView) {
        double totalHeight = 0;
        for (int i = 0; i < listView.getItems().size(); i++) {
            ListCell<String> cell = (ListCell<String>) listView.lookup(".list-cell");
            if (cell != null) {
                totalHeight += cell.getHeight();
            }
        }
        listView.setPrefHeight(totalHeight);
    }

    private void setUpperCaseTextFormatter(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            change.setText(change.getText().toUpperCase());
            return change;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);
    }
}
