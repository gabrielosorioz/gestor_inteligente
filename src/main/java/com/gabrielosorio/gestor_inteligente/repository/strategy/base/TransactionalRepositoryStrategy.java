package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class to manage the shared behavior for Transactional Strategies.
 * Handles the `sharedConnection` attribute and provides utility methods
 * to obtain the appropriate connection.
 *
 * @param <T> the type of entity managed by this strategy.
 */
public abstract class TransactionalRepositoryStrategy<T> implements TransactionalStrategy<T> {

    private final ConnectionFactory connectionFactory;
    private final Logger log = Logger.getLogger(getClass().getName());
    private Connection sharedConnection;
    private boolean isSharedConnection;

    protected TransactionalRepositoryStrategy(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Retrieves the appropriate connection to be used by the strategy.
     * If a transactional connection is available, it is used; otherwise, a new
     * connection is obtained from the connection pool.
     *
     * @return the connection to be used.
     * @throws SQLException if an error occurs while obtaining a connection.
     */
    protected Connection getConnection() throws SQLException {
        if (sharedConnection != null) {
            log.info("Using shared transactional connection.");
            return sharedConnection;
        } else {
            log.info("Using a new connection from the connection pool.");
            return connectionFactory.getConnection();
        }
    }

    /**
     * Checks if the connection is shared.
     *
     * @return true if the connection is shared, false otherwise.
     */
    protected boolean isSharedConnection() {
        return isSharedConnection;
    }

    @Override
    public void openSharedConnection(Connection connection) {
        if (sharedConnection != null) {
            throw new IllegalStateException("Shared transactional connection is already set.");
        }
        this.sharedConnection = connection;
        this.isSharedConnection = true;
        log.info("Shared transactional connection was established.");
    }

    @Override
    public void closeSharedConnection() {
        if (this.sharedConnection != null) {
            try {
                if (!this.sharedConnection.isClosed()) {
                    if (!isSharedConnection) {
                        this.sharedConnection.close();
                    }
                }
            } catch (SQLException e) {
                throw new TransactionException("Failed to close the shared connection.", e);
            } finally {
                if (isSharedConnection) {
                    this.sharedConnection = null;
                    this.isSharedConnection = false;
                }
            }
        }
    }

    protected void closeConnection(Connection connection){
        if (connection != null && !isSharedConnection()) {
            try {
                if(!connection.isClosed()){
                    connection.close();
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, "Failed to close the connection.", e);
            }
        }
    }

}


