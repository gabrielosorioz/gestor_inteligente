package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.ArrayList;
import java.util.List;

public class FindUserByCPF extends AbstractSpecification<User> {
    private final String cpf;

    public FindUserByCPF(String cpf) {
        this.cpf = cpf;
    }

    @Override
    public String toSql() {
        return getQuery("findUserByCPF");
    }

    @Override
    public List<Object> getParameters() {
        List<Object> parameters = new ArrayList<>();
        parameters.add(cpf);
        return parameters;
    }

    @Override
    public boolean isSatisfiedBy(User user) {
        if (user == null || user.getCpf() == null || cpf == null) {
            return false;
        }
        return normalizeCpf(user.getCpf()).equals(normalizeCpf(cpf));
    }

    private String normalizeCpf(String raw) {
        return raw == null ? "" : raw.replaceAll("\\D", "");
    }
}
