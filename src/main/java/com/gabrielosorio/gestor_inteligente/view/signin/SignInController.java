package com.gabrielosorio.gestor_inteligente.view.signin;

import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.service.base.AuthenticationService;
import com.gabrielosorio.gestor_inteligente.service.base.ScreenLoaderService;
import com.gabrielosorio.gestor_inteligente.service.impl.ServiceFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SignInController implements Initializable {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button loginButton;

    private final AuthenticationService authenticationService;
    private final ScreenLoaderService screenLoaderService;

    public SignInController(ServiceFactory serviceFactory) {
        this.authenticationService = serviceFactory.getAuthenticationService();
        this.screenLoaderService = serviceFactory.getScreenLoaderService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupEventHandlers();
        setupFocusManagement();
    }

    private void setupEventHandlers() {
        loginButton.setOnAction(event -> performLogin());
        username.setOnKeyPressed(this::handleKeyPressed);
        password.setOnKeyPressed(this::handleKeyPressed);
        Platform.runLater(() -> username.requestFocus());
    }

    private void setupFocusManagement() {
        username.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                password.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                performLogin();
                event.consume();
            }
        });

        password.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performLogin();
                event.consume();
            }
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            performLogin();
            event.consume();
        }
    }

    private void performLogin() {
        String email = username.getText().trim();
        String pass = password.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            showAlert("Erro de Validação", "Por favor, preencha todos os campos.", Alert.AlertType.WARNING);
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Email Inválido", "Por favor, insira um email válido.", Alert.AlertType.WARNING);
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Entrando...");

        new Thread(() -> {
            try {
                Optional<User> userOptional = authenticationService.authenticate(email, pass);

                Platform.runLater(() -> {
                    if (userOptional.isPresent()) {
                        try {
                            screenLoaderService.loadMainApplication();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            showAlert("Erro do Sistema",
                                    "Não foi possível carregar a tela principal. Reinicie a aplicação.",
                                    Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Falha na Autenticação",
                                "Email ou senha incorretos. Tente novamente.",
                                Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Erro do Sistema",
                            "Ocorreu um erro inesperado. Tente novamente em alguns momentos.",
                            Alert.AlertType.ERROR);
                });
            } finally {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Entrar");
                    password.clear();
                });
            }
        }).start();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}