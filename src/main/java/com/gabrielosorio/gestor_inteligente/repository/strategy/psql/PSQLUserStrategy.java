package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.Role;
import com.gabrielosorio.gestor_inteligente.model.Permission;
import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalRepositoryStrategyV2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLUserStrategy extends TransactionalRepositoryStrategyV2<User> implements RepositoryStrategy<User> {

    private final QueryLoader qLoader;
    private final Logger log = Logger.getLogger(getClass().getName());

    private void logInfo(String message) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String methodName = stackTraceElement.getMethodName();
        String className = this.getClass().getSimpleName();
        log.info(() -> String.format("[%s#%s] %s", className, methodName, message));
    }

    public PSQLUserStrategy(ConnectionFactory connectionFactory) {
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
    }

    @Override
    public User add(User user) {
        var query = qLoader.getQuery("insertUser");
        Connection connection = null;
        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getFirstName());
                ps.setString(2, user.getLastName());
                ps.setString(3, user.getCellphone());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getCpf());
                ps.setString(6, user.getPassword());
                ps.setLong(7, user.getRole().getId());
                ps.setBoolean(8, user.isActive());
                ps.setTimestamp(9, user.getLastLogin() != null ? Timestamp.valueOf(user.getLastLogin()) : null);
                ps.setTimestamp(10, Timestamp.valueOf(user.getCreatedAt()));
                ps.setTimestamp(11, Timestamp.valueOf(user.getUpdatedAt()));
                ps.setString(12, user.getCreatedBy());
                ps.setString(13, user.getUpdatedBy());

                ps.executeUpdate();

                try (var gKeys = ps.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        user.setId(gKeys.getLong("id"));
                        logInfo("User successfully inserted.");
                    } else {
                        throw new SQLException("Failed to insert user, no key generated.");
                    }
                }

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to insert user. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Failed to insert user", e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }

        return user;
    }

    @Override
    public Optional<User> find(long id) {
        var query = qLoader.getQuery("findUserById");
        Connection connection = null;
        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query)) {
                ps.setLong(1, id);

                try (var rs = ps.executeQuery()) {
                    List<User> users = mapResultSetToUserList(rs);
                    return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find user. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("User search error.", e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<User> findAll() {
        var query = qLoader.getQuery("findAllUsers");
        Connection connection = null;

        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query);
                 var rs = ps.executeQuery()) {
                return mapResultSetToUserList(rs);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find all users. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("User find all error.", e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<User> findBySpecification(Specification<User> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
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
                    return mapResultSetToUserList(rs);
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find user by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("User find by specification error.", e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public User update(User user) {
        var query = qLoader.getQuery("updateUser");
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query)) {

                ps.setString(1, user.getFirstName());
                ps.setString(2, user.getLastName());
                ps.setString(3, user.getCellphone());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getCpf());
                ps.setString(6, user.getPassword());
                ps.setLong(7, user.getRole().getId());
                ps.setBoolean(8, user.isActive());
                ps.setTimestamp(9, user.getLastLogin() != null ? Timestamp.valueOf(user.getLastLogin()) : null);
                ps.setTimestamp(10, Timestamp.valueOf(user.getCreatedAt()));
                ps.setTimestamp(11, Timestamp.valueOf(user.getUpdatedAt()));
                ps.setString(12, user.getCreatedBy());
                ps.setString(13, user.getUpdatedBy());
                ps.setLong(14, user.getId());

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Failed to update user, no rows affected.");
                }

                logInfo("User successfully updated.");
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to update user. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Failed to update user", e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }
        return user;
    }

    @Override
    public boolean remove(long id) {
        var query = qLoader.getQuery("deleteUserById");
        Connection connection = null;
        int affectedRows;

        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query)) {
                ps.setLong(1, id);
                affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    log.warning("No User found with id: " + id);
                    return false;
                }

                logInfo("User with id " + id + " successfully deleted.");
                return true;

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to delete user. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to obtain connection. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to obtain connection", e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Mapeia o ResultSet para uma lista de usuários, agrupando as permissões por usuário
     */
    private List<User> mapResultSetToUserList(ResultSet rs) throws SQLException {
        Map<Long, User> userMap = new LinkedHashMap<>();
        Map<Long, Role> roleMap = new HashMap<>();

        while (rs.next()) {
            Long userId = rs.getLong("id");
            Long roleId = rs.getLong("role_id");

            User user = userMap.get(userId);
            if (user == null) {
                user = mapUserFromResultSet(rs);
                userMap.put(userId, user);
            }

            Role role = roleMap.get(roleId);
            if (role == null) {
                role = mapRoleFromResultSet(rs);
                roleMap.put(roleId, role);
                user.setRole(role);
            }

            long permissionId = rs.getLong("permission_id");
            if (!rs.wasNull() && permissionId > 0) {
                Permission permission = mapPermissionFromResultSet(rs);
                role.addPermission(permission);
            }
        }

        return new ArrayList<>(userMap.values());
    }

    /**
     * Mapeia os dados do usuário do ResultSet
     */
    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        var user = new User();
        user.setId(rs.getLong("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setCellphone(rs.getString("cellphone"));
        user.setEmail(rs.getString("email"));
        user.setCpf(rs.getString("cpf"));
        user.setPassword(rs.getString("password"));
        user.setActive(rs.getBoolean("active"));

        var lastLoginTimestamp = rs.getTimestamp("last_login");
        if (lastLoginTimestamp != null) {
            user.setLastLogin(lastLoginTimestamp.toLocalDateTime());
        }

        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        user.setCreatedBy(rs.getString("created_by"));
        user.setUpdatedBy(rs.getString("updated_by"));

        return user;
    }

    /**
     * Mapeia os dados da role do ResultSet
     */
    private Role mapRoleFromResultSet(ResultSet rs) throws SQLException {
        var role = new Role();
        role.setId(rs.getLong("role_id"));
        role.setName(rs.getString("role_name"));
        role.setDescription(rs.getString("role_description"));
        role.setActive(rs.getBoolean("role_active"));
        role.setCreatedAt(rs.getTimestamp("role_created_at").toLocalDateTime());
        role.setUpdatedAt(rs.getTimestamp("role_updated_at").toLocalDateTime());

        return role;
    }

    /**
     * Mapeia os dados da permissão do ResultSet
     */
    private Permission mapPermissionFromResultSet(ResultSet rs) throws SQLException {
        var permission = new Permission();
        permission.setId(rs.getLong("permission_id"));

        String permissionTypeStr = rs.getString("permission_type");
        if (permissionTypeStr != null) {
            permission.setPermissionType(PermissionType.valueOf(permissionTypeStr));
        }

        permission.setName(rs.getString("permission_name"));
        permission.setDescription(rs.getString("permission_description"));
        permission.setCategory(rs.getString("permission_category"));
        permission.setActive(rs.getBoolean("permission_active"));
        permission.setCreatedAt(rs.getTimestamp("permission_created_at").toLocalDateTime());

        return permission;
    }
}