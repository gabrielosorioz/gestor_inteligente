package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.enums.SaleStatus;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalRepositoryStrategyV2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLSaleStrategy extends TransactionalRepositoryStrategyV2<Sale,Long> implements RepositoryStrategy<Sale,Long> {

    private final QueryLoader qLoader;
    private final  Logger log = Logger.getLogger(getClass().getName());

    private void logInfo(String message) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String methodName = stackTraceElement.getMethodName();
        String className = this.getClass().getSimpleName(); // Pega apenas o nome da classe
        log.info(() -> String.format("[%s#%s] %s", className, methodName, message));
    }

    public PSQLSaleStrategy(ConnectionFactory connectionFactory){
        super(connectionFactory);
        this.qLoader = new QueryLoader(connectionFactory.getDBScheme());
    }

    @Override
    public Sale add(Sale sale) {
        var query = qLoader.getQuery("insertSale");
        Connection connection = null;
        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setTimestamp(1, sale.getDateSale());
                ps.setTimestamp(2, sale.getDataCancel());
                ps.setBigDecimal(3, sale.getTotalChange());
                ps.setBigDecimal(4, sale.getTotalAmountPaid());
                ps.setBigDecimal(5, sale.getOriginalTotalPrice());
                ps.setBigDecimal(6, sale.getTotalPrice());
                ps.setBigDecimal(7, sale.getTotalDiscount());
                ps.setBigDecimal(8, sale.getItemsDiscount());
                ps.setBigDecimal(9, sale.getSaleDiscount());
                ps.setString(10, sale.getStatus().name());

                ps.executeUpdate();

                try (var gKeys = ps.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        sale.setId(gKeys.getLong("id"));
                        logInfo("Sale successfully inserted.");
                    } else {
                        throw new SQLException("Failed to insert sale, no key generated.");
                    }
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                log.log(Level.SEVERE, "Failed to insert sale. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Failed to insert sale", e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }

        return sale;
    }

    @Override
    public Optional<Sale> find(Long id) {
        var query = qLoader.getQuery("findSaleById");
        Connection connection = null;
        try {
            connection = getConnection();

            try(var ps = connection.prepareStatement(query)){
                ps.setLong(1,id);

                try(var rs = ps.executeQuery()){
                    if(rs.next()){
                        return Optional.of(mapResultSet(rs));
                    }
                }
            } catch (SQLException e){
                log.log(Level.SEVERE, "Failed to find sale product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Sale Product search error. ",e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }

        return Optional.empty();
    }

    @Override
    public List<Sale> findAll() {
        var sales = new ArrayList<Sale>();
        var query = qLoader.getQuery("findAllSales");
        Connection connection = null;

        try{
            connection = getConnection();

            try(var ps = connection.prepareStatement(query);
                var rs = ps.executeQuery()) {
                while(rs.next()){
                    var sale = mapResultSet(rs);
                    sales.add(sale);
                }
            } catch (SQLException e){
                log.log(Level.SEVERE, "Failed to find all sales. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Sale find all error. ",e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }

        return sales;
    }

    @Override
    public List<Sale> findBySpecification(Specification<Sale> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var sales = new ArrayList<Sale>();
        Connection connection = null;
        try{
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
                        sales.add(product);
                    }
                }
            } catch (SQLException e){
                log.log(Level.SEVERE, "Failed to find sale by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Sale find by specification error. ",e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }

        return sales;
    }

    @Override
    public Sale update(Sale sale) {
        var query = qLoader.getQuery("updateSale");
        Connection connection = null;

        try{
            connection = getConnection();
            try(var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

                ps.setTimestamp(1,sale.getDateSale());
                ps.setTimestamp(2,sale.getDataCancel());
                ps.setBigDecimal(3,sale.getTotalChange());
                ps.setBigDecimal(4,sale.getTotalAmountPaid());
                ps.setBigDecimal(5,sale.getOriginalTotalPrice());
                ps.setBigDecimal(6,sale.getTotalPrice());
                ps.setBigDecimal(7,sale.getTotalDiscount());
                ps.setBigDecimal(8, sale.getItemsDiscount());
                ps.setBigDecimal(9, sale.getSaleDiscount());
                ps.setString(10,sale.getStatus().name());
                ps.setLong(11,sale.getId());
                int affectedRows = ps.executeUpdate();

                if(affectedRows == 0){
                    throw new SQLException("Failed to update product, no rows affected.");
                }

                logInfo("Sale successfully updated.");
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to update sale. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Failed to update sale", e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }
        return sale;
    }

    @Override
    public boolean remove(Long id) {
        var query = qLoader.getQuery("deleteSaleById");
        Connection connection = null;
        int affectedRows;

        try{
            connection = getConnection();

            try (var ps = connection.prepareStatement(query)) {
                ps.setLong(1, id);
                affectedRows = ps.executeUpdate();

                if(affectedRows == 0){
                    log.warning("No Sale found with id: " + id);
                    return false;
                }

                logInfo("Sale with id " + id + " successfully deleted.");
                return true;

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to delete sale. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }
    }

    private Sale mapResultSet(ResultSet rs)  throws SQLException {
        var s = new Sale();
        s.setId(rs.getLong("id"));
        s.setDateSale(rs.getTimestamp("datesale"));
        s.setDataCancel(rs.getTimestamp("dataCancel"));
//        s.setTotalChange(rs.getBigDecimal("totalchange")); // Note:
        s.setTotalAmountPaid(rs.getBigDecimal("totalamountpaid"));
        s.setOriginalTotalPrice(rs.getBigDecimal("originaltotalprice"));
        s.setTotalPrice(rs.getBigDecimal("totalprice"));
        s.setTotalDiscount(rs.getBigDecimal("totaldiscount"));
        s.setItemsDiscount(rs.getBigDecimal("itemsdiscount"));
        s.setSaleDiscount(rs.getBigDecimal("salediscount"));
        s.setStatus(SaleStatus.valueOf(rs.getString("status")));
        return s;
    }


}
