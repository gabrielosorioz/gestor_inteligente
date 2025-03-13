package com.gabrielosorio.gestor_inteligente.repository.strategy.base;
import java.sql.Connection;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;

import java.sql.SQLException;

public class TransactionManagerV2 {
    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

    public static void beginTransaction() throws SQLException {
        Connection connection = ConnectionFactory.getInstance().getConnection();
        connection.setAutoCommit(false);
        CURRENT_CONNECTION.set(connection);
    }

    public static Connection getCurrentConnection() {
        return CURRENT_CONNECTION.get();
    }

    public static void commit() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            connection.commit();
            cleanup();
        }
    }

    public static void rollback() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            connection.rollback();
            cleanup();
        }
    }

    private static void cleanup() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            connection.setAutoCommit(true);
            connection.close();
            CURRENT_CONNECTION.remove();
        }
    }
}
