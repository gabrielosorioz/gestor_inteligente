package com.gabrielosorio.gestor_inteligente.view.shared.product;

import com.gabrielosorio.gestor_inteligente.model.Product;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class ProductSearcher {

    private final ObservableList<Product> productList;

    public ProductSearcher(ObservableList<Product> productList) {
        this.productList = productList;
    }

    public FilteredList<Product> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new FilteredList<>(productList, p -> true);
        }

        final String searchLower = searchTerm.toLowerCase();
        return new FilteredList<>(productList, product -> matchesProduct(product, searchLower));
    }

    private boolean matchesProduct(Product product, String searchTerm) {
        if (isNumeric(searchTerm)) {
            long searchNumber = Long.parseLong(searchTerm);

            if (searchNumber == product.getProductCode()) {
                return true;
            }

            return product.getBarCode()
                    .filter(ProductSearcher::isNumeric)
                    .map(Long::parseLong)
                    .filter(barCode -> barCode == searchNumber)
                    .isPresent();
        }

        return product.getDescription().toLowerCase().contains(searchTerm);
    }

    private static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}