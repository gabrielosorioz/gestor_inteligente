package com.gabrielosorio.gestor_inteligente.view;
import com.gabrielosorio.gestor_inteligente.GestorInteligenteApp;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.service.CheckoutService;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CheckoutMovementController implements Initializable, ShortcutHandler {

    private Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private AnchorPane mainContent;

    @FXML
    private DatePicker endDate,startDate;

    @FXML
    private Label statusLbl, initialCash,cash;

    @FXML
    private ImageView statusView;

    private Node checkoutMovementDialog;

    private CheckoutMovementDialogController checkoutMovementDialogController;

    private final CheckoutService checkoutService;

    private Checkout checkout;

    public CheckoutMovementController(CheckoutService checkoutService){
        this.checkoutService = checkoutService;
        var user = new User();
        user.setFirstName("Gabriel");
        user.setLastName("Osório");
        this.checkout = checkoutService.openCheckout(user);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setTodayDate();
        initialCash.setText(TextFieldUtils.formatText(checkout.getInitialCash().toPlainString()));
        cash.setText(bigDecimalToMonetaryString(checkout.getInitialCash()));



    }

    private void setTodayDate(){
        LocalDate today = LocalDate.now();
        startDate.setValue(today);
        endDate.setValue(today);
    }

    private void open() {
        /**
         * 1. Buscar no banco de dados um caixa pela data atual (Somente data, não data e hora)
         * 1.2 Se houver um registro de caixa no qual a data de abertura é igual á data atual,
         * exibir os dados, senão, abrir um novo caixa e exibir os dados deste novo caixa

         **/
    }

    protected void showCheckoutMovementDialog() {
        try {
            if (checkoutMovementDialog == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(GestorInteligenteApp.class.getResource("fxml/CheckoutMovementDialog.fxml"));
                checkoutMovementDialog = fxmlLoader.load();
                checkoutMovementDialogController = fxmlLoader.getController();
            }
            if (!mainContent.getChildren().contains(checkoutMovementDialog)) {
                mainContent.getChildren().add(checkoutMovementDialog);
                checkoutMovementDialogController.requestFocusOnField();
            }

            checkoutMovementDialogController.getBtnOk().setOnMouseClicked(mouseEvent -> {
                var initialCash = checkoutMovementDialogController.getValue();
                checkoutService.setInitialCash(checkout.getId(), new Payment(PaymentMethod.DINHEIRO,initialCash),"");
                checkoutMovementDialogController.close();
            });

        } catch (Exception e) {
            log.severe("ERROR at load checkout movement dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String bigDecimalToMonetaryString(BigDecimal value){
        return TextFieldUtils.formatText(value.toPlainString());
    }


    @Override
    public void handleShortcut(KeyCode keyCode) {
        if(keyCode.equals(KeyCode.F1)){
            showCheckoutMovementDialog();
        }
    }
}
