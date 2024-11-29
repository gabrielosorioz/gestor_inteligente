package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.service.NotificationService;
import com.gabrielosorio.gestor_inteligente.view.ToastNotification;
import javafx.scene.image.Image;

public class NotificationServiceImpl implements NotificationService {

    @Override
    public void showSuccess(String message) {
        var notification = new ToastNotification();
        notification.setTitle("Erro!");
        notification.setColor("#F44336");
        notification.setIcon(new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-cancelar-96.png"));
        notification.setText(message);
        notification.showAndWait();
    }

    @Override
    public void showError(String message) {
        var notification = new ToastNotification();
        notification.setTitle("Sucesso!");
        notification.setText("Produto salvo com sucesso.");
        notification.showAndWait();
    }

}
