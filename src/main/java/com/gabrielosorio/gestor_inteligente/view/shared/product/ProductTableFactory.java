package com.gabrielosorio.gestor_inteligente.view.shared.product;

import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.utils.TableViewUtils;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class ProductTableFactory {

    private final String cssPath;

    public ProductTableFactory() {
        this("/com/gabrielosorio/gestor_inteligente/css/productTbView.css");
    }

    public ProductTableFactory(String cssPath) {
        this.cssPath = cssPath;
    }

    // Interface para presets (permite enums fixos e implementações custom)
    public interface Preset {
        void applyTo(TableView<Product> tableView);
    }

    // Enum com presets fixos — implementa Preset
    public enum TablePreset implements Preset {
        DEFAULT(120, 410, 160, 160, 130, 140),
        COMPACT(80, 300, 120, 120, 100, 100),
        POS_SCREEN(100, 250, 100, 0, 100, 0),
        WIDE_SCREEN(150, 500, 200, 200, 150, 180),
        MOBILE(60, 200, 80, 80, 60, 80);

        private final double codigo;
        private final double descricao;
        private final double precoCusto;
        private final double precoVenda;
        private final double quantidade;
        private final double categoria;

        TablePreset(double codigo, double descricao, double precoCusto,
                    double precoVenda, double quantidade, double categoria) {
            this.codigo = codigo;
            this.descricao = descricao;
            this.precoCusto = precoCusto;
            this.precoVenda = precoVenda;
            this.quantidade = quantidade;
            this.categoria = categoria;
        }

        @Override
        public void applyTo(TableView<Product> tableView) {
            if (codigo > 0) TableViewUtils.getColumnById(tableView, "productCodeProp").setPrefWidth(codigo);
            if (descricao > 0) TableViewUtils.getColumnById(tableView, "descriptionProp").setPrefWidth(descricao);
            if (precoCusto > 0) TableViewUtils.getColumnById(tableView, "costPriceProp").setPrefWidth(precoCusto);
            if (precoVenda > 0) TableViewUtils.getColumnById(tableView, "sellingPriceProp").setPrefWidth(precoVenda);
            if (quantidade > 0) TableViewUtils.getColumnById(tableView, "quantityProp").setPrefWidth(quantidade);
            if (categoria > 0) TableViewUtils.getColumnById(tableView, "getCategoryDescription").setPrefWidth(categoria);
        }

        /**
         * Retorna um Preset custom (não um enum) com valores fornecidos.
         */
        public static Preset custom(double codigo, double descricao, double precoCusto,
                                    double precoVenda, double quantidade, double categoria) {
            return new CustomPreset(codigo, descricao, precoCusto, precoVenda, quantidade, categoria);
        }

        // Implementação interna para presets customizados
        private static class CustomPreset implements Preset {
            private final double codigo;
            private final double descricao;
            private final double precoCusto;
            private final double precoVenda;
            private final double quantidade;
            private final double categoria;

            CustomPreset(double codigo, double descricao, double precoCusto,
                         double precoVenda, double quantidade, double categoria) {
                this.codigo = codigo;
                this.descricao = descricao;
                this.precoCusto = precoCusto;
                this.precoVenda = precoVenda;
                this.quantidade = quantidade;
                this.categoria = categoria;
            }

            @Override
            public void applyTo(TableView<Product> tableView) {
                if (codigo > 0) TableViewUtils.getColumnById(tableView, "productCodeProp").setPrefWidth(codigo);
                if (descricao > 0) TableViewUtils.getColumnById(tableView, "descriptionProp").setPrefWidth(descricao);
                if (precoCusto > 0) TableViewUtils.getColumnById(tableView, "costPriceProp").setPrefWidth(precoCusto);
                if (precoVenda > 0) TableViewUtils.getColumnById(tableView, "sellingPriceProp").setPrefWidth(precoVenda);
                if (quantidade > 0) TableViewUtils.getColumnById(tableView, "quantityProp").setPrefWidth(quantidade);
                if (categoria > 0) TableViewUtils.getColumnById(tableView, "getCategoryDescription").setPrefWidth(categoria);
            }
        }
    }

    // ------------------- Métodos de criação de tabela -------------------

    // Simples, usa preset DEFAULT
    public TableView<Product> createTable(ObservableList<Product> observableList) {
        return createTable(observableList, TablePreset.DEFAULT);
    }

    // Sobrecarga que aceita o tipo antigo (compatibilidade): delega para a API Preset
    public TableView<Product> createTable(ObservableList<Product> observableList, TablePreset preset) {
        return createTable(observableList, (Preset) preset);
    }

    // Nova API: aceita qualquer Preset (enum ou custom)
    public TableView<Product> createTable(ObservableList<Product> observableList, Preset preset) {
        TableViewFactory<Product> factory = new TableViewFactory<>(Product.class);
        TableView<Product> tableView = factory.createTableView(cssPath);

        configureTableLayout(tableView, preset);
        tableView.setItems(observableList);

        return tableView;
    }

    // Método com larguras customizadas (direto) — mantém comportamento anterior
    public TableView<Product> createTable(ObservableList<Product> observableList,
                                          double codigo, double descricao, double precoCusto,
                                          double precoVenda, double quantidade, double categoria) {
        TableViewFactory<Product> factory = new TableViewFactory<>(Product.class);
        TableView<Product> tableView = factory.createTableView(cssPath);

        configureTableLayoutCustom(tableView, codigo, descricao, precoCusto, precoVenda, quantidade, categoria);
        tableView.setItems(observableList);

        return tableView;
    }

    // Row factory — versão compatível com TablePreset
    public TableView<Product> createTableWithRowFactory(ObservableList<Product> observableList,
                                                        Callback<TableView<Product>, javafx.scene.control.TableRow<Product>> rowFactory) {
        return createTableWithRowFactory(observableList, TablePreset.DEFAULT, rowFactory);
    }

    // Row factory com preset (aceita TablePreset - delega)
    public TableView<Product> createTableWithRowFactory(ObservableList<Product> observableList,
                                                        TablePreset preset,
                                                        Callback<TableView<Product>, javafx.scene.control.TableRow<Product>> rowFactory) {
        return createTableWithRowFactory(observableList, (Preset) preset, rowFactory);
    }

    // Row factory nova API
    public TableView<Product> createTableWithRowFactory(ObservableList<Product> observableList,
                                                        Preset preset,
                                                        Callback<TableView<Product>, javafx.scene.control.TableRow<Product>> rowFactory) {
        TableView<Product> tableView = createTable(observableList, preset);
        tableView.setRowFactory(rowFactory);
        return tableView;
    }

    // ------------------- Configuração de layout -------------------

    // Configuração usando Preset
    public void configureTableLayout(TableView<Product> tableView, Preset preset) {
        preset.applyTo(tableView);
        applyColumnStyling(tableView);
    }

    // Configuração direta usando valores numéricos (mantido)
    public void configureTableLayoutCustom(TableView<Product> tableView,
                                           double codigo, double descricao, double precoCusto,
                                           double precoVenda, double quantidade, double categoria) {
        TablePreset.custom(codigo, descricao, precoCusto, precoVenda, quantidade, categoria).applyTo(tableView);
        applyColumnStyling(tableView);
    }

    private void applyColumnStyling(TableView<Product> tableView) {
        tableView.getColumns().forEach(c -> {
            c.setStyle("-fx-alignment: center;");
            TableViewUtils.resetColumnProps(c);
        });
    }

    public void setTableAnchors(TableView<Product> tableView, double top, double right, double bottom, double left) {
        AnchorPane.setTopAnchor(tableView, top);
        AnchorPane.setRightAnchor(tableView, right);
        AnchorPane.setBottomAnchor(tableView, bottom);
        AnchorPane.setLeftAnchor(tableView, left);
    }
}
