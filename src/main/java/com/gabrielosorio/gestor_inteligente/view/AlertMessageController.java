package com.gabrielosorio.gestor_inteligente.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AlertMessageController implements Initializable {

    @FXML
    private AnchorPane alertCodeBox;

    @FXML
    private AnchorPane alertContent;

    @FXML
    private Button btnNo;

    @FXML
    private Button btnYes;

    private Consumer<Void> onYesAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        btnYes.setOnMouseClicked(mouseEvent -> {
            AnchorPane parent = (AnchorPane) alertContent.getParent();
            parent.getChildren().remove(alertContent);

            if(onYesAction != null){
                onYesAction.accept(null);
            }

        });

        btnNo.setOnMouseClicked(mouseEvent -> {
            AnchorPane parent = (AnchorPane) alertContent.getParent();
            parent.getChildren().remove(alertContent);
        });
    }


    public void setOnYesAction(Consumer<Void> action){
        this.onYesAction = action;
    }

}
