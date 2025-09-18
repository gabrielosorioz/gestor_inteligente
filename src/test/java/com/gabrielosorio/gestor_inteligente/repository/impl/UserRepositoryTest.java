package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.model.Permission;
import com.gabrielosorio.gestor_inteligente.model.Role;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import com.gabrielosorio.gestor_inteligente.repository.base.UserRepository;
import com.gabrielosorio.gestor_inteligente.repository.factory.PSQLRepositoryFactory;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

    private static ConnectionFactory connectionFactory;
    private static PSQLRepositoryFactory repositoryFactory;
    private static UserRepository userRepository;
    private static User testUser;
    private static Role testRole;

    @BeforeAll
    static void setUpClass() {
        // Inicializa a conexão com o banco de teste
        connectionFactory = ConnectionFactory.getInstance(DBScheme.POSTGRESQL_TEST);
        repositoryFactory = new PSQLRepositoryFactory(connectionFactory);
        userRepository = repositoryFactory.getUserRepository();

        // Limpa o banco de dados antes dos testes
        cleanDatabase();

        // Cria dados de teste
        setupTestData();
    }

    @BeforeEach
    void setUp() {
        // Limpa apenas a tabela de usuários antes de cada teste
        cleanUserTable();
    }

    @Test
    @Order(1)
    @DisplayName("Deve inserir um usuário no banco de dados")
    void testAddUser() {
        // Given
        User newUser = createTestUser("João", "Silva", "joao.silva@email.com");

        // When
        User savedUser = userRepository.add(newUser);

        // Then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId()); // UUID não pode ser > 0, apenas não nulo
        assertEquals(newUser.getFirstName(), savedUser.getFirstName());
        assertEquals(newUser.getLastName(), savedUser.getLastName());
        assertEquals(newUser.getEmail(), savedUser.getEmail());
        assertEquals(newUser.getCpf(), savedUser.getCpf());
        assertEquals(newUser.getCellphone(), savedUser.getCellphone());
        assertEquals(newUser.isActive(), savedUser.isActive());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("Deve encontrar um usuário por ID")
    void testFindUserById() {
        // Given
        User newUser = createTestUser("Maria", "Santos", "maria.santos@email.com");
        User savedUser = userRepository.add(newUser);

        // When
        Optional<User> foundUser = userRepository.find(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getFirstName(), foundUser.get().getFirstName());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
        assertNotNull(foundUser.get().getRole());
        assertEquals(testRole.getName(), foundUser.get().getRole().getName());
    }

    @Test
    @Order(3)
    @DisplayName("Deve retornar empty quando usuário não existe")
    void testFindUserByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID(); // Mudado de long para UUID aleatório

        // When
        Optional<User> foundUser = userRepository.find(nonExistentId);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("Deve encontrar um usuário por email")
    void testFindUserByEmail() {
        // Given
        String email = "teste.email@domain.com";
        User newUser = createTestUser("Pedro", "Oliveira", email);
        userRepository.add(newUser);

        // When
        Optional<User> foundUser = userRepository.findUserByEmail(email);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
        assertEquals(newUser.getFirstName(), foundUser.get().getFirstName());
        assertEquals(newUser.getLastName(), foundUser.get().getLastName());
    }

    @Test
    @Order(5)
    @DisplayName("Deve retornar empty quando email não existe")
    void testFindUserByEmailNotFound() {
        // Given
        String nonExistentEmail = "naoexiste@email.com";

        // When
        Optional<User> foundUser = userRepository.findUserByEmail(nonExistentEmail);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(6)
    @DisplayName("Deve buscar todos os usuários")
    void testFindAllUsers() {
        // Given
        User user1 = createTestUser("Ana", "Costa", "ana.costa@email.com");
        User user2 = createTestUser("Carlos", "Ferreira", "carlos.ferreira@email.com");
        User user3 = createTestUser("Lucia", "Mendes", "lucia.mendes@email.com");

        userRepository.add(user1);
        userRepository.add(user2);
        userRepository.add(user3);

        // When
        List<User> allUsers = userRepository.findAll();

        // Then
        assertNotNull(allUsers);
        assertEquals(3, allUsers.size());

        // Verifica se todos os usuários têm role e permissões carregadas
        for (User user : allUsers) {
            assertNotNull(user.getRole());
            assertNotNull(user.getRole().getPermissions());
            assertFalse(user.getRole().getPermissions().isEmpty());
        }
    }

    @Test
    @Order(7)
    @DisplayName("Deve atualizar um usuário existente")
    void testUpdateUser() {
        // Given
        User newUser = createTestUser("Roberto", "Lima", "roberto.lima@email.com");
        User savedUser = userRepository.add(newUser);

        // Modifica os dados
        savedUser.setFirstName("Roberto Carlos");
        savedUser.setLastName("Lima Santos");
        savedUser.setEmail("roberto.carlos@newemail.com");
        savedUser.setCellphone("87654321");
        savedUser.setActive(false);
        savedUser.setUpdatedAt(LocalDateTime.now());
        savedUser.setUpdatedBy("admin_test");

        // When
        User updatedUser = userRepository.update(savedUser);

        // Then
        assertNotNull(updatedUser);
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Roberto Carlos", updatedUser.getFirstName());
        assertEquals("Lima Santos", updatedUser.getLastName());
        assertEquals("roberto.carlos@newemail.com", updatedUser.getEmail());
        assertEquals("87654321", updatedUser.getCellphone());
        assertFalse(updatedUser.isActive());

        // Verifica se foi realmente atualizado no banco
        Optional<User> foundUser = userRepository.find(updatedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Roberto Carlos", foundUser.get().getFirstName());
        assertEquals("roberto.carlos@newemail.com", foundUser.get().getEmail());
    }

    @Test
    @Order(8)
    @DisplayName("Deve remover um usuário por ID")
    void testRemoveUser() {
        // Given
        User newUser = createTestUser("Sofia", "Rodrigues", "sofia.rodrigues@email.com");
        User savedUser = userRepository.add(newUser);
        UUID userId = savedUser.getId(); // Mudado de long para UUID

        // Verifica que o usuário existe
        assertTrue(userRepository.find(userId).isPresent());

        // When
        boolean removed = userRepository.remove(userId);

        // Then
        assertTrue(removed);
        assertFalse(userRepository.find(userId).isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("Deve retornar false ao tentar remover usuário inexistente")
    void testRemoveNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean removed = userRepository.remove(nonExistentId);

        // Then
        assertFalse(removed);
    }

    @Test
    @Order(10)
    @DisplayName("Deve carregar usuário com role e permissões completas")
    void testUserWithRoleAndPermissions() {
        // Given
        User newUser = createTestUser("Admin", "Sistema", "admin@sistema.com");
        User savedUser = userRepository.add(newUser);

        // When
        Optional<User> foundUser = userRepository.find(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        User user = foundUser.get();

        // Verifica Role
        assertNotNull(user.getRole());
        assertEquals(testRole.getName(), user.getRole().getName());
        assertEquals(testRole.getDescription(), user.getRole().getDescription());
        assertTrue(user.getRole().isActive());

        // Verifica Permissões
        List<Permission> permissions = user.getRole().getPermissions();
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());

        // Verifica se tem a permissão CREATE_USER
        boolean hasCreateUserPermission = permissions.stream()
                .anyMatch(p -> p.getPermissionType() == PermissionType.CREATE_USER);
        assertTrue(hasCreateUserPermission);
    }

    @Test
    @Order(11)
    @DisplayName("Deve validar campos obrigatórios do usuário")
    void testUserValidation() {
        // Teste para verificar se os campos obrigatórios são preservados
        User newUser = createTestUser("Validation", "Test", "validation@test.com");

        // Testa com último login
        newUser.setLastLogin(LocalDateTime.now().minusDays(1));

        User savedUser = userRepository.add(newUser);

        assertNotNull(savedUser.getLastLogin());
        assertTrue(savedUser.getLastLogin().isBefore(LocalDateTime.now()));
    }

    @Test
    @Order(12)
    @DisplayName("Deve funcionar com último login nulo")
    void testUserWithNullLastLogin() {
        // Given
        User newUser = createTestUser("Null", "Login", "null.login@test.com");
        newUser.setLastLogin(null);

        // When
        User savedUser = userRepository.add(newUser);

        // Then
        assertNotNull(savedUser);
        Optional<User> foundUser = userRepository.find(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertNull(foundUser.get().getLastLogin());
    }

    // Métodos auxiliares
    private static void setupTestData() {
        testRole = new Role("ADMIN", "Administrador do sistema");
        testRole.setId(1L); // Assumindo que existe uma role com ID 1 no banco de teste

        Permission permission = new Permission();
        permission.setId(1L);
        permission.setPermissionType(PermissionType.CREATE_USER);
        permission.setName("Criar Usuário");
        permission.setDescription("Permissão para criar novos usuários");
        permission.setCategory("USER_MANAGEMENT");
        permission.setActive(true);
        permission.setCreatedAt(LocalDateTime.now());

        testRole.addPermission(permission);
    }

    private User createTestUser(String firstName, String lastName, String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setCpf("123456789" + (int)(Math.random() * 100000));
        user.setCellphone("11987654321");
        user.setPassword("senha123");
        user.setRole(testRole);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy("test_admin");
        user.setUpdatedBy("test_admin");
        return user;
    }

    private static void cleanDatabase() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.prepareStatement("TRUNCATE TABLE users CASCADE").executeUpdate();
            connection.prepareStatement("TRUNCATE TABLE role_permissions RESTART IDENTITY CASCADE").executeUpdate();
            connection.prepareStatement("TRUNCATE TABLE roles RESTART IDENTITY CASCADE").executeUpdate();
            connection.prepareStatement("TRUNCATE TABLE permissions RESTART IDENTITY CASCADE").executeUpdate();
            insertBasicTestData(connection);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar banco de dados para testes", e);
        }
    }

    private static void cleanUserTable() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.prepareStatement("DELETE FROM users").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar tabela users", e);
        }
    }

    private static void insertBasicTestData(Connection connection) throws SQLException {
        // Insere permissão básica
        String insertPermission = "INSERT INTO permissions (id, permission_type, name, description, category, active, created_at) " +
                "VALUES (1, 'CREATE_USER', 'Criar Usuário', 'Permissão para criar novos usuários', 'USER_MANAGEMENT', true, CURRENT_TIMESTAMP)";
        connection.prepareStatement(insertPermission).executeUpdate();

        // Insere role básica
        String insertRole = "INSERT INTO roles (id, name, description, active, created_at, updated_at) " +
                "VALUES (1, 'ADMIN', 'Administrador do sistema', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        connection.prepareStatement(insertRole).executeUpdate();

        // Associa permissão à role
        String insertRolePermission = "INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1)";
        connection.prepareStatement(insertRolePermission).executeUpdate();

    }

    @AfterAll
    static void tearDownClass() {
        // Limpa o banco após todos os testes
        cleanDatabase();
    }
}