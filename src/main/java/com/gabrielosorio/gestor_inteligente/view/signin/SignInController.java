package com.gabrielosorio.gestor_inteligente.view.signin;

import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.service.base.AuthenticationService;
import com.gabrielosorio.gestor_inteligente.service.base.ScreenLoaderService;
import com.gabrielosorio.gestor_inteligente.service.base.UserService;
import com.gabrielosorio.gestor_inteligente.service.impl.ServiceFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SignInController implements Initializable {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button loginButton;
    @FXML private Label resetPasswordLbl;
    @FXML private Label signUpLbl;

    private final AuthenticationService authenticationService;
    private final ScreenLoaderService screenLoaderService;
    private final UserService userService;

    public SignInController(ServiceFactory serviceFactory) {
        this.authenticationService = serviceFactory.getAuthenticationService();
        this.screenLoaderService = serviceFactory.getScreenLoaderService();
        userService = serviceFactory.getUserService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupEventHandlers();
        setupFocusManagement();
        setupLabelClickHandlers();
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

    private void setupLabelClickHandlers() {
        signUpLbl.setOnMouseClicked(e -> {
            showSignUpForm();
        });
        signUpLbl.setStyle("-fx-cursor: hand;");

        resetPasswordLbl.setOnMouseClicked(this::onResetPasswordClicked);
        resetPasswordLbl.setStyle("-fx-cursor: hand;");

        // Adicionar efeito hover aos labels
        setupLabelHoverEffects();
    }

    private void setupLabelHoverEffects() {
        // Efeito hover para signUpLbl
        signUpLbl.setOnMouseEntered(event -> {
            signUpLbl.setStyle("-fx-cursor: hand; -fx-text-fill: #0066cc; -fx-underline: true;");
        });

        signUpLbl.setOnMouseExited(event -> {
            signUpLbl.setStyle("-fx-cursor: hand;");
        });

        // Efeito hover para resetPasswordLbl
        resetPasswordLbl.setOnMouseEntered(event -> {
            resetPasswordLbl.setStyle("-fx-cursor: hand; -fx-text-fill: #0066cc; -fx-underline: true;");
        });

        resetPasswordLbl.setOnMouseExited(event -> {
            resetPasswordLbl.setStyle("-fx-cursor: hand;");
        });
    }

    private void showSignUpForm() {
        try {
            screenLoaderService.loadSignUpScreen(userService);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro do Sistema",
                    "Não foi possível carregar a tela de cadastro. Tente novamente.",
                    Alert.AlertType.ERROR);
        }
    }

    private void onResetPasswordClicked(MouseEvent event) {
        showAlert("Funcionalidade em Desenvolvimento",
                "A funcionalidade de redefinição de senha ainda não está disponível.",
                Alert.AlertType.INFORMATION);
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
                            screenLoaderService.loadMainApplication(userOptional.get());
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