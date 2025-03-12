package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovementType;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalRepositoryStrategy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLCheckoutMovementTypeStrategy extends TransactionalRepositoryStrategy<CheckoutMovementType> implements RepositoryStrategy<CheckoutMovementType> {

    private final QueryLoader qLoader;
    private final Logger log = Logger.getLogger(getClass().getName());

    public PSQLCheckoutMovementTypeStrategy(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
    }

    @Override
    public CheckoutMovementType add(CheckoutMovementType checkoutMovementType) {
        var query = qLoader.getQuery("insertCheckoutMovementType");
        try (var connection = getConnection();
             var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, checkoutMovementType.getName());
            ps.executeUpdate();

            try (var gKeys = ps.getGeneratedKeys()) {
                if (gKeys.next()) {
                    checkoutMovementType.setId(gKeys.getLong("id"));
                    log.info("CheckoutMovementType successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert CheckoutMovementType, no key generated.");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert CheckoutMovementType. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert CheckoutMovementType", e);
        }
        return checkoutMovementType;
    }

    @Override
    public Optional<CheckoutMovementType> find(long id) {
        var query = qLoader.getQuery("findCheckoutMovementTypeById");

        try (var connection = getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find CheckoutMovementType. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovementType search error.", e);
        }

        return Optional.empty();
    }

    @Override
    public List<CheckoutMovementType> findAll() {
        var checkoutMovementTypes = new ArrayList<CheckoutMovementType>();
        var query = qLoader.getQuery("findAllCheckoutMovementTypes");

        try (var connection = getConnection();
             var ps = connection.prepareStatement(query);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                var checkoutMovementType = mapResultSet(rs);
                checkoutMovementTypes.add(checkoutMovementType);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all CheckoutMovementTypes. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovementType find all error.", e);
        }
        return checkoutMovementTypes;
    }

    @Override
    public List<CheckoutMovementType> findBySpecification(Specification<CheckoutMovementType> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var checkoutMovementTypes = new ArrayList<CheckoutMovementType>();

        try (var connection = getConnection();
             var ps = connection.prepareStatement(query)) {

            if (params.size() != ps.getParameterMetaData().getParameterCount()) {
                throw new SQLException("Mismatch between provided parameters and expected query parameters.");
            }

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    var checkoutMovementType = mapResultSet(rs);
                    checkoutMovementTypes.add(checkoutMovementType);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find CheckoutMovementType by specification. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("CheckoutMovementType find by specification error.", e);
        }

        return checkoutMovementTypes;
    }

    @Override
    public CheckoutMovementType update(CheckoutMovementType checkoutMovementType) {
        var query = qLoader.getQuery("updateCheckoutMovementType");
        try (var connection = getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setString(1, checkoutMovementType.getName());
            ps.setLong(2, checkoutMovementType.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Failed to update CheckoutMovementType, no rows affected.");
            }

            log.info("CheckoutMovementType successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update CheckoutMovementType. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update CheckoutMovementType", e);
        }

        return checkoutMovementType;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteCheckoutMovementTypeById");
        try (var connection = getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                log.warning("No CheckoutMovementType found with id: " + id);
                return false;
            }

            log.info("CheckoutMovementType with id " + id + " successfully deleted.");
            return true;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete CheckoutMovementType. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException(e);
        }
    }

    private CheckoutMovementType mapResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        var checkoutMovementType = new CheckoutMovementType(id,name);
        return checkoutMovementType;
    }
}
