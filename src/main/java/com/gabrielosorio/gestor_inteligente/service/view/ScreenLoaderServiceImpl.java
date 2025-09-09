package com.gabrielosorio.gestor_inteligente.service.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.service.base.NotificationService;
import com.gabrielosorio.gestor_inteligente.service.base.ScreenLoaderService;
import com.gabrielosorio.gestor_inteligente.service.base.UserService;
import com.gabrielosorio.gestor_inteligente.service.impl.NotificationServiceImpl;
import com.gabrielosorio.gestor_inteligente.service.impl.ServiceFactory;
import com.gabrielosorio.gestor_inteligente.view.main.MainNavigationController;
import com.gabrielosorio.gestor_inteligente.view.signin.SignInController;
import com.gabrielosorio.gestor_inteligente.view.signup.SignUpController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ScreenLoaderServiceImpl implements ScreenLoaderService {

    private final ServiceFactory serviceFactory;

    public ScreenLoaderServiceImpl(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public void loadMainApplication(User user) throws RuntimeException {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/MainNavigation.fxml"));
            MainNavigationController mainController = new MainNavigationController(serviceFactory,user);
            loader.setController(mainController);

            Scene mainScene = new Scene(loader.load());

            Stage primaryStage = GestorInteligenteApp.getPrimaryStage();
            primaryStage.setScene(mainScene);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar a tela principal", e);
        }
    }

    @Override
    public void loadLogin(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/SignIn.fxml"));
        SignInController signInController = new SignInController(serviceFactory);
        fxmlLoader.setController(signInController);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @Override
    public void loadSignUpScreen(UserService userService) throws RuntimeException {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/SignUp.fxml"));
            loader.setController(new SignUpController(userService,this));

            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = GestorInteligenteApp.getPrimaryStage();
            stage.setScene(scene);
            stage.setTitle("Cadastro de Usuário - Gestor Inteligente");
            stage.show();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar tela de cadastro", e);
        }
    }

    /**
     * Carrega a tela de login
     * @throws RuntimeException se não conseguir carregar a tela
     */
    public void loadSignInScreen() throws RuntimeException {
        try {
            FXMLLoader loader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/SignIn.fxml"));
            loader.setController(new SignInController(serviceFactory));

            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = GestorInteligenteApp.getPrimaryStage();
            stage.setScene(scene);
            stage.setTitle("Login - Gestor Inteligente");
            stage.show();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar tela de login", e);
        }
    }
}
