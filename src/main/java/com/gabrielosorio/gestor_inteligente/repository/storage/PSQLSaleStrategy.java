package com.gabrielosorio.gestor_inteligente.repository.storage;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Sale;
import com.gabrielosorio.gestor_inteligente.model.enums.SaleStatus;
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

public class PSQLSaleStrategy implements RepositoryStrategy<Sale> {

    private final QueryLoader qLoader;
    private final ConnectionFactory connFactory;
    private Logger log = Logger.getLogger(getClass().getName());

    public PSQLSaleStrategy(ConnectionFactory connectionFactory){
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.connFactory = connectionFactory;
    }

    @Override
    public Sale add(Sale sale) {
        var query = qLoader.getQuery("insertSale");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

            ps.setTimestamp(1, sale.getDateSale());
            ps.setTimestamp(2, sale.getDataCancel());
            ps.setBigDecimal(3, sale.getTotalChange());
            ps.setBigDecimal(4, sale.getTotalAmountPaid());
            ps.setBigDecimal(5, sale.getOriginalTotalPrice());
            ps.setBigDecimal(6, sale.getTotalPrice());
            ps.setBigDecimal(7, sale.getTotalDiscount());
            ps.setString(8, sale.getStatus().name());

            ps.executeUpdate();

            try(var gKeys = ps.getGeneratedKeys()){
                if(gKeys.next()){
                    sale.setId(gKeys.getLong("id"));
                    log.info("Sale successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert sale, no key generated.");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert sale. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert sale", e);

        }
        return sale;
    }

    @Override
    public Optional<Sale> find(long id) {
        var query = qLoader.getQuery("findSaleById");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            try(var rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find sale product. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale Product search error. ",e);
        }

        return Optional.empty();
    }

    @Override
    public List<Sale> findAll() {
        var sales = new ArrayList<Sale>();
        var query = qLoader.getQuery("findAllSales");

        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            while(rs.next()){
                var sale = mapResultSet(rs);
                sales.add(sale);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all sales. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale find all error. ",e);

        }

        return sales;
    }

    @Override
    public List<Sale> findBySpecification(Specification<Sale> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var sales = new ArrayList<Sale>();

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
                    var product = mapResultSet(rs);
                    sales.add(product);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find sale by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Sale find by specification error. ",e);
        }
        return sales;
    }

    @Override
    public Sale update(Sale sale) {
        var query = qLoader.getQuery("updateSale");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

            ps.setTimestamp(1,sale.getDateSale());
            ps.setTimestamp(2,sale.getDataCancel());
            ps.setBigDecimal(3,sale.getTotalChange());
            ps.setBigDecimal(4,sale.getTotalAmountPaid());
            ps.setBigDecimal(5,sale.getOriginalTotalPrice());
            ps.setBigDecimal(6,sale.getTotalPrice());
            ps.setBigDecimal(7,sale.getTotalDiscount());
            ps.setString(8,sale.getStatus().name());
            ps.setLong(9,sale.getId());

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Failed to update product, no rows affected.");
            }

            log.info("Sale successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update sale. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update sale", e);
        }
        return sale;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteSaleById");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                log.warning("No Sale found with id: " + id);
                return false;
            }

            log.info("Sale with id " + id + " successfully deleted.");
            return true;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete sale. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException(e);
        }
    }

    private Sale mapResultSet(ResultSet rs)  throws SQLException {
        var s = new Sale();
        s.setId(rs.getLong("id"));
        s.setDateSale(rs.getTimestamp("datesale"));
        s.setDataCancel(rs.getTimestamp("dataCancel"));
        // totalChange
        s.setTotalAmountPaid(rs.getBigDecimal("totalamountpaid"));
        s.setOriginalTotalPrice(rs.getBigDecimal("originalTotalPrice"));
        s.setTotalPrice(rs.getBigDecimal("totalPrice"));
        s.setTotalDiscount(rs.getBigDecimal("totaldiscount"));
        s.setStatus(SaleStatus.valueOf(rs.getString("status")));
        return s;
    }
}
