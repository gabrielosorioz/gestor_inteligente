package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class TransactionalRepositoryStrategyV2<T> implements RepositoryStrategy<T> {
    protected Connection getConnection() throws SQLException {
        Connection connection = TransactionManagerV2.getCurrentConnection();
        if (connection != null) {
            return connection; // Usar conexão transacional
        }
        return ConnectionFactory.getInstance().getConnection(); // Conexão normal
    }
}
