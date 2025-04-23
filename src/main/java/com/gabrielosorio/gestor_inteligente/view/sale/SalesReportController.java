package com.gabrielosorio.gestor_inteligente.view.sale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;

import java.net.URL;
import java.util.ResourceBundle;

public class SalesReportController implements Initializable {

    @FXML
    private PieChart categoryChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Criar os dados fictícios
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Cozinha", 350),
                new PieChart.Data("Casa e Decoração", 200),
                new PieChart.Data("Brinquedos", 150),
                new PieChart.Data("Limpeza e Organização", 180),
                new PieChart.Data("Outros", 120)
        );

        // Calcular o total de vendas
        double totalSales = pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        // Atualizar os rótulos para mostrar a porcentagem
        for (PieChart.Data data : pieChartData) {
            double percentage = (data.getPieValue() / totalSales) * 100;
            data.nameProperty().set(String.format("%s (%.1f%%)", data.getName(), percentage));
        }

        // Adicionar os dados ao PieChart
        categoryChart.setData(pieChartData);

        // Tornar os rótulos visíveis nas fatias
        categoryChart.setLabelsVisible(true);

        // Configurar a legenda para aparecer automaticamente
        categoryChart.setLegendVisible(true);
    }
}
