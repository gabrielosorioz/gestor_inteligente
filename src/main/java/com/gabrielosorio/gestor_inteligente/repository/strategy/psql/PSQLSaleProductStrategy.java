package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleProduct;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalRepositoryStrategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLSaleProductStrategy extends TransactionalRepositoryStrategy<SaleProduct> implements RepositoryStrategy<SaleProduct>, BatchInsertable<SaleProduct> {

    private final QueryLoader qLoader;
    private final PSQLProductStrategy productStrategy;
    private final PSQLSaleStrategy saleStrategy;
    private Logger log = Logger.getLogger(getClass().getName());

    public PSQLSaleProductStrategy(ConnectionFactory connectionFactory){
        super(connectionFactory);
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.productStrategy = new PSQLProductStrategy(connectionFactory);
        this.saleStrategy = new PSQLSaleStrategy(connectionFactory);
    }

    @Override
    public SaleProduct add(SaleProduct saleProduct) {
        var query = qLoader.getQuery("insertSaleProduct");

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

            ps.setLong(1,saleProduct.getSale().getId());
            ps.setLong(2,saleProduct.getProduct().getId());
            ps.setLong(3,saleProduct.getQuantity());
            ps.setBigDecimal(4,saleProduct.getUnitPrice());
            ps.setBigDecimal(5,saleProduct.getOriginalSubtotal());
            ps.setBigDecimal(6,saleProduct.getSubTotal());
            ps.setBigDecimal(7,saleProduct.getDiscount());

            ps.executeUpdate();

            try(var gKeys = ps.getGeneratedKeys()){
                if(gKeys.next()){
                    saleProduct.setId(gKeys.getLong("id"));
                    log.info("Sale Product successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert sale product, no key generated.");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert Sale Product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert Sale Product", e);

        }
        return saleProduct;
    }

    @Override
    public Optional<SaleProduct> find(long id) {
        var query = qLoader.getQuery("findSaleProductById");
        try(var connection = getConnection();
           var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            try(var rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find Sale Product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to find Sale Product", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SaleProduct> findAll() {
        var saleProducts = new ArrayList<SaleProduct>();
        var query = qLoader.getQuery("findAllSaleProduct");

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            while(rs.next()){
                var saleProduct = mapResultSet(rs);
                saleProducts.add(saleProduct);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all sale products. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Product find all error. ",e);

        }
        return saleProducts;
    }

    @Override
    public List<SaleProduct> findBySpecification(Specification<SaleProduct> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var saleProducts = new ArrayList<SaleProduct>();

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            if(params.size() != ps.getParameterMetaData().getParameterCount()){
                throw new SQLException("Mismatch between provided parameters and expected query parameters.");
            }

            for(int i = 0; i < params.size(); i++){
                ps.setObject(i+ 1, params.get(i));
            }

            try (var rs = ps.executeQuery()){
                while(rs.next()){
                    var saleProduct = mapResultSet(rs);
                    saleProducts.add(saleProduct);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find sale product by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Product find by specification error. ",e);
        }
        return saleProducts;
    }

    @Override
    public SaleProduct update(SaleProduct saleProduct) {
        var query = qLoader.getQuery("updateSaleProduct");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1, saleProduct.getSaleId());
            ps.setLong(2, saleProduct.getProduct().getId());
            ps.setLong(3, saleProduct.getQuantity());
            ps.setBigDecimal(4, saleProduct.getUnitPrice());
            ps.setBigDecimal(5, saleProduct.getOriginalSubtotal());
            ps.setBigDecimal(6, saleProduct.getSubTotal());
            ps.setBigDecimal(7, saleProduct.getDiscount());
            ps.setLong(8, saleProduct.getId());

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Failed to update sale product, no rows affected.");
            }

            log.info("Sale Product successfully updated.");


        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update product", e);
        }
        return saleProduct;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteSaleProductById");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);
            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                log.warning("No sale product found with id: " + id);
                return false;
            }

            log.info("Sale Product with id " + id + " successfully deleted.");
            return true;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete sale product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException(e);
        }
    }

    private Optional<Product> findProductById(long id){
        return productStrategy.find(id);
    }

    private Optional<Sale> findSaleById(long id){
        return saleStrategy.find(id);
    }

    private SaleProduct mapResultSet(ResultSet rs) throws SQLException {
        var sP = new SaleProduct();
        sP.setId(rs.getLong("id"));
        sP.setSaleId(rs.getLong("saleId"));
        sP.setProduct(findProductById(rs.getLong("product_id")).orElse(null));
        sP.setSale(findSaleById(rs.getLong("saleId")).orElse(null));
        sP.setQuantity(rs.getLong("quantity"));
        sP.setDiscount(rs.getBigDecimal("discount"));
        sP.setSubTotal(rs.getBigDecimal("subtotal"));
        sP.setOriginalSubtotal(rs.getBigDecimal("originalsubtotal"));
        sP.setUnitPrice(rs.getBigDecimal("unitprice"));
        return sP;
    }

    @Override
    public List<SaleProduct> addAll(List<SaleProduct> saleProducts) {
        var query = qLoader.getQuery("insertSaleProduct");
        Connection connection = null;

        try {
            connection = getConnection();

            // Check and adjust autoCommit only for non-shared connections
            if (!isSharedConnection()) {
                connection.setAutoCommit(false);
            }

            try(var ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){

                for(SaleProduct saleProduct: saleProducts){
                    ps.setLong(1,saleProduct.getSale().getId());
                    ps.setLong(2,saleProduct.getProduct().getId());
                    ps.setLong(3,saleProduct.getQuantity());
                    ps.setBigDecimal(4,saleProduct.getUnitPrice());
                    ps.setBigDecimal(5,saleProduct.getOriginalSubtotal());
                    ps.setBigDecimal(6,saleProduct.getSubTotal());
                    ps.setBigDecimal(7,saleProduct.getDiscount());
                    ps.addBatch();
                }

                ps.executeBatch();

                if(!isSharedConnection()){
                    connection.commit();
                }

                try(var gKeys = ps.getGeneratedKeys()){
                    int index = 0;
                    while(gKeys.next()){
                        saleProducts.get(index++).setId(gKeys.getLong("id"));
                    }
                }

                log.info("All SaleProducts successfully inserted.");
            } catch (SQLException e){
                if(!isSharedConnection()){
                    try {
                        connection.rollback();
                        log.warning("Transaction rolled back due to an error: " + e.getMessage());
                    } catch (SQLException rollBackEx) {
                        log.severe("RollBack failed " + rollBackEx.getMessage());
                    }
                }

                throw new RuntimeException("Failed to batch insert SaleProducts. " + e.getMessage(),e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection " + e.getMessage(),e);
        } finally {
            if(connection != null){
                if(!isSharedConnection()){
                    try {
                        connection.setAutoCommit(true);
                        connection.close();

                    } catch (SQLException closeEx){
                        log.severe("Failed to close the connection " + closeEx.getMessage());
                    }
                } else {
                    closeConnection(connection);
                }
            }
        }
        return saleProducts;
    }
}
