package com.gabrielosorio.gestor_inteligente.events;

import javafx.scene.input.KeyCode;

public class ProductFormShortcutEvent {

    private final KeyCode keyCode;

    public ProductFormShortcutEvent(KeyCode keyCode) {
        this.keyCode = keyCode;
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }
}
