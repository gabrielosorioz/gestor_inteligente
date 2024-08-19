package com.gabrielosorio.gestor_inteligente.utils;

import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Stock;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

public class StockDataUtils {

    private static final Logger log = Logger.getLogger(StockDataUtils.class.getName());

    public static List<Stock> fetchStockData(){
        String fileName = "src/main/resources/com/gabrielosorio/gestor_inteligente/data/products.json";
        List<Stock> stockData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder jsonString = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                jsonString.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonString.toString());

            jsonArray.forEach(e -> {
                JSONObject productJsonObject = (JSONObject) e;

                /** extract data from products */
                Integer id = productJsonObject.getInt("id");
                Integer productID = productJsonObject.getInt("productId");
                String description = productJsonObject.getString("description");
                String barCode = productJsonObject.getString("barCode");
                BigDecimal costPrice = productJsonObject.getBigDecimal("costPrice");
                BigDecimal sellingPrice = productJsonObject.getBigDecimal("sellingPrice");
                Status status = Status.ACTIVE;
                Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());

                /** extract data from supplier */
                JSONObject supplierJsonObject = productJsonObject.getJSONObject("supplier");
                Integer supplierId = supplierJsonObject.getInt("id");
                String supplierName = supplierJsonObject.getString("name");
                Supplier supplier = new Supplier(supplierId,supplierName);

                /** extract data from category */
                JSONObject categoryJsonObject = productJsonObject.getJSONObject("category");
                Integer categoryId = categoryJsonObject.getInt("id");
                String categoryDescription = categoryJsonObject.getString("description");
                Category category = new Category(categoryId,categoryDescription);


                Product stockProduct = Product.builder()
                        .id(id)
                        .productId(productID)
                        .barCode(barCode)
                        .description(description)
                        .costPrice(costPrice)
                        .sellingPrice(sellingPrice)
                        .supplier(supplier)
                        .category(category)
                        .status(status)
                        .dateCreate(now)
                        .dateUpdate(now)
                        .dateDelete(null)
                        .build();

                Stock stock = new Stock(stockProduct);
                stockData.add(stock);
            });

            return stockData;


        } catch (FileNotFoundException e) {
            log.severe("Error fetching stock data, file not founded: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.severe("Error fetching stock data: " + e.getMessage());
            throw new RuntimeException(e);
        }


    }

}
