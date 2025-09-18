module com.gabrielosorio.gestor_inteligente {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires jbcrypt;
    requires javafx.graphics;

    opens com.gabrielosorio.gestor_inteligente to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente;
    exports com.gabrielosorio.gestor_inteligente.model;
    opens com.gabrielosorio.gestor_inteligente.model to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.model.enums;
    opens com.gabrielosorio.gestor_inteligente.model.enums to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.utils;
    opens com.gabrielosorio.gestor_inteligente.utils to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.events;
    opens com.gabrielosorio.gestor_inteligente.events to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view.main;
    opens com.gabrielosorio.gestor_inteligente.view.main to javafx.fxml;
    opens com.gabrielosorio.gestor_inteligente.view.shared to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view.shared;
    exports com.gabrielosorio.gestor_inteligente.view.checkout;
    opens com.gabrielosorio.gestor_inteligente.view.checkout to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view.product;
    opens com.gabrielosorio.gestor_inteligente.view.product to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view.sale;
    opens com.gabrielosorio.gestor_inteligente.view.sale to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view.payment;
    opens com.gabrielosorio.gestor_inteligente.view.payment to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view.main.helpers;
    opens com.gabrielosorio.gestor_inteligente.view.main.helpers to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view.checkout.helpers;
    opens com.gabrielosorio.gestor_inteligente.view.checkout.helpers to javafx.fxml;
    opens com.gabrielosorio.gestor_inteligente.view.signin to javafx.fxml;
    opens com.gabrielosorio.gestor_inteligente.view.signup to javafx.fxml;

}