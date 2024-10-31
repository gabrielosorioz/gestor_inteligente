package com.gabrielosorio.gestor_inteligente.view;

import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.repository.SalePaymentRepository;
import com.gabrielosorio.gestor_inteligente.repository.SaleProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.SaleRepository;
import com.gabrielosorio.gestor_inteligente.repository.storage.PSQLSalePaymentStrategy;
import com.gabrielosorio.gestor_inteligente.repository.storage.PSQLSaleProductStrategy;
import com.gabrielosorio.gestor_inteligente.repository.storage.PSQLSaleStrategy;
import com.gabrielosorio.gestor_inteligente.service.SalePaymentService;
import com.gabrielosorio.gestor_inteligente.service.SaleProductService;
import com.gabrielosorio.gestor_inteligente.service.SaleService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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
        var saleProductService = createSaleProductService();
        var salePaymentService = createSalePaymentService();
        return new PaymentViewController(sale,saleService,saleProductService,salePaymentService,saleTableViewOp);
    }

    private static SaleService createSaleService() {
        SaleRepository saleRepository = new SaleRepository();
        saleRepository.init(new PSQLSaleStrategy());
        return new SaleService(saleRepository);
    }

    private static SaleProductService createSaleProductService(){
        var saleProductRepo = new SaleProductRepository();
        saleProductRepo.init(new PSQLSaleProductStrategy());
        return new SaleProductService(saleProductRepo);
    }

    private static SalePaymentService createSalePaymentService(){
        var salePaymentRepo = new SalePaymentRepository();
        salePaymentRepo.init(new PSQLSalePaymentStrategy());
        return new SalePaymentService(salePaymentRepo);
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