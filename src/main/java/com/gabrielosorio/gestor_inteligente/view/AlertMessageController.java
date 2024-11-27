package com.gabrielosorio.gestor_inteligente.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
        setUpFocusOnBtn();
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
    }

    private void setUpFocusOnBtn(){
        List<Node> elements = Arrays.asList(shadow,alertContent,shadow);

        elements.forEach(node -> {
            node.focusedProperty().addListener((observableValue, oldValue, isFocused) -> {
                if (isFocused) {
                    btnYes.requestFocus();
                }
            });
            node.setOnMouseClicked(mouseEvent -> {
                btnYes.requestFocus();
            });

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

    public Button getYesButton(){
        return btnYes;
    }

    public void close(){
        AnchorPane parent = (AnchorPane) alertContent.getParent();
        parent.getChildren().remove(alertContent);
    }

}
