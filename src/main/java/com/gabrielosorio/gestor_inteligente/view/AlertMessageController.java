package com.gabrielosorio.gestor_inteligente.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AlertMessageController implements Initializable {

    @FXML
    private AnchorPane alertCodeBox,alertContent
            ,shadow;

    @FXML
    private Button btnNo,btnYes;

    @FXML
    private ImageView icon;

    @FXML
    private Text text;

    @FXML
    private Label title;

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
            close();
        });

        shadow.setOnMouseClicked(mouseEvent -> {
            btnYes.requestFocus();
        });

        btnYes.getParent().setOnMouseClicked(mouseEvent -> {
            btnYes.requestFocus();
        });
    }


    public void setOnYesAction(Consumer<Void> action){
        this.onYesAction = action;
    }

    public void setText(String string){
        text.setText(string);
    }

    public AnchorPane getAlertCodeBox(){
        return alertCodeBox;
    }

    public void close(){
        AnchorPane parent = (AnchorPane) alertContent.getParent();
        parent.getChildren().remove(alertContent);
    }

}
