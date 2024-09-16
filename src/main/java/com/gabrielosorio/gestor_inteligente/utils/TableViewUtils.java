package com.gabrielosorio.gestor_inteligente.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class TableViewUtils {

    public static <T> void monetaryLabel(TableColumn<T, String> currencyColumn){
        currencyColumn.setCellFactory(column -> new TableCell<T,String>() {
            private final Label currencyLabel = new Label("R$");
            private final Text valueText = new Text();

            {
                currencyLabel.setStyle("-fx-text-fill: black;");
                currencyLabel.setPrefWidth(50);
                valueText.setTextAlignment(TextAlignment.RIGHT);
                HBox hbox = new HBox(5,currencyLabel, valueText);
                hbox.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hbox);
                setText(null);
            }

            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item,empty);
                if(empty || item == null){
                    setGraphic(null);
                } else {
                    valueText.setText(TextFieldUtils.formatText(item));
                    setGraphic(getGraphic());
                }
            }
        });
    }

    public static <T> void resetColumnProps(TableColumn<?,?>... columns){
        for(TableColumn<?,?> column: columns){
            column.setResizable(false);
            column.setReorderable(false);
            column.setSortable(false);
        }
    }

}
