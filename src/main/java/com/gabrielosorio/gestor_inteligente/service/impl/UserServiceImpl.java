package com.gabrielosorio.gestor_inteligente.service.impl;

import com.gabrielosorio.gestor_inteligente.model.Role;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.dto.UserRegistrationRequest;
import com.gabrielosorio.gestor_inteligente.model.dto.UserRegistrationResult;
import com.gabrielosorio.gestor_inteligente.repository.base.UserRepository;
import com.gabrielosorio.gestor_inteligente.service.base.AuthenticationService;
import com.gabrielosorio.gestor_inteligente.service.base.UserService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    private static final String CPF_REGEX = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^\\d{11}$";
    private static final String PHONE_REGEX = "^\\(?\\d{2}\\)?[\\s-]?9?\\d{4}[\\s-]?\\d{4}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern CPF_PATTERN = Pattern.compile(CPF_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserRegistrationResult registerUser(UserRegistrationRequest request) {
        try {
            // Validar dados de entrada
            UserRegistrationResult validationResult = validateUserData(request);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }

            // Verificar se email já existe
            Optional<User> existingUser = userRepository.findUserByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                return UserRegistrationResult.failure("Email já está em uso por outro usuário.");
            }

            Optional<User> existingUserByCpf = userRepository.findUserByCpf(formatCPF(request.getCpf()));
            if (existingUserByCpf.isPresent()) {
                return UserRegistrationResult.failure("CPF já está cadastrado no sistema.");
            }

            // Criar novo usuário
            User newUser = createUserFromRequest(request);

            // Salvar usuário
            User savedUser = userRepository.add(newUser);

            return UserRegistrationResult.success("Usuário cadastrado com sucesso!", savedUser);

        } catch (Exception e) {
            System.err.println("Erro ao cadastrar usuário: " + e.getMessage());
            e.printStackTrace();
            return UserRegistrationResult.failure("Erro interno do sistema. Tente novamente mais tarde.");
        }
    }

    private UserRegistrationResult validateUserData(UserRegistrationRequest request) {
        // Validar campos obrigatórios
        if (isNullOrEmpty(request.getFirstName())) {
            return UserRegistrationResult.failure("Nome é obrigatório.");
        }

        if (isNullOrEmpty(request.getLastName())) {
            return UserRegistrationResult.failure("Sobrenome é obrigatório.");
        }

        if (isNullOrEmpty(request.getEmail())) {
            return UserRegistrationResult.failure("Email é obrigatório.");
        }

        if (isNullOrEmpty(request.getCpf())) {
            return UserRegistrationResult.failure("CPF é obrigatório.");
        }

        if (isNullOrEmpty(request.getPassword())) {
            return UserRegistrationResult.failure("Senha é obrigatória.");
        }

        if (isNullOrEmpty(request.getConfirmPassword())) {
            return UserRegistrationResult.failure("Confirmação de senha é obrigatória.");
        }

        // Validar formato do email
        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            return UserRegistrationResult.failure("Email deve ter um formato válido.");
        }

        // Validar CPF
        String cleanCpf = request.getCpf().replaceAll("[^\\d]", "");
        if (!CPF_PATTERN.matcher(request.getCpf()).matches() || !isValidCPF(cleanCpf)) {
            return UserRegistrationResult.failure("CPF deve ter um formato válido (xxx.xxx.xxx-xx).");
        }

        // Validar telefone se fornecido
        if (!isNullOrEmpty(request.getCellphone()) && !PHONE_PATTERN.matcher(request.getCellphone()).matches()) {
            return UserRegistrationResult.failure("Telefone deve ter um formato válido.");
        }

        // Validar senha
        if (request.getPassword().length() < 6) {
            return UserRegistrationResult.failure("Senha deve ter pelo menos 6 caracteres.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return UserRegistrationResult.failure("Senha e confirmação de senha devem ser iguais.");
        }

        return UserRegistrationResult.success("Dados válidos", null);
    }

    private User createUserFromRequest(UserRegistrationRequest request) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setCpf(formatCPF(request.getCpf()));
        user.setCellphone(request.getCellphone() != null ? request.getCellphone().trim() : null);
        user.setPassword(AuthenticationService.hashPassword(request.getPassword()));
        user.setRole(createSuperAdminRole());
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy("System");
        user.setUpdatedBy("System");

        return user;
    }

    private Role createSuperAdminRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName("SUPER_ADMIN");
        role.setDescription("Super Administrador com acesso completo ao sistema e todas as permissões");
        return role;
    }

    private String formatCPF(String cpf) {
        String cleanCpf = cpf.replaceAll("[^\\d]", "");
        if (cleanCpf.length() == 11) {
            return cleanCpf.substring(0, 3) + "." +
                    cleanCpf.substring(3, 6) + "." +
                    cleanCpf.substring(6, 9) + "-" +
                    cleanCpf.substring(9, 11);
        }
        return cpf; // Retorna original se não conseguir formatar
    }

    private boolean isValidCPF(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Cálculo do primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (cpf.charAt(i) - '0') * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;

            // Cálculo do segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += (cpf.charAt(i) - '0') * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;

            return (cpf.charAt(9) - '0') == firstDigit && (cpf.charAt(10) - '0') == secondDigit;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

}