package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;

public abstract class TransactionalRepositoryStrategyV2<T> implements RepositoryStrategy<T> {

    protected Connection connection;
    private static final Logger LOGGER = Logger.getLogger(TransactionalRepositoryStrategyV2.class.getName());

    /**
     * Construtor que inicializa a conexão corretamente.
     */
    public TransactionalRepositoryStrategyV2() {
        try {
            this.connection = getConnection();
            LOGGER.info("Connection successfully initialized.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error obtaining connection", e);
            throw new RuntimeException("Error obtaining connection", e);
        }
    }

    /**
     * Retorna a conexão atual. Se houver uma conexão transacional ativa,
     * ela é retornada. Caso contrário, é criada uma nova conexão.
     */
    protected Connection getConnection() throws SQLException {
        Connection currentTransactionConnection = TransactionManagerV2.getCurrentConnection();
        if (currentTransactionConnection != null) {
            LOGGER.info("Using transactional connection.");
            return currentTransactionConnection;
        }
        LOGGER.info("Using a new connection from ConnectionFactory.");
        return ConnectionFactory.getInstance().getConnection();
    }

    /**
     * Fecha a conexão somente se ela não for a conexão transacional compartilhada.
     */
    protected void closeConnection() {
        if (connection != null) {
            if (!TransactionManagerV2.isSharedConnection(connection)) {
                try {
                    connection.close();
                    LOGGER.info("Connection successfully closed.");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", e);
                }
            } else {
                LOGGER.info("Connection is shared, skipping close.");
            }
        }
    }
}
