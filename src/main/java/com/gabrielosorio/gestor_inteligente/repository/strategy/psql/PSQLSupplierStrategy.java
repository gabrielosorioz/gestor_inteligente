package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Supplier;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLSupplierStrategy implements RepositoryStrategy<Supplier> {

    private final QueryLoader qLoader;
    private final ConnectionFactory connFactory;
    private Logger log = Logger.getLogger(getClass().getName());

    public PSQLSupplierStrategy(ConnectionFactory connFactory){
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.connFactory = connFactory;
    }

    @Override
    public Supplier add(Supplier supplier) {
        var query = qLoader.getQuery("insertSupplier");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,supplier.getId());
            ps.setString(2,supplier.getName());
            ps.setString(3,supplier.getCNPJ().orElse(null));
            ps.setString(4,supplier.getAddress().orElse(null));
            ps.setString(5,supplier.getCellPhone().orElse(null));
            ps.setString(6,supplier.getEmail().orElse(null));
            ps.executeUpdate();

            try(var gKeys = ps.getGeneratedKeys()){
                if(gKeys.next()){
                    log.info("Supplier successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert supplier, no key generated. ");
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert Supplier. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert Supplier", e);
        }
        return supplier;
    }

    @Override
    public Optional<Supplier> find(long id) {
        var query = qLoader.getQuery("findSupplierById");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            try(var rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find Supplier. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Supplier search error. ",e);
        }
        return Optional.empty();
    }

    @Override
    public List<Supplier> findAll() {
        var suppliers = new ArrayList<Supplier>();
        var query = qLoader.getQuery("findAllSuppliers");

        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            while (rs.next()){
                var category = mapResultSet(rs);
                suppliers.add(category);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all suppliers. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Suppliers find all error. ",e);
        }

        return suppliers;
    }

    @Override
    public List<Supplier> findBySpecification(Specification<Supplier> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var suppliers = new ArrayList<Supplier>();

        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            if(params.size() != ps.getParameterMetaData().getParameterCount()){
                throw new SQLException("Mismatch between provided parameters and expected query parameters.");
            }

            for(int i = 0; i < params.size(); i++){
                ps.setObject(i +1, params.get(i));
            }

            try(var rs = ps.executeQuery()){
                while (rs.next()){
                    var supplier = mapResultSet(rs);
                    suppliers.add(supplier);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find supplier by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Supplier find by specification error. ",e);
        }
        return suppliers;
    }

    @Override
    public Supplier update(Supplier supplier) {
        var query = qLoader.getQuery("updateSupplier");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setString(1, supplier.getName());
            ps.setString(2,supplier.getCNPJ().orElse(null));
            ps.setString(3,supplier.getAddress().orElse(null));
            ps.setString(4,supplier.getCellPhone().orElse(null));
            ps.setString(5,supplier.getEmail().orElse(null));
            ps.setLong(6,supplier.getId());

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Failed to update supplier, no rows affected.");
            }

            log.info("Supplier successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update supplier. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update supplier", e);
        }

        return supplier;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteSupplierById");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);
            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                log.warning("No category found with id " + id);
                return false;
            }

            log.info("Category with id " + id + " successfully deleted.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete category. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to delete category.",e);
        }

        return false;
    }

    private Supplier mapResultSet(ResultSet rs)  throws SQLException {
        Supplier s = new Supplier(rs.getLong("id"),rs.getString("name"));
        s.setAddress(Optional.ofNullable(rs.getString("address")));
        s.setCNPJ(Optional.ofNullable(rs.getString("cnpj")));
        s.setCellPhone(Optional.ofNullable(rs.getString("cellphone")));
        s.setEmail(Optional.ofNullable(rs.getString("email")));
        return s;
    }
}
