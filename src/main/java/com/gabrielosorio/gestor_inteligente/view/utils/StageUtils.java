package com.gabrielosorio.gestor_inteligente.view.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class StageUtils {

    public static Stage getStageFromNode(Node node) {
        if (node == null) return null;

        Scene scene = node.getScene();
        if (scene == null) return null;

        Window window = scene.getWindow();
        if (window instanceof Stage) {
            return (Stage) window;
        }
        return null;
    }
}
