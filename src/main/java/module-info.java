module com.example.gestor_inteligente {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.gabrielosorio.gestor_inteligente to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente;
    exports com.gabrielosorio.gestor_inteligente.model;
    opens com.gabrielosorio.gestor_inteligente.model to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.model.enums;
    opens com.gabrielosorio.gestor_inteligente.model.enums to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.controller;
    opens com.gabrielosorio.gestor_inteligente.controller to javafx.fxml;
}