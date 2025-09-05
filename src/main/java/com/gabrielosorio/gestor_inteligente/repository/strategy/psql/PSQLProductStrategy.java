package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.exception.DuplicateProductException;
import com.gabrielosorio.gestor_inteligente.model.Category;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.ProductRepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalRepositoryStrategyV2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLProductStrategy extends TransactionalRepositoryStrategyV2<Product,Long> implements ProductRepositoryStrategy {

    private final QueryLoader qLoader;
    private Logger log = Logger.getLogger(getClass().getName());
    private final PSQLCategoryStrategy categoryStrategy;
    private final PSQLSupplierStrategy supplierStrategy;


    public PSQLProductStrategy(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        categoryStrategy = new PSQLCategoryStrategy(connectionFactory);
        supplierStrategy = new PSQLSupplierStrategy(connectionFactory);
        qLoader = new QueryLoader(connectionFactory.getDBScheme());
    }

    @Override
    public Product add(Product product) {
        var query = qLoader.getQuery("insertProduct");
        Connection connection = null;
        try {
            connection = getConnection();

            try(var ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)) {

                long id = generateId();
                ps.setLong(1,id);
                ps.setLong(2,product.getProductCode());
                ps.setString(3,product.getBarCode().orElse(null));
                ps.setString(4, product.getDescription());
                ps.setBigDecimal(5,product.getCostPrice());
                ps.setBigDecimal(6,product.getSellingPrice());
                ps.setDouble(7,product.getProfitPercent());
                ps.setDouble(8,product.getMarkupPercent());
                ps.setLong(9,product.getQuantity());
                ps.setString(10,product.getStatus().name());
                ps.setTimestamp(11,product.getDateCreate());
                ps.setTimestamp(12,product.getDateUpdate());
                ps.setTimestamp(13,product.getDateDelete());
                ps.setObject(14, product.getCategory().map(Category::getId).orElse(null),Types.BIGINT);
                ps.setObject(15, product.getSupplier().map(Supplier::getId).orElse(null),Types.BIGINT);

                ps.executeUpdate();

                try (var gKeys = ps.getGeneratedKeys()){
                    if(gKeys.next()){
                        product.setId(gKeys.getLong(1));
                        log.info("Product successfully inserted.");
                    } else {
                        throw new SQLException("Failed to insert product, no key generated. ");
                    }
                }

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to insert product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Failed to insert product", e);
            }

        } catch (SQLException e){
            log.log(Level.SEVERE, "Failed to insert product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert product", e);
        } finally {
            closeConnection(connection);
        }

        return product;
    }

    @Override
    public Optional<Product> find(Long id) {
        var query = qLoader.getQuery("findProductById");
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query)){

                ps.setLong(1,id);

                try(var rs = ps.executeQuery()){
                    if(rs.next()){
                        return Optional.of(mapResultSet(rs));
                    }
                }

            }
            catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Product search error. ",e);
            }

        } catch (SQLException e){
            log.log(Level.SEVERE, "Failed to find product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Product search error. ",e);
        } finally {
            closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        var products = new ArrayList<Product>();
        var query = qLoader.getQuery("findAllProducts");
        Connection connection = null;

        try {
            connection = getConnection();
            try(var ps = connection.prepareStatement(query);
                var rs = ps.executeQuery()){

                while(rs.next()){
                    var product = mapResultSet(rs);
                    products.add(product);
                }

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find all products. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Product find all error. ",e);
            }
        } catch (SQLException e){
            log.log(Level.SEVERE, "Failed to find all products. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Product find all error. ",e);
        } finally {
            closeConnection(connection);
        }

        return products;
    }

    @Override
    public List<Product> findBySpecification(Specification<Product> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var products = new ArrayList<Product>();
        Connection connection = null;

        try {
            connection = getConnection();
            try(var ps = connection.prepareStatement(query)){

                if(params.size() != ps.getParameterMetaData().getParameterCount()){
                    throw new SQLException("Mismatch between provided parameters and expected query parameters.");
                }

                for(int i = 0 ; i < params.size(); i++){
                    ps.setObject(i + 1, params.get(i));
                }

                try(var rs = ps.executeQuery()){
                    while(rs.next()){
                        var product = mapResultSet(rs);
                        products.add(product);
                    }
                }

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find product by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Product find by specification error. ",e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find product by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Product find by specification error. ",e);
        } finally {
            closeConnection(connection);
        }

        return products;
    }

    @Override
    public Product update(Product product) {
        Connection connection = null;
        try {
            connection = getConnection();
            var query = qLoader.getQuery("updateProduct");
            try (var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, product.getProductCode());
                ps.setString(2, product.getBarCode().orElse(null));
                ps.setString(3, product.getDescription());
                ps.setBigDecimal(4, product.getCostPrice());
                ps.setBigDecimal(5, product.getSellingPrice());
                ps.setDouble(6, product.getProfitPercent());
                ps.setDouble(7, product.getMarkupPercent());
                ps.setLong(8, product.getQuantity());
                ps.setString(9, product.getStatus().name());
                ps.setTimestamp(10, product.getDateCreate());
                ps.setTimestamp(11, product.getDateUpdate());
                ps.setTimestamp(12, product.getDateDelete());
                ps.setObject(13, product.getSupplier().isPresent() ? product.getSupplier().get().getId() : null, Types.BIGINT);
                ps.setObject(14, product.getCategory().isPresent() ? product.getCategory().get().getId() : null, Types.BIGINT);
                ps.setLong(15, product.getId());

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Failed to update product, no rows affected.");
                }

                log.info("Product successfully updated.");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateProductException("Product code already exists: " + product.getProductCode(), e);
            }
            log.log(Level.SEVERE, "Failed to update product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update product", e);
        } finally {
            closeConnection(connection);
        }
        return product;
    }

    @Override
    public boolean remove(Long id) {
        var query = qLoader.getQuery("deleteProductById");
        Connection connection = null;
        try {
            connection = getConnection();
            try(var ps = connection.prepareStatement(query)){

                ps.setLong(1,id);

                int affectedRows = ps.executeUpdate();

                if(affectedRows == 0){
                    log.warning("No product found with id: " + id);
                    return false;
                }

                log.info("Product with id " + id + " successfully deleted.");
                return true;

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to delete product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    private Product mapResultSet (ResultSet rs) throws SQLException {
        var p = Product.builder()
                .id(rs.getLong("product_id"))
                .productCode(rs.getLong("product_code"))
                .barCode(Optional.ofNullable(rs.getString("bar_code")))
                .description(rs.getString("description"))
                .costPrice(rs.getBigDecimal("cost_price"))
                .sellingPrice(rs.getBigDecimal("selling_price"))
                .quantity(rs.getLong("quantity"))
                .status(Status.valueOf(rs.getString("status")))
                .dateCreate(rs.getTimestamp("date_create"))
                .dateUpdate(rs.getTimestamp("date_update"))
                .dateDelete(rs.getTimestamp("date_delete"))
                .category(findCategoryById(rs.getLong("category_id")))
                .supplier(findSupplierById(rs.getLong("supplier_id")))
                .build();
        return p;
    }

    private Optional<Category> findCategoryById(long id){
        return categoryStrategy.find(id);
    }

    private Optional<Supplier> findSupplierById(long id){
        return supplierStrategy.find(id);
    }

    private long generateId(){
        String query = qLoader.getQuery("productMaxId");
        long newId = 1;

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            if(rs.next()){
                long maxId = rs.getLong("max_id");
                newId = maxId + 1;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error getting product ID. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Error getting product ID.\"",e);
        }
        return newId;
    }

    @Override
    public long genPCode() {
        long generatedPCode = 0;
        String query = qLoader.getQuery("maxProductCode");

        try (var connection = getConnection();
             var ps = connection.prepareStatement(query);
             var rs = ps.executeQuery()) {

            if (rs.next()) {
                generatedPCode = rs.getLong("max_pcode") + 1;
            }

            while (existsPCode(generatedPCode)) {
                generatedPCode++;
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error generating product code.", e);
            throw new RuntimeException("Error generating product code.", e);
        }

        return generatedPCode;
    }

    @Override
    public boolean existsPCode(long pCode){
        var query = qLoader.getQuery("existsProductCode");

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,pCode);

            try(var rs = ps.executeQuery()){
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error checking product code existence. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Error checking product code existence\"",e);
        }
    }

    @Override
    public boolean existsBarCode(String barCode) {
        var query = qLoader.getQuery("productByBarCode");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setString(1,barCode.trim());

            try(var rs = ps.executeQuery()){
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking barcode existence.",e);
        }
    }

}
