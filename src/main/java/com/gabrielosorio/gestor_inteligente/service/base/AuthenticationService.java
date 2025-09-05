package com.gabrielosorio.gestor_inteligente.service.base;

import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public interface AuthenticationService {

    /**
     * Autentica um usuário com email e senha
     * @param email Email do usuário
     * @param password Senha em texto plano
     * @return Optional contendo o usuário autenticado ou empty se inválido
     */
    Optional<User> authenticate(String email, String password);

    /**
     * Obtém o usuário atualmente logado
     * @return Usuário atual ou null se ninguém estiver logado
     */
    User getCurrentUser();

    /**
     * Verifica se há um usuário logado
     * @return true se há usuário logado
     */
    boolean isUserLoggedIn();

    /**
     * Faz logout do usuário atual
     */
    void logout();

    /**
     * Verifica se o usuário atual tem uma permissão específica
     * @param permissionType Tipo da permissão a verificar
     * @return true se o usuário tem a permissão
     */
    boolean hasPermission(PermissionType permissionType);

    /**
     * Altera a senha de um usuário
     * @param userId ID do usuário
     * @param currentPassword Senha atual
     * @param newPassword Nova senha
     * @return true se a senha foi alterada com sucesso
     */
    boolean changePassword(long userId, String currentPassword, String newPassword);

    /**
     * Redefine a senha de um usuário (apenas para administradores)
     * @param userId ID do usuário
     * @param newPassword Nova senha
     * @return true se a senha foi redefinida com sucesso
     */
    boolean resetPassword(long userId, String newPassword);

    static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * Gera hash BCrypt para uma senha
     * @param password Senha em texto plano
     * @return Hash BCrypt da senha
     */

    static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }


}
