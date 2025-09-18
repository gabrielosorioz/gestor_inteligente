package com.gabrielosorio.gestor_inteligente.view.shared;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.view.shared.base.ScreenManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScreenManagerImpl implements ScreenManager {

    private final Map<String, Parent> viewCache = new HashMap<>();
    private final Map<Parent, Object> controllerCache = new HashMap<>();
    private final AnchorPane contentContainer;
    private ScreenChangeListener screenChangeListener;

    public ScreenManagerImpl(AnchorPane contentContainer) {
        this.contentContainer = contentContainer;
    }

    @Override
    public void loadScreen(String fxmlPath) {
        loadScreen(fxmlPath, null);
    }

    @Override
    public void loadScreen(String fxmlPath, Object controller) {
        try {
            Parent screen = getOrLoadScreen(fxmlPath, controller);
            Object screenController = controllerCache.get(screen);

            handleControllerSetup(screenController);
            displayScreen(screen);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela: " + fxmlPath, e);
        }
    }

    private Parent getOrLoadScreen(String fxmlPath, Object controller) throws IOException {
        Parent screen = viewCache.get(fxmlPath);
        if(screen == null) {
            screen = createNewScreen(fxmlPath,controller);
            viewCache.put(fxmlPath,screen);
        }
        return screen;
    }

    private Parent createNewScreen(String fxmlPath, Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource(fxmlPath));

        if (controller != null) {
            loader.setController(controller);
        }

        Parent screen = loader.load();
        Object loadedController = (controller != null) ? controller : loader.getController();

        controllerCache.put(screen, loadedController);

        return screen;
    }

    private void handleControllerSetup(Object controller) {
        if (screenChangeListener != null) {
            screenChangeListener.onScreenChanged(controller);
        }
    }

    private void displayScreen(Parent screen) {
        if (Platform.isFxApplicationThread()) {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(screen);
        } else {
            Platform.runLater(() -> {
                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(screen);
            });
        }
    }

    @Override
    public void clearCache() {
        viewCache.clear();
        controllerCache.clear();
    }

    @Override
    public void removeCachedScreen(String fxmlPath) {
        viewCache.entrySet().removeIf(entry -> entry.getKey().startsWith(fxmlPath));
    }

    @Override
    public void setScreenChangeListener(ScreenManager.ScreenChangeListener listener) {
        this.screenChangeListener = listener;
    }


}
