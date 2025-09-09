package com.gabrielosorio.gestor_inteligente.repository.base;

import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends RepositoryStrategy<User, UUID> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByCpf(String cpf);
}
