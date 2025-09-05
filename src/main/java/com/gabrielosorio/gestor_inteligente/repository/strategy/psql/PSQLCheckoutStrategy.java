package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLCheckoutStrategy implements RepositoryStrategy<Checkout,Long> {

    private final QueryLoader qLoader;
    private final ConnectionFactory connFactory;
    private Logger log = Logger.getLogger(getClass().getName());

    public PSQLCheckoutStrategy(ConnectionFactory connFactory) {
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.connFactory = connFactory;
    }


    @Override
    public Checkout add(Checkout checkout) {
        var query = qLoader.getQuery("insertCheckout");
        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, checkout.getStatus().getName());
            ps.setTimestamp(2, Timestamp.valueOf(checkout.getOpenedAt()));
            ps.setTimestamp(3,
            checkout.getClosedAt() != null ? Timestamp.valueOf(checkout.getClosedAt()) : null);
            ps.setBigDecimal(4, checkout.getInitialCash());
            ps.setBigDecimal(5, checkout.getTotalEntry());
            ps.setBigDecimal(6, checkout.getTotalExit());
            ps.setBigDecimal(7, checkout.getClosingBalance());
            ps.setString(8, checkout.getOpenedBy().getFirstName());
            ps.setString(9,
                    checkout.getClosedBy() != null ? checkout.getClosedBy().getLastName() : null);

            ps.setTimestamp(10,
                    checkout.getCreatedAt() != null ? Timestamp.valueOf(checkout.getCreatedAt()) : null);
            ps.setTimestamp(11,
                    checkout.getUpdatedAt() != null ? Timestamp.valueOf(checkout.getUpdatedAt()) : null);

            ps.executeUpdate();

            try (var gKeys = ps.getGeneratedKeys()) {
                if (gKeys.next()) {
                    checkout.setId(gKeys.getLong("id"));
                    log.info("Checkout successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert checkout, no key generated.");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert checkout. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert checkout", e);
        }
        return checkout;
    }


    @Override
    public Optional<Checkout> find(Long id) {
        var query = qLoader.getQuery("findCheckoutById");
        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find checkout. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Checkout search error.", e);
        }
        return Optional.empty();
    }


    @Override
    public List<Checkout> findAll() {
        var checkouts = new ArrayList<Checkout>();
        var query = qLoader.getQuery("findAllCheckouts");

        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                var checkout = mapResultSet(rs);
                checkouts.add(checkout);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all checkouts. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Checkout find all error.", e);
        }

        return checkouts;
    }


    @Override
    public List<Checkout> findBySpecification(Specification<Checkout> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var checkouts = new ArrayList<Checkout>();

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
                    var checkout = mapResultSet(rs);
                    checkouts.add(checkout);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find checkout by specification. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Checkout find by specification error.", e);
        }

        return checkouts;
    }


    @Override
    public Checkout update(Checkout newCheckout) {
        var query = qLoader.getQuery("updateCheckout");
        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setString(1, newCheckout.getStatus().getName());
            ps.setTimestamp(2, Timestamp.valueOf(newCheckout.getOpenedAt()));
            ps.setTimestamp(3, newCheckout.getClosedAt() != null ? Timestamp.valueOf(newCheckout.getClosedAt()) : null);
            ps.setBigDecimal(4, newCheckout.getInitialCash());
            ps.setBigDecimal(5, newCheckout.getTotalEntry());
            ps.setBigDecimal(6, newCheckout.getTotalExit());
            ps.setBigDecimal(7, newCheckout.getClosingBalance());
            ps.setString(8, newCheckout.getOpenedBy().getFirstName());
            ps.setString(9, newCheckout.getClosedBy().getFirstName());
            ps.setTimestamp(10, Timestamp.valueOf(newCheckout.getCreatedAt()));
            ps.setTimestamp(11, Timestamp.valueOf(newCheckout.getUpdatedAt()));
            ps.setLong(12, newCheckout.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Failed to update checkout, no rows affected.");
            }

            log.info("Checkout successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update checkout. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update checkout", e);
        }

        return newCheckout;
    }

    @Override
    public boolean remove(Long id) {
        var query = qLoader.getQuery("deleteCheckoutById");
        try (var connection = connFactory.getConnection();
             var ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                log.warning("No checkout found with id " + id);
                return false;
            } else {
                log.info("Checkout with id " + id + " successfully deleted.");
                return true;
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete checkout. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to delete checkout.", e);
        }
    }


    private Checkout mapResultSet(ResultSet rs) throws SQLException {
        return new Checkout(
                rs.getLong("id"),
                CheckoutStatus.fromDescription(rs.getString("status")),
                rs.getTimestamp("opened_at").toLocalDateTime(),
                rs.getTimestamp("closed_at") != null ? rs.getTimestamp("closed_at").toLocalDateTime() : null,
                rs.getBigDecimal("initial_cash"),
                rs.getBigDecimal("total_entry"),
                rs.getBigDecimal("total_exit"),
                rs.getBigDecimal("closing_balance"),
                new User(),
                new User(),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }

}
