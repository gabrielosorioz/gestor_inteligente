package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalStrategy;

/**
 * Abstract base class for transactional services that provides
 * access to a repository and its transactional strategy.
 *
 * @param <T> the type of entity managed by the service.
 */
public abstract class AbstractTransactionalService<T> {

    private final Repository<T> repository;

    /**
     * Constructor that initializes the repository for this service.
     *
     * @param repository the repository associated with this service.
     */
    protected AbstractTransactionalService(Repository<T> repository) {
        this.repository = repository;
    }

    /**
     * Retrieves the repository used by this service.
     *
     * @return the repository instance.
     */
    protected Repository<T> getRepository() {
        return repository;
    }

    /**
     * Retrieves the transactional strategy from the repository.
     *
     * @return the transactional strategy for managing transactions.
     * @throws UnsupportedOperationException if the repository does not support transactional operations.
     */
    public TransactionalStrategy<T> getTransactionalStrategy() {
        return repository.getTransactionalStrategy();
    }
}

