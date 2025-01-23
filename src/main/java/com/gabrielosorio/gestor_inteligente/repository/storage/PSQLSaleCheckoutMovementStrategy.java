package com.gabrielosorio.gestor_inteligente.repository.storage;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.CheckoutMovement;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.SaleCheckoutMovement;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
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

public class PSQLSaleCheckoutMovementStrategy extends TransactionalRepositoryStrategy<SaleCheckoutMovement> implements RepositoryStrategy<SaleCheckoutMovement>, BatchInsertable<SaleCheckoutMovement> {

    private final QueryLoader qLoader;
    private final PSQLSaleStrategy saleStrategy;
    private final PSQLCheckoutMovementStrategy checkoutMovementStrategy;
    private final Logger log = Logger.getLogger(getClass().getName());

    public PSQLSaleCheckoutMovementStrategy(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.checkoutMovementStrategy = new PSQLCheckoutMovementStrategy(connectionFactory);
        this.saleStrategy = new PSQLSaleStrategy(connectionFactory);
    }

    private Optional<CheckoutMovement> findCheckoutMovementById(long id){
        return checkoutMovementStrategy.find(id);
    }

    private Optional<Sale> findSaleById(long id){
        return saleStrategy.find(id);
    }

    private SaleCheckoutMovement mapResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long saleId = rs.getLong("sale_id");
        long checkoutMovementId = rs.getLong("checkout_movement_id");
        var checkoutMovement = findCheckoutMovementById(checkoutMovementId).get();
        var sale = findSaleById(saleId).get();
        var saleCheckoutMovement = new SaleCheckoutMovement(id,checkoutMovement,sale);
        return saleCheckoutMovement;
    }

    @Override
    public SaleCheckoutMovement add(SaleCheckoutMovement saleCheckoutMovement) {
        var query = qLoader.getQuery("insertSaleCheckoutMovement");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

            ps.setLong(1,saleCheckoutMovement.getCheckoutMovement().getId());
            ps.setLong(2,saleCheckoutMovement.getSale().getId());
            ps.executeUpdate();

            try(var gKeys = ps.getGeneratedKeys()){
                if(gKeys.next()){
                    saleCheckoutMovement.setId(gKeys.getLong("id"));
                    log.info("SaleCheckoutMovement successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert sale checkout movement, no key generated.");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert sale checkout movement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert sale checkout movement", e);

        }
        return saleCheckoutMovement;
    }

    @Override
    public Optional<SaleCheckoutMovement> find(long id) {
        var query = qLoader.getQuery("findSaleCheckoutMovementById");

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            try(var rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find Sale Checkout. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Checkout search error. ",e);

        }

        return Optional.empty();
    }

    @Override
    public List<SaleCheckoutMovement> findAll() {
        var saleCheckoutMovements = new ArrayList<SaleCheckoutMovement>();
        var query = qLoader.getQuery("findAllSaleCheckoutMovements");

        try(var connection = getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            while(rs.next()){
                var saleCheckoutMovement = mapResultSet(rs);
                saleCheckoutMovements.add(saleCheckoutMovement);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all sale checkout movements. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Checkout Movements find all error. ",e);

        }
        return saleCheckoutMovements;
    }

    @Override
    public List<SaleCheckoutMovement> findBySpecification(Specification<SaleCheckoutMovement> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var saleCheckoutMovements = new ArrayList<SaleCheckoutMovement>();

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
                    var saleCheckoutMovement = mapResultSet(rs);
                    saleCheckoutMovements.add(saleCheckoutMovement);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find sale checkout movement by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Checkout Movement find by specification error. ",e);
        }
        return saleCheckoutMovements;
    }

    @Override
    public SaleCheckoutMovement update(SaleCheckoutMovement saleCheckoutMovement) {
        var query = qLoader.getQuery("updateSaleCheckoutMovement");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1, saleCheckoutMovement.getCheckoutMovement().getId());
            ps.setLong(2, saleCheckoutMovement.getSale().getId());
            ps.setLong(3, saleCheckoutMovement.getId());

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Failed to update sale checkout movement, no rows affected.");
            }

            log.info("Sale Checkout movement successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update sale checkout movement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update sale checkout movement", e);
        }

        return saleCheckoutMovement;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteSaleCheckoutMovementById");
        try(var connection = getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                log.warning("No Sale Checkout Movement found with id: " + id);
                return false;
            }

            log.info("Sale Checkout Movement with id " + id + " successfully deleted.");
            return true;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete Sale Checkout Movement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SaleCheckoutMovement> addAll(List<SaleCheckoutMovement> saleCheckoutMovements) {
        var query = qLoader.getQuery("insertSaleCheckoutMovement");
        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            try(var ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){

                for(SaleCheckoutMovement saleCheckoutMovement: saleCheckoutMovements){
                    ps.setLong(1,saleCheckoutMovement.getCheckoutMovement().getId());
                    ps.setLong(2,saleCheckoutMovement.getSale().getId());
                    ps.addBatch();
                }

                ps.executeBatch();
                connection.commit();

                try(var gKeys = ps.getGeneratedKeys()){
                    int index = 0;
                    while(gKeys.next()){
                        saleCheckoutMovements.get(index++).setId(gKeys.getLong("id"));
                    }
                }

                log.info("All SaleCheckoutMovements successfully inserted.");
            } catch (SQLException e){
                if(connection != null){
                    try {
                        connection.rollback();
                        log.warning("Transaction rolled back due to an error: " + e.getMessage());
                    } catch (SQLException rollBackEx) {
                        log.severe("RollBack failed " + rollBackEx.getMessage());
                    }
                }
                throw new RuntimeException("Failed to batch insert SaleCheckoutMovements. " + e.getMessage(),e);
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
        return saleCheckoutMovements;
    }
}
