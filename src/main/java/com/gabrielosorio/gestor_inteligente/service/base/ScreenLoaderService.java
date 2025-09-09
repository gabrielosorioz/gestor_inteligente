package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.User;
import javafx.stage.Stage;
import java.io.IOException;

public interface ScreenLoaderService {

    /**
     * Carrega e exibe a tela principal da aplicação.
     *
     * @throws RuntimeException se houver erro ao carregar a tela
     */
    void loadMainApplication(User user) throws RuntimeException;

    void loadLogin(Stage stage) throws IOException;

    void loadSignInScreen() throws RuntimeException;

    void loadSignUpScreen(UserService userService) throws RuntimeException;

}
