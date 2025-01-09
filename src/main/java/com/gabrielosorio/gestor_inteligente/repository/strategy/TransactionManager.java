package com.gabrielosorio.gestor_inteligente.repository.strategy;

import com.gabrielosorio.gestor_inteligente.exception.TransactionException;
import java.sql.SQLException;

/**
 * Interface responsible for managing transactions in a persistence environment.
 * It provides methods to begin, commit, and rollback transactions, ensuring consistency
 * and control during atomic operations involving multiple strategies.
 */
public interface TransactionManager {

    /**
     * Begins a transaction, setting up the necessary environment to perform
     * subsequent operations atomically.
     *
     * @throws SQLException if an error occurs while configuring the transaction environment.
     */
    void beginTransaction() throws SQLException;

    /**
     * Commits all operations performed during the current transaction.
     * Should only be called after a successful {@link #beginTransaction()} call.
     *
     * @throws TransactionException if an error occurs while attempting to commit the transaction.
     */
    void commitTransaction() throws TransactionException;

    /**
     * Rolls back all operations performed during the current transaction.
     * Should be called in case of errors or inconsistencies to ensure system state integrity.
     *
     * @throws TransactionException if an error occurs while attempting to rollback the transaction.
     */
    void rollbackTransaction() throws TransactionException;
}
