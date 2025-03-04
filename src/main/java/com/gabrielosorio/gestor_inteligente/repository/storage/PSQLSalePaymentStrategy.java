package com.gabrielosorio.gestor_inteligente.repository.storage;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SalePayment;
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

public class PSQLSalePaymentStrategy extends TransactionalRepositoryStrategy<SalePayment> implements RepositoryStrategy<SalePayment>, BatchInsertable<SalePayment> {

    private final QueryLoader qLoader;
    private final PSQLSaleStrategy saleStrategy;
    private final PSQLPaymentStrategy paymentStrategy;
    private final Logger log = Logger.getLogger(getClass().getName());

    public PSQLSalePaymentStrategy(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        saleStrategy = new PSQLSaleStrategy(connectionFactory);
        paymentStrategy = new PSQLPaymentStrategy(connectionFactory);
    }

    @Override
    public SalePayment add(SalePayment salePayment) {
        var query = qLoader.getQuery("insertSalePayment");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

            ps.setLong(1,salePayment.getSaleId());
            ps.setLong(2,salePayment.getPaymentId());
            ps.setBigDecimal(3,salePayment.getPayment().getValue());
            ps.executeUpdate();

            try(var gKeys = ps.getGeneratedKeys()){
                if(gKeys.next()){
                    salePayment.setId(gKeys.getLong("id"));
                    log.info("SalePayment successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert product, no key generated.");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert sale payment. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert sale payment", e);

        }
        return salePayment;
    }

    @Override
    public Optional<SalePayment> find(long id) {
        var query = qLoader.getQuery("findSalePaymentById");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            try(var rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Product search error. ",e);
        }

        return Optional.empty();
    }

    @Override
    public List<SalePayment> findAll() {
        var salePayments = new ArrayList<SalePayment>();
        var query = qLoader.getQuery("findAllSalePayments");

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            while(rs.next()){
                var salePayment = mapResultSet(rs);
                salePayments.add(salePayment);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all products. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Product find all error. ",e);
        }
        return salePayments;
    }

    @Override
    public List<SalePayment> findBySpecification(Specification<SalePayment> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var salePayments = new ArrayList<SalePayment>();

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            if(params.size() != ps.getParameterMetaData().getParameterCount()){
                throw new SQLException("Mismatch between provided parameters and expected query parameters.");
            }

            for(int i = 0 ; i < params.size(); i++){
                ps.setObject(i + 1, params.get(i));
            }

            try(var rs = ps.executeQuery()){
                while(rs.next()){
                    var product = mapResultSet(rs);
                    salePayments.add(product);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find sale payment by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Payment find by specification error. ",e);
        }
        return salePayments;
    }

    @Override
    public SalePayment update(SalePayment salePayment) {
        var query = qLoader.getQuery("updateSalePayment");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,salePayment.getSaleId());
            ps.setLong(2,salePayment.getPaymentId());
            ps.setBigDecimal(3,salePayment.getPayment().getValue());
            ps.setLong(4,salePayment.getId());

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Failed to update product, no rows affected.");
            }

            log.info("Sale Payment successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to sale payment. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to sale payment", e);
        }
        return salePayment;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteSalePaymentById");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                log.warning("No Sale Payment found with id: " + id);
                return false;
            }

            log.info("Sale Payment with id " + id + " successfully deleted.");
            return true;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete Sale Payment. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException(e);
        }
    }

    private SalePayment mapResultSet(ResultSet rs) throws SQLException {
        var paym = findPaymentById(rs.getLong("payment_id"));
        var sale = findSaleById(rs.getLong("saleid"));

        if(paym.isPresent() && sale.isPresent()){
            var salePay = new SalePayment(paym.get(),sale.get());
            salePay.setId(rs.getLong("id"));
            return salePay;

        } else {
            log.severe("Payment or Sale is null");
            return null;
        }
    }

    private Optional<Sale> findSaleById(long id){
        return saleStrategy.find(id);
    }

    private Optional<Payment> findPaymentById(long id){
        return paymentStrategy.find(id);
    }

    @Override
    public List<SalePayment> addAll(List<SalePayment> salePayments) {
        var query = qLoader.getQuery("insertSalePayment");
        Connection connection = null;

        try {
            connection = getConnection();

            // Check and adjust autoCommit only for non-shared connections
            if (!isSharedConnection()) {
                connection.setAutoCommit(false);
            }

            try(var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
                for(SalePayment salePayment: salePayments){
                    ps.setLong(1,salePayment.getSaleId());
                    ps.setLong(2,salePayment.getPaymentId());
                    ps.setBigDecimal(3,salePayment.getAmount());
                    ps.addBatch();
                }

                ps.executeBatch();

                if(!isSharedConnection()){
                    connection.commit();
                }

                try(var gKeys = ps.getGeneratedKeys()){
                    int index = 0;
                    while(gKeys.next()){
                        salePayments.get(index++).setId(gKeys.getLong("id"));
                    }
                }

                log.info("All SalePayments successfully inserted.");
            } catch (SQLException e) {
                if(!isSharedConnection()){
                    try {
                        connection.rollback();
                        log.warning("Transaction rolled back due to an error: " + e.getMessage());
                    } catch (SQLException rollBackEx) {
                        log.severe("RollBack failed " + rollBackEx.getMessage());
                    }
                }
                throw new RuntimeException("Failed to batch insert SalePayments " + e.getMessage(),e);
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

        return salePayments;
    }

}
