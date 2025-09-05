package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import com.gabrielosorio.gestor_inteligente.repository.base.UserRepository;
import com.gabrielosorio.gestor_inteligente.service.base.AuthenticationService;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private User currentUser;

    public AuthenticationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(String email, String password) {
        try {
            Optional<User> userOptional = userRepository.findUserByEmail(email);

            if (userOptional.isEmpty()) {
                return Optional.empty();
            }

            User user = userOptional.get();

            if (!user.isActive()) {
                return Optional.empty();
            }

            if (!BCrypt.checkpw(password, user.getPassword())) {
                return Optional.empty();
            }

            user.setLastLogin(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.update(user);
            this.currentUser = user;

            return Optional.of(user);

        } catch (Exception e) {
            System.err.println("Erro durante autenticação: " + e.getMessage());
            return Optional.empty();
        }
    }


    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean hasPermission(PermissionType permissionType) {
        return currentUser != null && currentUser.hasPermission(permissionType);
    }

    public boolean changePassword(long userId, String currentPassword, String newPassword) {
        try {
            Optional<User> userOptional = userRepository.find(userId);

            if (userOptional.isEmpty()) {
                return false;
            }

            User user = userOptional.get();

            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                return false;
            }

            user.setPassword(AuthenticationService.hashPassword(newPassword));
            user.setUpdatedAt(LocalDateTime.now());

            if (currentUser != null) {
                user.setUpdatedBy(currentUser.getEmail());
            }

            userRepository.update(user);
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao alterar senha: " + e.getMessage());
            return false;
        }
    }

    public boolean resetPassword(long userId, String newPassword) {
        try {
            if (!hasPermission(PermissionType.RESET_PASSWORD)) {
                return false;
            }

            Optional<User> userOptional = userRepository.find(userId);

            if (userOptional.isEmpty()) {
                return false;
            }

            User user = userOptional.get();

            // Define a nova senha (hash)
            user.setPassword(AuthenticationService.hashPassword(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy(currentUser.getEmail());

            userRepository.update(user);
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao redefinir senha: " + e.getMessage());
            return false;
        }
    }
}