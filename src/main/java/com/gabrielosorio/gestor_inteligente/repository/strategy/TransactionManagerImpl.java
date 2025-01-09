package com.gabrielosorio.gestor_inteligente.repository.strategy;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionManagerImpl implements TransactionManager {

    private static final Logger LOGGER = Logger.getLogger(TransactionManagerImpl.class.getName());

    private final List<TransactionalStrategy<?>> transactionalStrategies;
    private Connection sharedConnection;

    public TransactionManagerImpl(List<TransactionalStrategy<?>> transactionalStrategies) {
        this.transactionalStrategies = transactionalStrategies;
    }

    @Override
    public void beginTransaction() throws TransactionException {
        if (sharedConnection != null) {
            throw new TransactionException("A transaction is already in progress.");
        }

        try {
            // Obter uma conexão compartilhada e desativar auto-commit
            sharedConnection = ConnectionFactory.getInstance().getConnection();
            if (sharedConnection.isClosed()) {
                throw new TransactionException("Connection is closed.");
            }
            sharedConnection.setAutoCommit(false);

            // Configurar todas as estratégias para usar a conexão compartilhada
            transactionalStrategies.forEach(strategy -> strategy.openSharedConnection(sharedConnection));

            LOGGER.info("Transaction started successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to start transaction.", e);
            throw new TransactionException("Failed to start transaction.", e);
        }
    }

    @Override
    public void commitTransaction() throws TransactionException {
        if (sharedConnection == null) {
            throw new TransactionException("No transaction in progress to commit.");
        }

        try {
            sharedConnection.commit();
            LOGGER.info("Transaction committed successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to commit transaction.", e);
            throw new TransactionException("Failed to commit transaction.", e);
        } finally {
            closeSharedConnection();
        }
    }

    @Override
    public void rollbackTransaction() throws TransactionException {
        if (sharedConnection == null) {
            throw new TransactionException("No transaction in progress to rollback.");
        }

        try {
            sharedConnection.rollback();
            LOGGER.info("Transaction rolled back successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to rollback transaction.", e);
            throw new TransactionException("Failed to rollback transaction.", e);
        } finally {
            closeSharedConnection();
        }
    }

    private void closeSharedConnection() {
        transactionalStrategies.forEach(strategy -> {
            try {
                strategy.closeSharedConnection();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to close connection for strategy: " + strategy.getClass().getName(), e);
            }
        });

        try {
            if (sharedConnection != null) {
                sharedConnection.setAutoCommit(true);
                sharedConnection.close();
                LOGGER.info("Shared connection closed successfully.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to close shared connection.", e);
            throw new TransactionException("Failed to close shared connection.", e);
        } finally {
            sharedConnection = null;
        }
    }
}
