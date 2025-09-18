package com.gabrielosorio.gestor_inteligente.view.payment;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.model.Permission;
import com.gabrielosorio.gestor_inteligente.model.Role;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.repository.base.RepositoryFactory;
import com.gabrielosorio.gestor_inteligente.repository.factory.PSQLRepositoryFactory;
import com.gabrielosorio.gestor_inteligente.service.impl.*;
import com.gabrielosorio.gestor_inteligente.view.sale.SaleTableViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class PaymentViewHelper {

    private static final Logger logger = Logger.getLogger(PaymentViewHelper.class.getName());
    private static final RepositoryFactory REPOSITORY_FACTORY = new PSQLRepositoryFactory(ConnectionFactory.getInstance(DBScheme.POSTGRESQL));
    private static final ServiceFactory SERVICE_FACTORY = new ServiceFactory(REPOSITORY_FACTORY,ConnectionFactory.getInstance(DBScheme.POSTGRESQL));


    public static void showPaymentScreen(User user,SaleTableViewController saleTableViewOp) {
        var saleProducts = saleTableViewOp.getItems();

        if(saleProducts.isEmpty()){
            logger.warning("The sale products of sale table view is null.");
            return;
        }

        final var sale = new Sale(saleProducts);

        try {
            var paymentViewController = initializeController(user,sale,saleTableViewOp);
            var paymentStage = createPaymentStage(paymentViewController);
            paymentStage.showAndWait();

        } catch (Exception e) {
            logger.severe("Error loading payment view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static PaymentViewController initializeController(User user,Sale sale, SaleTableViewController saleTableViewOp){
        var saleService = SERVICE_FACTORY.getSaleService();
        return new PaymentViewController(user,sale,saleService,saleTableViewOp);
    }

    private static Stage createPaymentStage(PaymentViewController controller) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/sale/PaymentView.fxml"));
        fxmlLoader.setController(controller);

        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);

        stage.initOwner(GestorInteligenteApp.getPrimaryStage());
        stage.initModality(Modality.APPLICATION_MODAL);

        return stage;
    }

}
