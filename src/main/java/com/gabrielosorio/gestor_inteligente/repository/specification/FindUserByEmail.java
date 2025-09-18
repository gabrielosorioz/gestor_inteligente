package com.gabrielosorio.gestor_inteligente.repository.specification;

import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.AbstractSpecification;

import java.util.ArrayList;
import java.util.List;

public class FindUserByEmail extends AbstractSpecification<User> {
    private final String email;

    public FindUserByEmail(String email) {
        this.email = email;
    }

    @Override
    public String toSql() {
        return getQuery("findUserByEmail");
    }

    @Override
    public List<Object> getParameters() {
        List<Object> parameters = new ArrayList<>();
        parameters.add(email);
        return parameters;
    }

    @Override
    public boolean isSatisfiedBy(User user) {
        return user.getEmail() != null && user.getEmail().equalsIgnoreCase(email);
    }
}