package com.gabrielosorio.gestor_inteligente.view.signup;

import com.gabrielosorio.gestor_inteligente.model.dto.UserRegistrationRequest;
import com.gabrielosorio.gestor_inteligente.model.dto.UserRegistrationResult;
import com.gabrielosorio.gestor_inteligente.service.base.NotificationService;
import com.gabrielosorio.gestor_inteligente.service.base.ScreenLoaderService;
import com.gabrielosorio.gestor_inteligente.service.base.UserService;
import com.gabrielosorio.gestor_inteligente.service.impl.NotificationServiceImpl;
import com.gabrielosorio.gestor_inteligente.view.shared.ToastNotification;
import com.gabrielosorio.gestor_inteligente.view.utils.StageUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cpfField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;
    @FXML private Button registerButton;

    private final UserService userService;
    private final ScreenLoaderService screenLoaderService;

    public SignUpController(UserService userService, ScreenLoaderService screenLoaderService) {
        this.userService = userService;
        this.screenLoaderService = screenLoaderService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupEventHandlers();
        setupFocusManagement();
        setupFieldFormatting();
    }

    private void setupEventHandlers() {
        registerButton.setOnAction(event -> performRegistration());

        // Adicionar listeners de teclado para todos os campos
        firstNameField.setOnKeyPressed(this::handleKeyPressed);
        lastNameField.setOnKeyPressed(this::handleKeyPressed);
        emailField.setOnKeyPressed(this::handleKeyPressed);
        phoneField.setOnKeyPressed(this::handleKeyPressed);
        cpfField.setOnKeyPressed(this::handleKeyPressed);
        passwordField.setOnKeyPressed(this::handleKeyPressed);
        confirmPasswordField.setOnKeyPressed(this::handleKeyPressed);

        Platform.runLater(() -> firstNameField.requestFocus());
    }

    private void setupFocusManagement() {
        // Configurar navegação com Tab entre os campos
        firstNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                lastNameField.requestFocus();
                event.consume();
            }
        });

        lastNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                emailField.requestFocus();
                event.consume();
            }
        });

        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                phoneField.requestFocus();
                event.consume();
            }
        });

        phoneField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                cpfField.requestFocus();
                event.consume();
            }
        });

        cpfField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
                event.consume();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                confirmPasswordField.requestFocus();
                event.consume();
            }
        });

        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performRegistration();
                event.consume();
            }
        });
    }

    private void setupFieldFormatting() {
        // Formatação automática do CPF
        cpfField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String formatted = formatCPFInput(newValue);
                if (!formatted.equals(newValue)) {
                    cpfField.setText(formatted);
                    cpfField.positionCaret(formatted.length());
                }
            }
        });

        // Formatação automática do telefone
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String formatted = formatPhoneInput(newValue);
                if (!formatted.equals(newValue)) {
                    phoneField.setText(formatted);
                    phoneField.positionCaret(formatted.length());
                }
            }
        });
    }

    private String formatCPFInput(String input) {
        // Remove tudo que não é dígito
        String numbers = input.replaceAll("\\D", "");

        // Limita a 11 dígitos
        if (numbers.length() > 11) {
            numbers = numbers.substring(0, 11);
        }

        // Aplica a formatação
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < numbers.length(); i++) {
            if (i == 3 || i == 6) {
                formatted.append(".");
            } else if (i == 9) {
                formatted.append("-");
            }
            formatted.append(numbers.charAt(i));
        }

        return formatted.toString();
    }

    private String formatPhoneInput(String input) {
        // Remove tudo que não é dígito
        String numbers = input.replaceAll("\\D", "");

        // Limita a 11 dígitos
        if (numbers.length() > 11) {
            numbers = numbers.substring(0, 11);
        }

        // Aplica a formatação baseada no tamanho
        if (numbers.length() <= 2) {
            return numbers;
        } else if (numbers.length() <= 7) {
            return "(" + numbers.substring(0, 2) + ") " + numbers.substring(2);
        } else if (numbers.length() <= 10) {
            return "(" + numbers.substring(0, 2) + ") " + numbers.substring(2, 6) + "-" + numbers.substring(6);
        } else {
            return "(" + numbers.substring(0, 2) + ") " + numbers.substring(2, 7) + "-" + numbers.substring(7);
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            // Voltar para tela de login
            try {
                screenLoaderService.loadSignInScreen();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erro do Sistema", "Não foi possível voltar para a tela de login.", Alert.AlertType.ERROR);
            }
            event.consume();
        }
    }

    private void performRegistration() {
        // Criar o request com os dados dos campos
        UserRegistrationRequest request = new UserRegistrationRequest(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                cpfField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText()
        );

        registerButton.setDisable(true);
        registerButton.setText("Cadastrando...");

        new Thread(() -> {
            try {
                UserRegistrationResult result = userService.registerUser(request);

                Platform.runLater(() -> {
                    if (result.isSuccess()) {

                        NotificationService notificationService = new NotificationServiceImpl(
                                StageUtils.getStageFromNode(registerButton),
                                ToastNotification.AnimationType.SLIDE_DOWN_FROM_TOP
                        );
                        notificationService.showSuccess("Cadastro realizado");
                        clearForm();
                        try {
                            screenLoaderService.loadSignInScreen();
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert("Erro do Sistema", "Usuário cadastrado com sucesso, mas não foi possível voltar para a tela de login.", Alert.AlertType.WARNING);
                        }
                    } else {
                        showAlert("Erro no Cadastro", result.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    e.printStackTrace();
                    showAlert("Erro do Sistema", "Ocorreu um erro inesperado. Tente novamente.", Alert.AlertType.ERROR);
                });
            } finally {
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    registerButton.setText("Cadastrar");
                });
            }
        }).start();
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        cpfField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        firstNameField.requestFocus();
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