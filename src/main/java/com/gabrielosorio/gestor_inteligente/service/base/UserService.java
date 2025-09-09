package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.dto.UserRegistrationRequest;
import com.gabrielosorio.gestor_inteligente.model.dto.UserRegistrationResult;

public interface UserService {
    UserRegistrationResult registerUser(UserRegistrationRequest request);
}
