package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;

public class TransactionManagerV2 {
    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();
    private static final ThreadLocal<ConnectionFactory> CURRENT_CONNECTION_FACTORY = new ThreadLocal<>();
    private static final Logger LOGGER = Logger.getLogger(TransactionManagerV2.class.getName());

    /**
     * Inicia uma transação usando a ConnectionFactory fornecida
     */
    public static void beginTransaction(ConnectionFactory connectionFactory) throws SQLException {
        if (CURRENT_CONNECTION.get() != null) {
            throw new IllegalStateException("Uma transação já está em andamento nesta thread.");
        }

        Connection connection = connectionFactory.getConnection();
        connection.setAutoCommit(false);
        CURRENT_CONNECTION.set(connection);
        CURRENT_CONNECTION_FACTORY.set(connectionFactory);
        LOGGER.info("[BEGIN TRANSACTION] - Conexão iniciada pela classe: " + getCallerClassName());
    }

    public static Connection getCurrentConnection() {
        return CURRENT_CONNECTION.get();
    }

    public static ConnectionFactory getCurrentConnectionFactory() {
        return CURRENT_CONNECTION_FACTORY.get();
    }

    public static void commit() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection == null) {
            throw new IllegalStateException("Nenhuma transação ativa para commit.");
        }

        connection.commit();
        LOGGER.info("[COMMIT] - Transação confirmada pela classe: " + getCallerClassName());
        cleanup();
    }

    public static void rollback() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection == null) {
            throw new IllegalStateException("Nenhuma transação ativa para rollback.");
        }

        connection.rollback();
        LOGGER.warning("[ROLLBACK] - Transação revertida pela classe: " + getCallerClassName());
        cleanup();
    }

    private static void cleanup() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
                LOGGER.info("[CLEANUP] - Conexão fechada e removida da ThreadLocal.");
            } finally {
                CURRENT_CONNECTION.remove();
                CURRENT_CONNECTION_FACTORY.remove();
            }
        }
    }

    private static String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (!element.getClassName().equals(TransactionManagerV2.class.getName()) &&
                    !element.getClassName().startsWith("java.")) {
                return element.getClassName() + "#" + element.getMethodName();
            }
        }
        return "Unknown";
    }

    public static boolean isTransactionActive() {
        return CURRENT_CONNECTION.get() != null;
    }

    // Método de compatibilidade para código legado que não passa ConnectionFactory
    @Deprecated
    public static void beginTransaction() throws SQLException {
        throw new UnsupportedOperationException(
                "beginTransaction() without ConnectionFactory is no longer supported. " +
                        "Use beginTransaction(ConnectionFactory connectionFactory) instead."
        );
    }
}