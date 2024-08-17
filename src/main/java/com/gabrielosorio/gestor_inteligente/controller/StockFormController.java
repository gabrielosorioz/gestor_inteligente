package com.gabrielosorio.gestor_inteligente.controller;
import com.gabrielosorio.gestor_inteligente.utils.AutoCompleteField;
import com.gabrielosorio.gestor_inteligente.utils.TextFieldUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class StockFormController implements Initializable {

    @FXML
    private TextField idField,barCodeField,descriptionField,costPriceField,sellingPriceField,
    markupField,quantityField;

    @FXML
    private TextField categoryField,supplierField;

    @FXML
    private ListView<String> categoryList;

    @FXML
    private ListView<String> supplierList;


    ArrayList<String> categories = new ArrayList<>(Arrays.asList("Cozinha", "Caneca", "Brinquedos", "Dia dos pais", "Dia das mães",
            "Dia dos namorados", "Cozinha utensílios"));
    ArrayList<String> suppliers = new ArrayList<>(Arrays.asList("Loja 1","Loja 2", "Fornecedor X", "Fornecedor Y"));

    private void setUpperCaseField(List<TextField> fields){
        fields.forEach(field -> {
            TextFieldUtils.setUpperCaseTextFormatter(field);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<TextField> fields = new ArrayList<>(Arrays.asList(
                idField,
                barCodeField,descriptionField,
                costPriceField,sellingPriceField,
                markupField,quantityField)
        );

        setUpperCaseField(fields);
        categoryList.getItems().addAll(categories);
        supplierList.getItems().addAll(suppliers);
        AutoCompleteField auto = new AutoCompleteField(categoryField,categoryList);
        AutoCompleteField auto2 = new AutoCompleteField(supplierField,supplierList);

    }


}
