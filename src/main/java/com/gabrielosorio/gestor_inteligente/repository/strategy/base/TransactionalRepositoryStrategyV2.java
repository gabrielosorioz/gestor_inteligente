package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;

public abstract class TransactionalRepositoryStrategyV2<T> implements RepositoryStrategy<T> {

    private static final Logger LOGGER = Logger.getLogger(TransactionalRepositoryStrategyV2.class.getName());

    /**
     * Retorna a conexão atual. Se houver uma conexão transacional ativa,
     * ela é retornada. Caso contrário, é criada uma nova conexão.
     */
    protected Connection getConnection() throws SQLException {
        Connection currentTransactionConnection = TransactionManagerV2.getCurrentConnection();
        if (currentTransactionConnection != null) {
            logInfo("Using transactional connection. " + currentTransactionConnection);
            return currentTransactionConnection;
        }
        var c = ConnectionFactory.getInstance().getConnection();
        logWarning("No active transaction! Using a new connection." + c);
        return c;
    }

    /**
     * Fecha a conexão somente se ela não for a conexão transacional compartilhada.
     */
    protected void closeConnection(Connection connection) {
        if (connection != null) {
            if (!TransactionManagerV2.isTransactionActive()) {
                try {
                    if(!connection.getAutoCommit()){
                        connection.setAutoCommit(true);
                    }
                    connection.close();
                    logInfo("Connection successfully closed.");
                } catch (SQLException e) {
                    logError("Error closing connection", e);
                }
            } else {
                logInfo("Connection is shared, skipping close.");
            }
        }
    }

    /**
     * Método para log de informações com nome da classe e método.
     */
    private void logInfo(String message) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String className = this.getClass().getSimpleName();
        String methodName = stackTraceElement.getMethodName();
        LOGGER.info(String.format("[%s#%s] %s", className, methodName, message));
    }

    /**
     * Método para log de warnings com nome da classe e método.
     */
    private void logWarning(String message) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String className = this.getClass().getSimpleName();
        String methodName = stackTraceElement.getMethodName();
        LOGGER.warning(String.format("[%s#%s] %s", className, methodName, message));
    }

    /**
     * Método para log de erros com nome da classe e método.
     */
    private void logError(String message, Exception e) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String className = this.getClass().getSimpleName();
        String methodName = stackTraceElement.getMethodName();
        LOGGER.log(Level.SEVERE, String.format("[%s#%s] %s", className, methodName, message), e);
    }
}

