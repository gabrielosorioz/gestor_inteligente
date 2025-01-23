package com.gabrielosorio.gestor_inteligente.repository.strategy.base;

import java.sql.Connection;

/**
 * Interface that defines a strategy for managing transactions
 * for specific repository operations.
 *
 * @param <T> the type of entity managed by this strategy.
 */
public interface TransactionalStrategy<T> {

    /**
     * Sets up a transactional connection to be used by the strategy.
     * This method allows the strategy to use a shared connection to perform
     * operations consistently within a transaction context.
     *
     * @param connection the connection to be used for transactions. Must not be {@code null}.
     */
    void openSharedConnection(Connection connection);

    /**
     * Releases the transactional connection currently associated with the strategy.
     * Implementations must ensure that resources are properly released,
     * either by closing or cleaning up references as necessary.
     */
    void closeSharedConnection();
}
