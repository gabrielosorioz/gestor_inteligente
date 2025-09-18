package com.gabrielosorio.gestor_inteligente.repository.impl;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.repository.base.Repository;
import com.gabrielosorio.gestor_inteligente.repository.base.UserRepository;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindUserByCPF;
import com.gabrielosorio.gestor_inteligente.repository.specification.FindUserByEmail;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PSQLUserRepository extends Repository<User, UUID> implements UserRepository {

    public PSQLUserRepository(RepositoryStrategy<User,UUID> strategy) {
        init(strategy);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        List<User> users = strategy.findBySpecification(new FindUserByEmail(email));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public Optional<User> findUserByCpf(String cpf) {
        List<User> users = strategy.findBySpecification(new FindUserByCPF(cpf));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
}