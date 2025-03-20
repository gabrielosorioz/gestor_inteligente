package com.gabrielosorio.gestor_inteligente.repository.strategy.base;
import java.sql.Connection;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import java.sql.SQLException;
import java.util.logging.Logger;

public class TransactionManagerV2 {
    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();
    private static final Logger LOGGER = Logger.getLogger(TransactionManagerV2.class.getName());

    public static void beginTransaction() throws SQLException {
        Connection connection = ConnectionFactory.getInstance().getConnection();
        connection.setAutoCommit(false);
        CURRENT_CONNECTION.set(connection);
        LOGGER.info("[BEGIN TRANSACTION] - Conexão iniciada pela classe: " + getCallerClassName());
    }

    public static Connection getCurrentConnection() {
        return CURRENT_CONNECTION.get();
    }

    public static void commit() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            connection.commit();
            LOGGER.info("[COMMIT] - Transação confirmada pela classe: " + getCallerClassName());
            cleanup();
        }
    }

    public static void rollback() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            connection.rollback();
            LOGGER.warning("[ROLLBACK] - Transação revertida pela classe: " + getCallerClassName());
            cleanup();
        }
    }

    private static void cleanup() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            connection.setAutoCommit(true);
            connection.close();
            CURRENT_CONNECTION.remove();
            LOGGER.info("[CLEANUP] - Conexão fechada e removida da ThreadLocal.");
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

    /**
     * Verifica se a conexão fornecida é a conexão transacional compartilhada pelo TransactionManager.
     *
     * @param connection A conexão a ser verificada.
     * @return true se a conexão for a conexão compartilhada; false caso contrário.
     */
    public static boolean isSharedConnection(Connection connection) {
        return connection != null && connection == CURRENT_CONNECTION.get();
    }
}
