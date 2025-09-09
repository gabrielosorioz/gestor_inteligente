package com.gabrielosorio.gestor_inteligente.model.dto;
import com.gabrielosorio.gestor_inteligente.model.User;

public class UserRegistrationResult {
    private final boolean success;
    private final String message;
    private final User user;

    private UserRegistrationResult(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public static UserRegistrationResult success(String message, User user) {
        return new UserRegistrationResult(true, message, user);
    }

    public static UserRegistrationResult failure(String message) {
        return new UserRegistrationResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public User getUser() { return user; }
}
