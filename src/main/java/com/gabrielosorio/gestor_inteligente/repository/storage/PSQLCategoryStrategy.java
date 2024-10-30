package com.gabrielosorio.gestor_inteligente.repository.storage;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.Category;
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

public class PSQLCategoryStrategy implements RepositoryStrategy<Category> {

    private final QueryLoader qLoader;
    private final ConnectionFactory connFactory;
    private Logger log = Logger.getLogger(getClass().getName());

    public PSQLCategoryStrategy(ConnectionFactory connFactory) {
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
        this.connFactory = connFactory;
    }


    @Override
    public Category add(Category category) {
        var query = qLoader.getQuery("insertCategory");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){

            ps.setString(1, category.getDescription());
            ps.executeUpdate();

            try(var gKeys = ps.getGeneratedKeys()){
                if(gKeys.next()){
                    category.setId(gKeys.getLong("id"));
                    log.info("Category successfully inserted.");
                } else {
                    throw new SQLException("Failed to insert category, no key generated.");
                }

            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to insert category. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to insert category", e);

        }
        return category;
    }

    @Override
    public Optional<Category> find(long id) {
        var query = qLoader.getQuery("findCategoryById");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);

            try(var rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find category. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Category search error. ",e);
        }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        var categories = new ArrayList<Category>();
        var query = qLoader.getQuery("findAllCategories");

        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query);
            var rs = ps.executeQuery()){

            while (rs.next()){
                var category = mapResultSet(rs);
                categories.add(category);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find all categories. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Category find all error. ",e);
        }

        return categories;
    }

    @Override
    public List<Category> findBySpecification(Specification<Category> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        var categories = new ArrayList<Category>();

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
                    var category = mapResultSet(rs);
                    categories.add(category);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to find category by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Category find by specification error. ",e);
        }
        return categories;
    }

    @Override
    public Category update(Category category) {
        var query = qLoader.getQuery("updateCategory");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setString(1,category.getDescription());
            ps.setLong(2,category.getId());

            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Failed to update category, no rows affected.");
            }

            log.info("Category successfully updated.");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to update category. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update category", e);
        }

        return category;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteCategoryById");
        try(var connection = connFactory.getConnection();
            var ps = connection.prepareStatement(query)){

            ps.setLong(1,id);
            int affectedRows = ps.executeUpdate();

            if(affectedRows == 0){
                log.warning("No supplier found with id " + id);
                return false;
            } else {
                log.info("Category with id " + id + " successfully deleted.");
                return true;
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to delete supplier. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to delete supplier.",e);
        }

    }

    private Category mapResultSet(ResultSet rs)  throws SQLException {
        return new Category(rs.getLong("id"),rs.getString("description"));
    }
}
