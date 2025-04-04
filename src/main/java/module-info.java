module com.example.gestor_inteligente {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires java.sql;


    opens com.gabrielosorio.gestor_inteligente to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente;
    exports com.gabrielosorio.gestor_inteligente.model;
    opens com.gabrielosorio.gestor_inteligente.model to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.model.enums;
    opens com.gabrielosorio.gestor_inteligente.model.enums to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.view;
    opens com.gabrielosorio.gestor_inteligente.view to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.utils;
    opens com.gabrielosorio.gestor_inteligente.utils to javafx.fxml;
    exports com.gabrielosorio.gestor_inteligente.events;
    opens com.gabrielosorio.gestor_inteligente.events to javafx.fxml;
}