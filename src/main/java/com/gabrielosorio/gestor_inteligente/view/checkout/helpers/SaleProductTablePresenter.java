package com.gabrielosorio.gestor_inteligente.view.checkout.helpers;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.view.table.AfterModelUpdateCallback;
import com.gabrielosorio.gestor_inteligente.view.table.TableViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Presenter para gerenciar a TableView de produtos em uma venda
 */
public class SaleProductTablePresenter {

    private final ObservableList<SaleProduct> saleProductList;
    public AfterModelUpdateCallback<SaleProduct, Object> afterModelUpdateCallback;
    private TableView<SaleProduct> tableView;
    private Runnable onItemsChanged;

    public SaleProductTablePresenter() {
        this.saleProductList = FXCollections.observableArrayList();
    }

    public TableView<SaleProduct> createTableView() {
        var builder = TableViewFactory.builder(SaleProduct.class)
                .withStylesheet("/com/gabrielosorio/gestor_inteligente/css/saleViewDetails.css")
                .columnWidth("productCodeProperty", 62.0)
                .columnWidth("productDescriptionProperty", 180.0,null,null)
                .columnWidth("quantityProperty", 66.0)
                .columnWidth("unitPriceProperty", 114.0)
                .columnWidth("discount", 114.0)
                .columnWidth("subtotalProperty", 83.0)
                .lockColumns(true)
                .actionColumn(
                        "action",
                        "",
                        30.0,
                        "action-col-box",
                        () -> {
                            ImageView iv = new ImageView(
                                    new Image("file:src/main/resources/com/gabrielosorio/gestor_inteligente/image/icons8-excluir-24-white.png")
                            );
                            iv.setFitWidth(14);
                            iv.setFitHeight(14);
                            iv.setPreserveRatio(true);
                            return iv;
                        },
                        item -> {
                            saleProductList.remove(item);

                            if (onItemsChanged != null) {
                                onItemsChanged.run();
                            }

                            if (tableView != null) {
                                tableView.refresh();
                            }
                        }


                );

        if (afterModelUpdateCallback != null) {
            builder.afterModelUpdate(afterModelUpdateCallback);
        }

        tableView = builder.build();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setItems(saleProductList);

        return tableView;
    }

    public ObservableList<SaleProduct> getSaleProductList() {
        return saleProductList;
    }

    public void setAfterModelUpdateCallback(AfterModelUpdateCallback<SaleProduct, Object> callback) {
        this.afterModelUpdateCallback = callback;
    }

    public void setOnItemsChanged(Runnable onItemsChanged) {
        this.onItemsChanged = onItemsChanged;
    }
}