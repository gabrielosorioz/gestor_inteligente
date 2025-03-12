package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.model.Permission;
import com.gabrielosorio.gestor_inteligente.model.Role;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.repository.impl.*;
import com.gabrielosorio.gestor_inteligente.repository.strategy.psql.*;
import com.gabrielosorio.gestor_inteligente.service.base.*;
import com.gabrielosorio.gestor_inteligente.service.impl.*;
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

    public static void showPaymentScreen(SaleTableViewController saleTableViewOp) {
        var saleProducts = saleTableViewOp.getItems();

        if(saleProducts.isEmpty()){
            logger.warning("The sale products of sale table view is null.");
            return;
        }

        final var sale = new Sale(saleProducts);

        try {
            var paymentViewController = initializeController(sale,saleTableViewOp);
            var paymentStage = createPaymentStage(paymentViewController);
            paymentStage.showAndWait();

        } catch (Exception e) {
            logger.severe("Error loading payment view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static PaymentViewController initializeController(Sale sale, SaleTableViewController saleTableViewOp){
        var saleService = createSaleService();

        Permission readPermission = new Permission();
        readPermission.setId(1L);
        readPermission.setName("READ");

        Permission writePermission = new Permission();
        writePermission.setId(2L);
        writePermission.setName("WRITE");

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");
        adminRole.setPermissions(List.of(readPermission, writePermission));


        User user = new User(
                1L,                           // id
                "John",                       // firstName
                "Doe",                        // lastName
                "1234567890",                 // cellphone
                "john.doe@example.com",       // email
                "12345678909",                // cpf
                "securePassword123",          // password
                adminRole,                    // role
                LocalDateTime.now(),          // createdAt
                LocalDateTime.now()           // updatedAt
        );

        return new PaymentViewController(user,sale,saleService,saleTableViewOp);
    }

    private static SaleService createSaleService() {
        SaleRepository saleRepository = new SaleRepository();
        var saleProductService = createSaleProductService();
        var salePaymentService = createSalePaymentService();
        var productService = createProductService();
        var checkoutMovementService = createCheckoutMovementService();
        var checkoutService = createCheckoutService(checkoutMovementService);
        var saleCheckoutMovRepo = createSaleCheckoutMovementService();
        saleRepository.init(new PSQLSaleStrategy(ConnectionFactory.getInstance()));
        return new SaleServiceImpl(saleRepository,saleProductService,salePaymentService,checkoutMovementService,checkoutService,productService,saleCheckoutMovRepo);
    }

    private static SaleCheckoutMovementService createSaleCheckoutMovementService(){
        var saleCheckoutMovementRepository = new SaleCheckoutMovementRepository();
        saleCheckoutMovementRepository.init(new PSQLSaleCheckoutMovementStrategy(ConnectionFactory.getInstance()));
        return new SaleCheckoutMovementServiceImpl(saleCheckoutMovementRepository);
    }

    private static SaleProductService createSaleProductService(){
        var saleProductRepo = new SaleProductRepository();
        saleProductRepo.init(new PSQLSaleProductStrategy(ConnectionFactory.getInstance()));
        return new SaleProductServiceImpl(saleProductRepo);
    }

    private static ProductService createProductService(){
        var productStrategy = new PSQLProductStrategy(ConnectionFactory.getInstance());
        var productRepository = new ProductRepository(productStrategy);
        return new ProductServiceImpl(productRepository);
    }

    private static SalePaymentService createSalePaymentService(){
        var salePaymentRepo = new SalePaymentRepository();
        salePaymentRepo.init(new PSQLSalePaymentStrategy(ConnectionFactory.getInstance()));
        return new SalePaymentServiceImpl(salePaymentRepo);
    }

    private static CheckoutMovementService createCheckoutMovementService(){
        var checkoutMovementRepo = new CheckoutMovementRepository();
        checkoutMovementRepo.init(new PSQLCheckoutMovementStrategy(ConnectionFactory.getInstance()));
        return new CheckoutMovementServiceImpl(checkoutMovementRepo);
    }

    private static CheckoutService createCheckoutService(CheckoutMovementService checkoutMovementService){
        var checkoutRepository = new CheckoutRepositoryImpl();
        checkoutRepository.init(new PSQLCheckoutStrategy(ConnectionFactory.getInstance()));
        return new CheckoutServiceImpl(checkoutRepository, checkoutMovementService);
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
