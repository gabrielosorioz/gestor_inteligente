package com.gabrielosorio.gestor_inteligente.view.shared;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class InfoMessageController implements Initializable {

    @FXML
    private AnchorPane alertCodeBox,alertContent,shadow;

    @FXML
    private Button btnOk;

    @FXML
    private ImageView icon;

    @FXML
    private Text text;

    @FXML
    private Label title;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnOk.requestFocus();

        btnOk.setOnMouseClicked(mouseEvent -> {
            close();
        });

        shadow.setOnMouseClicked(mouseEvent -> {
            btnOk.requestFocus();
        });

        btnOk.getParent().setOnMouseClicked(mouseEvent -> {
            btnOk.requestFocus();
        });
    }

    public void setText(String message){
        text.setText(message);
    }

    public Button getBtnOk(){
        return btnOk;
    }

    public void close(){
        AnchorPane parent = (AnchorPane) alertContent.getParent();
        parent.getChildren().remove(alertContent);
    }
}
