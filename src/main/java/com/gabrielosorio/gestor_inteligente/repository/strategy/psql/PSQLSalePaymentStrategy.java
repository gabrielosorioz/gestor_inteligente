package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Payment;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SalePayment;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.*;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLSalePaymentStrategy extends TransactionalRepositoryStrategyV2<SalePayment,Long> implements RepositoryStrategy<SalePayment,Long>, BatchInsertable<SalePayment>, BatchUpdatable<SalePayment>,
        BatchDeletable<Long> {

    private final QueryLoader qLoader;
    private final PSQLSaleStrategy saleStrategy;
    private final PSQLPaymentStrategy paymentStrategy;
    private final Logger log = Logger.getLogger(getClass().getName());

    public PSQLSalePaymentStrategy(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        paymentStrategy = new PSQLPaymentStrategy(connectionFactory);
        saleStrategy = new PSQLSaleStrategy(connectionFactory);
        this.qLoader = new QueryLoader(connectionFactory.getDBScheme());
    }

    @Override
    public SalePayment add(SalePayment salePayment) {
        var query = qLoader.getQuery("insertSalePayment");
        Connection connection = null; // 1. Declara fora

        try {
            connection = getConnection(); // 2. Obtém sem try-with-resources

            try (var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, salePayment.getSaleId());
                ps.setLong(2, salePayment.getPaymentId());
                ps.setBigDecimal(3, salePayment.getPayment().getValue());
                ps.setInt(4, salePayment.getInstallments());
                ps.executeUpdate();

                try (var gKeys = ps.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        salePayment.setId(gKeys.getLong("id"));
                        log.info("SalePayment successfully inserted.");
                    } else {
                        throw new SQLException("Failed to insert product, no key generated.");
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert sale payment. {0} {1} {2}",
                    new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert sale payment", e);
        } finally {
            closeConnection(connection); // 3. Fecha de forma segura
        }
        return salePayment;
    }


    @Override
    public Optional<SalePayment> find(Long id) {
        var query = qLoader.getQuery("findSalePaymentById");
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
            log.log(Level.SEVERE, "Failed to find product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Product search error. ", e);
        } finally {
            closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public List<SalePayment> findAll() {
        var salePayments = new ArrayList<SalePayment>();
        var query = qLoader.getQuery("findAllSalePayments");
        Connection connection = null;

        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query);
                 var rs = ps.executeQuery()) {

                while (rs.next()) {
                    var salePayment = mapResultSet(rs);
                    salePayments.add(salePayment);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all products. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Product find all error. ", e);
        } finally {
            closeConnection(connection);
        }
        return salePayments;
    }

    @Override
    public List<SalePayment> findBySpecification(Specification<SalePayment> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var salePayments = new ArrayList<SalePayment>();
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
                        var product = mapResultSet(rs);
                        salePayments.add(product);
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find sale payment by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Payment find by specification error. ", e);
        } finally {
            closeConnection(connection);
        }
        return salePayments;
    }

    @Override
    public SalePayment update(SalePayment salePayment) {
        var query = qLoader.getQuery("updateSalePayment");
        Connection connection = null;

        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query)) {
                // Ajuste os índices conforme sua query "updateSalePayment"
                ps.setLong(1, salePayment.getSaleId());
                ps.setLong(2, salePayment.getPaymentId());
                ps.setBigDecimal(3, salePayment.getPayment().getValue());
                ps.setInt(4, salePayment.getInstallments());
                ps.setLong(5, salePayment.getId()); // WHERE id = ?

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Failed to update sale payment, no rows affected.");
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update sale payment. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update sale payment", e);
        } finally {
            closeConnection(connection);
        }
        return salePayment;
    }

    @Override
    public boolean remove(Long id) {
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
        var paymentOpt = findPaymentById(rs.getLong("payment_id"));
        var saleOpt = findSaleById(rs.getLong("saleid"));

        if(paymentOpt.isPresent() && saleOpt.isPresent()){
            var salePay = new SalePayment(paymentOpt.get(), saleOpt.get());
            salePay.setId(rs.getLong("id"));
            salePay.setInstallments(rs.getInt("installments"));  // Mapeando o novo campo
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
        if (salePayments == null || salePayments.isEmpty()) return salePayments;

        var query = qLoader.getQuery("insertSalePayment");
        Connection connection = null;

        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                for (SalePayment sp : salePayments) {
                    ps.setLong(1, sp.getSaleId());
                    ps.setLong(2, sp.getPaymentId());
                    ps.setBigDecimal(3, sp.getPayment().getValue()); // Assumindo valor no objeto Payment
                    ps.setInt(4, sp.getInstallments());
                    ps.addBatch();
                }

                ps.executeBatch();

                try (var gKeys = ps.getGeneratedKeys()) {
                    int index = 0;
                    while (gKeys.next() && index < salePayments.size()) {
                        salePayments.get(index++).setId(gKeys.getLong("id"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert SalePayments.", e);
        } finally {
            closeConnection(connection);
        }
        return salePayments;
    }

    @Override
    public int deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;

        var query = qLoader.getQuery("deleteSalePaymentById"); // Verifique o nome correto no seu properties
        Connection connection = null;

        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query)) {
                for (Long id : ids) {
                    ps.setLong(1, id);
                    ps.addBatch();
                }

                int[] results = ps.executeBatch();
                int count = 0;
                for(int r : results) {
                    if(r > 0) count += r;
                    else if(r == Statement.SUCCESS_NO_INFO) count++;
                }
                return count;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch delete SalePayments.", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<SalePayment> updateAll(List<SalePayment> salePayments) {
        if (salePayments == null || salePayments.isEmpty()) return salePayments;

        var query = qLoader.getQuery("updateSalePayment");
        Connection connection = null;

        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query)) {
                for (SalePayment sp : salePayments) {
                    if (sp.getId() <= 0) throw new IllegalArgumentException("Invalid ID for update.");

                    ps.setLong(1, sp.getSaleId());
                    ps.setLong(2, sp.getPaymentId());
                    // Use getAmount() se existir direto em SalePayment, ou getPayment().getValue()
                    ps.setBigDecimal(3, sp.getAmount() != null ? sp.getAmount() : sp.getPayment().getValue());
                    ps.setInt(4, sp.getInstallments());
                    ps.setLong(5, sp.getId());

                    ps.addBatch();
                }

                int[] results = ps.executeBatch();

                // Validação simples do batch
                for (int r : results) {
                    if (r == Statement.EXECUTE_FAILED) throw new SQLException("Batch execution failed.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch update SalePayments.", e);
        } finally {
            closeConnection(connection);
        }
        return salePayments;
    }

}
