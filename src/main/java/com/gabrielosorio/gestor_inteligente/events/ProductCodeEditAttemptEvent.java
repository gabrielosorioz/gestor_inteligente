package com.gabrielosorio.gestor_inteligente.events;

import javafx.scene.Node;

public class ProductCodeEditAttemptEvent {

    private final Node warningDialog;

    public ProductCodeEditAttemptEvent(Node warningContent) {
        this.warningDialog = warningContent;
    }

    public Node getWarningDialog() {
        return warningDialog;
    }
}
