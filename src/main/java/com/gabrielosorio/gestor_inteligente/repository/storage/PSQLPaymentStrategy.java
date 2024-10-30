package com.gabrielosorio.gestor_inteligente.repository.storage;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.repository.strategy.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLPaymentStrategy implements RepositoryStrategy<Payment> {

    private final QueryLoader qLoader;
    private final ConnectionFactory connFactory;
    private final Logger log = Logger.getLogger(getClass().getName());

    public PSQLPaymentStrategy() {
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.connFactory = ConnectionFactory.getInstance();
    }

    @Override
    public Payment add(Payment payment) {
        var query = qLoader.getQuery("insertPayment");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

            ps.setString(1,payment.getDescription());
            ps.executeUpdate();

            try(var gKeys = ps.getGeneratedKeys()){
                if(gKeys.next()){
                    payment.setId(gKeys.getLong("id"));
                    log.info("Payment successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert payment, no key generated. ");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert product", e);

        }
        return payment;
    }

    @Override
    public Optional<Payment> find(long id) {
        var query = qLoader.getQuery("findPaymentById");

        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            try(var rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find payment. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Payment search error. ",e);

        }

        return Optional.empty();
    }

    @Override
    public List<Payment> findAll() {
        var payments = new ArrayList<Payment>();
        var query = qLoader.getQuery("findAllPayments");

        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            while(rs.next()){
                var payment = mapResultSet(rs);
                payments.add(payment);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all payments. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Payment find all error. ",e);

        }
        return payments;
    }

    @Override
    public List<Payment> findBySpecification(Specification<Payment> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var payments = new ArrayList<Payment>();

        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            if(params.size() != ps.getParameterMetaData().getParameterCount()){
                throw new SQLException("Mismatch between provided parameters and expected query parameters.");
            }

            for(int i = 0 ; i < params.size(); i++){
                ps.setObject(i + 1, params.get(i));
            }

            try(var rs = ps.executeQuery()){
                while(rs.next()){
                    var payment = mapResultSet(rs);
                    payments.add(payment);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find payment by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Payment find by specification error. ",e);

        }

        return payments;
    }

    @Override
    public Payment update(Payment payment) {
        var query = qLoader.getQuery("updatePayment");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setString(1, payment.getDescription());
            ps.setLong(2, payment.getId());

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Failed to update payment, no rows affected.");
            }

            log.info("Payment successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update product", e);
        }

        return payment;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deletePaymentById");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)) {

            ps.setLong(1,id);

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                log.warning("No Payment found with id: " + id);
                return false;
            }

            log.info("Payment with id " + id + " successfully deleted.");
            return true;


        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete payment. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException(e);

        }
    }

    private Payment mapResultSet(ResultSet rs) throws SQLException {
        var p = new Payment(rs.getString("description"));
        p.setId(rs.getLong("id"));
        return p;
    }

 }
