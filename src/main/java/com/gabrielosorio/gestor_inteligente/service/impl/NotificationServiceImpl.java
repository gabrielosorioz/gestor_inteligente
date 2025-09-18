package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.service.base.NotificationService;
import com.gabrielosorio.gestor_inteligente.view.shared.ToastNotification;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class NotificationServiceImpl implements NotificationService {

    private Stage parentStage;
    private ToastNotification.AnimationType animation;

    public NotificationServiceImpl(Stage parentStage, ToastNotification.AnimationType animation) {
        this.parentStage = parentStage;
        this.animation = animation;
    }

    public NotificationServiceImpl() {
    }

    @Override
    public void showError(String message) {
        var notification = new ToastNotification();
        notification.setTitle("Erro!");
        notification.setColor("#F44336");
        notification.setIcon(new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cancelar-96.png"));
        notification.setText(message);
        if(parentStage != null && animation != null){
            notification.showAndWait(parentStage,animation);
        } else {
            notification.showAndWait();
        }
    }

    @Override
    public void showSuccess(String message) {
        var notification = new ToastNotification();
        notification.setTitle("Sucesso!");
        notification.setText(message);
        if(parentStage != null && animation != null){
            notification.showAndWait(parentStage,animation);
        } else {
            notification.showAndWait();
        }
    }

}
