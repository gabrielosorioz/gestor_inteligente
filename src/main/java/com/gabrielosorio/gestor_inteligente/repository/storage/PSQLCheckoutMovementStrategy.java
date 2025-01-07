package com.gabrielosorio.gestor_inteligente.repository.storage;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.TypeCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.RepositoryStrategy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLCheckoutMovementStrategy implements RepositoryStrategy<CheckoutMovement>, BatchInsertable<CheckoutMovement> {

    private final QueryLoader qLoader;
    private final ConnectionFactory connFactory;
    private final PSQLCheckoutStrategy checkoutStrategy;
    private final PSQLPaymentStrategy paymentStrategy;
    private final PSQLSaleStrategy saleStrategy;
    private Logger log = Logger.getLogger(getClass().getName());


    public PSQLCheckoutMovementStrategy(ConnectionFactory connFactory) {
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.checkoutStrategy = new PSQLCheckoutStrategy(ConnectionFactory.getInstance());
        this.saleStrategy = new PSQLSaleStrategy();
        this.paymentStrategy = new PSQLPaymentStrategy();
        this.connFactory = connFactory;
    }

    @Override
    public CheckoutMovement add(CheckoutMovement checkoutMovement) {
        if (checkoutMovement == null) {
            throw new IllegalArgumentException("checkoutMovement must not be null");
        }

        var query = qLoader.getQuery("insertCheckoutMovement");
        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            checkoutMovement.getSale().ifPresentOrElse(
                    sale -> {
                        try {
                            ps.setLong(1, sale.getId());
                        } catch (SQLException e) {
                            throw new RuntimeException("Error when setting the sale_id parameter", e);
                        }
                    },
                    () -> {
                        try {
                            ps.setNull(1, java.sql.Types.BIGINT);
                        } catch (SQLException e) {
                            throw new RuntimeException("Error setting the sale_id parameter to NULL", e);
                        }
                    }
            );

            ps.setLong(2, checkoutMovement.getCheckout().getId());
            ps.setString(3, checkoutMovement.getType().name());
            ps.setTimestamp(4, Timestamp.valueOf(checkoutMovement.getDateTime()));
            ps.setLong(5, checkoutMovement.getPayment().getId());
            ps.setBigDecimal(6, checkoutMovement.getValue());
            ps.setString(7, checkoutMovement.getObs());

            ps.executeUpdate();

            try (var gKeys = ps.getGeneratedKeys()) {
                if (gKeys.next()) {
                    checkoutMovement.setId(gKeys.getLong("id"));
                    log.info("CheckoutMovement successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert CheckoutMovement, no key generated.");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert CheckoutMovement. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert CheckoutMovement", e);
        }
        return checkoutMovement;
    }



    @Override
    public Optional<CheckoutMovement> find(long id) {
        var query = qLoader.getQuery("findCheckoutMovementById");
        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find checkout movement. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovement search error.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<CheckoutMovement> findAll() {
        var checkoutMovements = new ArrayList<CheckoutMovement>();
        var query = qLoader.getQuery("findAllCheckoutMovements");

        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                var checkoutMovement = mapResultSet(rs);
                checkoutMovements.add(checkoutMovement);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all checkout movements. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovement find all error.", e);
        }

        return checkoutMovements;
    }


    @Override
    public List<CheckoutMovement> findBySpecification(Specification<CheckoutMovement> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var checkoutMovements = new ArrayList<CheckoutMovement>();

        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query)) {

            if (params.size() != ps.getParameterMetaData().getParameterCount()) {
                throw new SQLException("Mismatch between provided parameters and expected query parameters.");
            }

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    var checkoutMovement = mapResultSet(rs);
                    checkoutMovements.add(checkoutMovement);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find checkout movements by specification. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovement find by specification error.", e);
        }

        return checkoutMovements;
    }


    @Override
    public CheckoutMovement update(CheckoutMovement newCheckoutMovement) {
        var query = qLoader.getQuery("updateCheckoutMovement");

        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query)) {

            if (newCheckoutMovement.getSale().isPresent()) {
                ps.setLong(1, newCheckoutMovement.getSale().get().getId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, newCheckoutMovement.getCheckout().getId());
            ps.setString(3, newCheckoutMovement.getType().name());
            ps.setTimestamp(4, Timestamp.valueOf(newCheckoutMovement.getDateTime()));
            ps.setLong(5, newCheckoutMovement.getPayment().getId());
            ps.setBigDecimal(6, newCheckoutMovement.getValue());
            ps.setString(7, newCheckoutMovement.getObs());
            ps.setLong(8, newCheckoutMovement.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Failed to update CheckoutMovement, no rows affected.");
            }

            log.info("CheckoutMovement successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update CheckoutMovement. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update CheckoutMovement", e);
        }

        return newCheckoutMovement;
    }


    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteCheckoutMovementById");
        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                log.warning("No CheckoutMovement found with id " + id);
                return false;
            } else {
                log.info("CheckoutMovement with id " + id + " successfully deleted.");
                return true;
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete CheckoutMovement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to delete CheckoutMovement.", e); // Rethrow the exception as runtime exception
        }
    }


    private Optional<Checkout> findCheckoutById(long id){
        return checkoutStrategy.find(id);
    }

    private Optional<Sale> findSaleById(long id){
        return saleStrategy.find(id);
    }

    private Optional<Payment> findPaymentById(long id){
        return paymentStrategy.find(id);
    }

    public CheckoutMovement mapResultSet(ResultSet rs) throws SQLException {
        var checkoutMovement = new CheckoutMovement();
        checkoutMovement.setId(rs.getLong("id"));
        checkoutMovement.setSale(findSaleById(rs.getLong("sale_id")));
        checkoutMovement.setCheckout(findCheckoutById(rs.getLong("checkout_id")).get());
        checkoutMovement.setType(TypeCheckoutMovement.valueOf(rs.getString("type")));
        checkoutMovement.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        checkoutMovement.setPayment(findPaymentById(rs.getLong("payment_id")).get());
        checkoutMovement.setValue(rs.getBigDecimal("value"));
        checkoutMovement.setObs(rs.getString("obs"));
        return checkoutMovement;
    }

    @Override
    public List<CheckoutMovement> addAll(List<CheckoutMovement> checkoutMovements) {
        var query = qLoader.getQuery("insertCheckoutMovement");
        Connection connection = null;

        try {
            connection = connFactory.getConnection();
            connection.setAutoCommit(false);

            try(var ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){

                for(CheckoutMovement checkoutMovement: checkoutMovements){

                    checkoutMovement.getSale().ifPresentOrElse(
                    sale -> {
                            try {
                                ps.setLong(1, sale.getId());
                            } catch (SQLException e) {
                                throw new RuntimeException("Error when setting the sale_id parameter", e);
                            }
                        },
                        () -> {
                            try {
                                ps.setNull(1, java.sql.Types.BIGINT);
                            } catch (SQLException e) {
                                throw new RuntimeException("Error setting the sale_id parameter to NULL", e);
                            }
                        }
                    );

                    ps.setLong(2, checkoutMovement.getCheckout().getId());
                    ps.setString(3, checkoutMovement.getType().name());
                    ps.setTimestamp(4, Timestamp.valueOf(checkoutMovement.getDateTime()));
                    ps.setLong(5, checkoutMovement.getPayment().getId());
                    ps.setBigDecimal(6, checkoutMovement.getValue());
                    ps.setString(7, checkoutMovement.getObs());
                    ps.addBatch();
                }

                ps.executeBatch();
                connection.commit();

                try(var gKeys = ps.getGeneratedKeys()){
                    int index = 0;
                    while(gKeys.next()){
                        checkoutMovements.get(index++).setId(gKeys.getLong("id"));
                    }
                }

                log.info("All checkoutMovements successfully inserted.");
            } catch (SQLException e){
                if(connection != null){
                    try {
                        connection.rollback();
                        log.warning("Transaction rolled back due to an error: " + e.getMessage());
                    } catch (SQLException rollBackEx) {
                        log.severe("RollBack failed " + rollBackEx.getMessage());
                    }
                }
                throw new RuntimeException("Failed to batch insert checkoutMovements. " + e.getMessage(),e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection " + e.getMessage(),e);
        } finally {
            if(connection != null){
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx){
                    log.severe("Failed to close the connection " + closeEx.getMessage());
                }
            }
        }
        return checkoutMovements;
    }

}
