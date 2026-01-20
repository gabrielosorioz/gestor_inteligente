package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalRepositoryStrategyV2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLCheckoutMovementStrategy extends TransactionalRepositoryStrategyV2<CheckoutMovement,Long> implements RepositoryStrategy<CheckoutMovement,Long>, BatchInsertable<CheckoutMovement> {

    private final QueryLoader qLoader;
    private final PSQLCheckoutStrategy checkoutStrategy;
    private final PSQLPaymentStrategy paymentStrategy;
    private final PSQLCheckoutMovementTypeStrategy checkoutMovementTypeStrategy;
    private final Logger log = Logger.getLogger(getClass().getName());


    public PSQLCheckoutMovementStrategy(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        this.checkoutStrategy = new PSQLCheckoutStrategy(connectionFactory);
        this.checkoutMovementTypeStrategy = new PSQLCheckoutMovementTypeStrategy(connectionFactory);
        this.paymentStrategy = new PSQLPaymentStrategy(connectionFactory);
        this.qLoader = new QueryLoader(connectionFactory.getDBScheme());
    }

    @Override
    public CheckoutMovement add(CheckoutMovement checkoutMovement) {
        if (checkoutMovement == null) {
            throw new IllegalArgumentException("checkoutMovement must not be null");
        }
        var query = qLoader.getQuery("insertCheckoutMovement");
        log.info("SQL INSERT: ID=" + checkoutMovement.getId() + " ValorParaGravar=" + checkoutMovement.getValue());
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, checkoutMovement.getCheckout().getId());
                ps.setTimestamp(2, Timestamp.valueOf(checkoutMovement.getDateTime()));
                ps.setBigDecimal(3, checkoutMovement.getValue());
                ps.setString(4, checkoutMovement.getObs());
                ps.setLong(5, checkoutMovement.getPayment().getId());
                ps.setLong(6, checkoutMovement.getMovementType().getId());
                ps.executeUpdate();

                try (var gKeys = ps.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        checkoutMovement.setId(gKeys.getLong("id"));
                        log.info("CheckoutMovement successfully inserted.");
                    } else {
                        throw new SQLException("Failed to insert CheckoutMovement, no key generated.");
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert CheckoutMovement. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert CheckoutMovement", e);
        } finally {
            closeConnection(connection);
        }
        return checkoutMovement;
    }



    @Override
    public Optional<CheckoutMovement> find(Long id) {
        var query = qLoader.getQuery("findCheckoutMovementById");
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query)) {
                ps.setLong(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find checkout movement. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovement search error.", e);
        } finally {
            closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public List<CheckoutMovement> findAll() {
        var checkoutMovements = new ArrayList<CheckoutMovement>();
        var query = qLoader.getQuery("findAllCheckoutMovements");
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query);
                 var rs = ps.executeQuery()) {
                while (rs.next()) {
                    var checkoutMovement = mapResultSet(rs);
                    checkoutMovements.add(checkoutMovement);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all checkout movements. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovement find all error.", e);
        } finally {
            closeConnection(connection);
        }
        return checkoutMovements;
    }


    @Override
    public List<CheckoutMovement> findBySpecification(Specification specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var checkoutMovements = new ArrayList<CheckoutMovement>();
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query)) {
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
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find checkout movements by specification. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovement find by specification error.", e);
        } finally {
            closeConnection(connection);
        }
        return checkoutMovements;
    }


    @Override
    public CheckoutMovement update(CheckoutMovement newCheckoutMovement) {
        var query = qLoader.getQuery("updateCheckoutMovement");
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query)) {
                ps.setLong(1, newCheckoutMovement.getCheckout().getId());
                ps.setTimestamp(2, Timestamp.valueOf(newCheckoutMovement.getDateTime()));
                ps.setBigDecimal(3, newCheckoutMovement.getValue());
                ps.setString(4, newCheckoutMovement.getObs());
                ps.setLong(5, newCheckoutMovement.getPayment().getId());
                ps.setLong(6, newCheckoutMovement.getMovementType().getId());
                ps.setLong(7, newCheckoutMovement.getId());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Failed to update CheckoutMovement, no rows affected.");
                }
                log.info("CheckoutMovement successfully updated.");
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update CheckoutMovement. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update CheckoutMovement", e);
        } finally {
            closeConnection(connection);
        }
        return newCheckoutMovement;
    }


    @Override
    public boolean remove(Long id) {
        var query = qLoader.getQuery("deleteCheckoutMovementById");
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query)) {
                ps.setLong(1, id);
                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    log.warning("No CheckoutMovement found with id " + id);
                    return false;
                } else {
                    log.info("CheckoutMovement with id " + id + " successfully deleted.");
                    return true;
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete CheckoutMovement. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to delete CheckoutMovement.", e);
        } finally {
            closeConnection(connection);
        }
    }

    private Optional<Checkout> findCheckoutById(long id){
        return checkoutStrategy.find(id);
    }

    private Optional<Payment> findPaymentById(long id){
        return paymentStrategy.find(id);
    }

    private Optional<CheckoutMovementType> findCheckoutMovementTypeById(long id){
       return checkoutMovementTypeStrategy.find(id);
    }

    public CheckoutMovement mapResultSet(ResultSet rs) throws SQLException {
        var checkoutMovement = new CheckoutMovement();
        checkoutMovement.setId(rs.getLong("id"));
        checkoutMovement.setCheckout(findCheckoutById(rs.getLong("checkout_id")).get());
        checkoutMovement.setMovementType(findCheckoutMovementTypeById(rs.getLong("checkoutmovement_type_id")).get());
        checkoutMovement.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        checkoutMovement.setPayment(findPaymentById(rs.getLong("payment_id")).get());
        checkoutMovement.getPayment().setValue(rs.getBigDecimal("value"));
        checkoutMovement.setValue(rs.getBigDecimal("value"));
        checkoutMovement.setObs(rs.getString("obs"));
        return checkoutMovement;
    }

    @Override
    public List<CheckoutMovement> addAll(List<CheckoutMovement> checkoutMovements) {
        var query = qLoader.getQuery("insertCheckoutMovement");
        Connection connection = null;

        try {
            connection = getConnection();

            try(var ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){

                for(CheckoutMovement checkoutMovement: checkoutMovements){
                    log.info("SQL INSERT: ID=" + checkoutMovement.getId() + " ValorParaGravar=" + checkoutMovement.getValue());
                    ps.setLong(1, checkoutMovement.getCheckout().getId());
                    ps.setTimestamp(2, Timestamp.valueOf(checkoutMovement.getDateTime()));
                    ps.setBigDecimal(3,checkoutMovement.getValue());
                    ps.setString(4,checkoutMovement.getObs());
                    ps.setLong(5,checkoutMovement.getPayment().getId());
                    ps.setLong(6,checkoutMovement.getMovementType().getId());
                    ps.addBatch();
                }

                ps.executeBatch();

                try(var gKeys = ps.getGeneratedKeys()){
                    int index = 0;
                    while(gKeys.next()){
                        checkoutMovements.get(index++).setId(gKeys.getLong("id"));
                    }
                }

                log.info("All checkoutMovements successfully inserted.");
            } catch (SQLException e){
                throw new RuntimeException("Failed to batch insert checkoutMovements. " + e.getMessage(),e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection " + e.getMessage(),e);
        } finally {
            closeConnection(connection);
        }
        return checkoutMovements;
    }

}
