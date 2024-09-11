package com.gabrielosorio.gestor_inteligente.utils;

import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class StockDataUtils {

    private static final Logger log = Logger.getLogger(StockDataUtils.class.getName());
    private static final String stockFilePath = "src/main/resources/com/gabrielosorio/gestor_inteligente/data/stock.json";

    public static List<Stock> fetchStockData() {

        JSONArray stockJSONArray = getJSONArray(stockFilePath);
        List<Stock> stockData = new ArrayList<>();

        stockJSONArray.forEach(o -> {
            JSONObject stockJSONObject = (JSONObject) o;
            Stock stock = parseStock(stockJSONObject);
            stockData.add(stock);

        });

        return stockData;

    }

    private static Stock parseStock(JSONObject stockJSONObject){
        Integer id = stockJSONObject.getInt("id");
        int quantity = stockJSONObject.getInt("quantity");
        Timestamp lastUpdate =  Timestamp.valueOf(stockJSONObject.getString("lastUpdate"));

        JSONObject productJSONObject =  stockJSONObject.getJSONObject("product");
        Product product = parseProduct(productJSONObject);

        Stock stock = new Stock(product,quantity);
        stock.setId(id);
        stock.setLastUpdate(lastUpdate);
        return stock;
    }

    private static Category parseCategory(JSONObject categoryJSONObject){
        Integer id = categoryJSONObject.getInt("id");
        String description = categoryJSONObject.getString("description");
        Category category = new Category(id,description);
        return category;
    }

    private static Supplier parseSupplier(JSONObject supplierJSONObject){
        Integer id = supplierJSONObject.getInt("id");
        String name = supplierJSONObject.getString("name");
        Supplier supplier = new Supplier(id,name);
        return supplier;
    }

    private static Product parseProduct(JSONObject productJSONObject){

        Integer id = productJSONObject.getInt("id");
        Integer productCode = productJSONObject.getInt("productCode");
        String description = productJSONObject.getString("description");
        Optional<String> barCode = Optional.ofNullable(productJSONObject.optString("barCode"));
        BigDecimal costPrice = productJSONObject.getBigDecimal("costPrice");
        BigDecimal sellingPrice = productJSONObject.getBigDecimal("sellingPrice");
        String lastUpdateString = productJSONObject.getString("dateUpdate");
        String dateCreateString = productJSONObject.getString("dateCreate");
        Timestamp lastUpdate = Timestamp.valueOf(lastUpdateString);
        Timestamp dateCreate = Timestamp.valueOf(dateCreateString);
        Status status = Status.ACTIVE;

        /** extract data from supplier,category of product*/
        JSONObject categoryJSONObject = productJSONObject.getJSONObject("category");
        JSONObject supplierJSONObject = productJSONObject.getJSONObject("supplier");

        Category category = parseCategory(categoryJSONObject);
        Supplier supplier = parseSupplier(supplierJSONObject);

        Product product = Product.builder()
                .id(id)
                .productCode(productCode)
                .barCode(barCode)
                .description(description)
                .costPrice(costPrice)
                .sellingPrice(sellingPrice)
                .supplier(supplier)
                .category(category)
                .status(status)
                .dateCreate(dateCreate)
                .dateUpdate(lastUpdate)
                .dateDelete(null)
                .build();

        return product;
    }

    private static JSONArray getJSONArray(String filename){
        StringBuilder jsonContent = new StringBuilder();
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            while((line = reader.readLine()) != null){
                jsonContent.append(line);
            }
            return new JSONArray(jsonContent.toString());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveJSONToFile(String filename, JSONArray jsonArray){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(jsonArray.toString(4));
        } catch (IOException e) {
            log.severe("Error saving JSON to file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void putStockJSONObject(JSONObject stockJSONObject, Stock stock){

        Product stockProduct = stock.getProduct();

        /** extract the data from stock and update */
        stockJSONObject.put("id",stock.getId());
        stockJSONObject.put("quantity",stock.getQuantity());
        stockJSONObject.put("lastUpdate",stock.getLastUpdate());


        /** extract data from product and put */
        JSONObject productJSONObject = stockJSONObject.getJSONObject("product");
        productJSONObject.put("id",stockProduct.getId());
        productJSONObject.put("productCode",stockProduct.getProductCode());
        productJSONObject.put("costPrice",stockProduct.getCostPrice().toPlainString());
        productJSONObject.put("description",stockProduct.getDescription());
        productJSONObject.put("dateCreate",stockProduct.getDateCreate());
        productJSONObject.put("barCode",stockProduct.getBarCode());
        productJSONObject.put("sellingPrice",stockProduct.getSellingPrice().toPlainString());
        productJSONObject.put("markupPercent",stockProduct.getMarkupPercent());
        productJSONObject.put("profitPercent",stockProduct.getProfitPercent());
        productJSONObject.put("dateUpdate",stockProduct.getDateUpdate());

        /**extract data from supplier of product and put */
        JSONObject supplierJSONObject = productJSONObject.getJSONObject("supplier");
        supplierJSONObject.put("id",stockProduct.getSupplier().getId());
        supplierJSONObject.put("supplier",stockProduct.getSupplier().getName());

        /**extract data from supplier of product and put */
        JSONObject categoryJSONObject = productJSONObject.getJSONObject("category");
        categoryJSONObject.put("id",stockProduct.getCategory().getId());
        supplierJSONObject.put("supplier",stockProduct.getCategory().getDescription());

    }

    private static JSONObject findStockJSONObjectByProductCode(JSONArray stockJSONArray, long productCode){
        for (Object o : stockJSONArray) {
            JSONObject stockJSONObject = (JSONObject) o;
            JSONObject productJSONObject = stockJSONObject.getJSONObject("product");

            if (productJSONObject.getInt("productCode") == productCode) {
                log.info("Product found. ");
                final JSONObject stockJSONObjectFound = stockJSONObject;
                return stockJSONObjectFound;
            }
        }
        log.severe("Error searching for product, ID not found.");
        throw new IllegalArgumentException("Invalid product ID to find Stock JSON object. " + "ID: " + productCode);
    }

    public static void updateStock(Stock updatedStock){
        JSONArray stockJSONArray = getJSONArray(stockFilePath);
        JSONObject stockJSONObjectFound = findStockJSONObjectByProductCode(stockJSONArray,updatedStock.getProduct().getProductCode());
        putStockJSONObject(stockJSONObjectFound,updatedStock);
        saveJSONToFile(stockFilePath,stockJSONArray);
    }





}
