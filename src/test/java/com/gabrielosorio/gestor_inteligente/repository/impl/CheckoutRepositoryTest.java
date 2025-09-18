package com.gabrielosorio.gestor_inteligente.repository.impl;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.model.Checkout;
import com.gabrielosorio.gestor_inteligente.model.Permission;
import com.gabrielosorio.gestor_inteligente.model.Role;
import com.gabrielosorio.gestor_inteligente.model.User;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import com.gabrielosorio.gestor_inteligente.repository.base.CheckoutRepository;
import com.gabrielosorio.gestor_inteligente.repository.base.UserRepository;
import com.gabrielosorio.gestor_inteligente.repository.factory.PSQLRepositoryFactory;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CheckoutRepositoryTest {

    private static ConnectionFactory connectionFactory;
    private static PSQLRepositoryFactory repositoryFactory;
    private static CheckoutRepository checkoutRepository;
    private static UserRepository userRepository;
    private static User testUserOpener;
    private static User testUserCloser;
    private static Role testRole;

    @BeforeAll
    static void setUpClass() {
        // Inicializa a conexão com o banco de teste
        connectionFactory = ConnectionFactory.getInstance(DBScheme.POSTGRESQL_TEST);
        repositoryFactory = new PSQLRepositoryFactory(connectionFactory);
        checkoutRepository = repositoryFactory.getCheckoutRepository();
        userRepository = repositoryFactory.getUserRepository();

        // Limpa o banco de dados antes dos testes
        cleanDatabase();

        // Cria dados de teste
        setupTestData();
    }

    @BeforeEach
    void setUp() {
        // Limpa apenas as tabelas relacionadas aos checkouts antes de cada teste
        cleanCheckoutTables();
    }

    @Test
    @Order(1)
    @DisplayName("Deve inserir um checkout no banco de dados")
    void testAddCheckout() {
        // Given
        Checkout newCheckout = createTestCheckout(testUserOpener, null);

        // When
        Checkout savedCheckout = checkoutRepository.add(newCheckout);

        // Then
        assertNotNull(savedCheckout);
        assertNotNull(savedCheckout.getId());
        assertTrue(savedCheckout.getId() > 0);
        assertEquals(CheckoutStatus.OPEN, savedCheckout.getStatus());
        assertEquals(newCheckout.getInitialCash(), savedCheckout.getInitialCash());
        assertEquals(newCheckout.getTotalEntry(), savedCheckout.getTotalEntry());
        assertEquals(newCheckout.getTotalExit(), savedCheckout.getTotalExit());
        assertEquals(newCheckout.getClosingBalance(), savedCheckout.getClosingBalance());
        assertNotNull(savedCheckout.getOpenedAt());
        assertNull(savedCheckout.getClosedAt());
        assertNotNull(savedCheckout.getCreatedAt());
        assertNotNull(savedCheckout.getUpdatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("Deve encontrar um checkout por ID")
    void testFindCheckoutById() {
        // Given
        Checkout newCheckout = createTestCheckout(testUserOpener, null);
        Checkout savedCheckout = checkoutRepository.add(newCheckout);

        // When
        Optional<Checkout> foundCheckout = checkoutRepository.find(savedCheckout.getId());

        // Then
        assertTrue(foundCheckout.isPresent());
        assertEquals(savedCheckout.getId(), foundCheckout.get().getId());
        assertEquals(savedCheckout.getStatus(), foundCheckout.get().getStatus());
        assertEquals(savedCheckout.getInitialCash(), foundCheckout.get().getInitialCash());

        // Verifica se o usuário que abriu foi carregado corretamente
        assertNotNull(foundCheckout.get().getOpenedBy());
        assertEquals(testUserOpener.getId(), foundCheckout.get().getOpenedBy().getId());
        assertEquals(testUserOpener.getFirstName(), foundCheckout.get().getOpenedBy().getFirstName());
    }

    @Test
    @Order(3)
    @DisplayName("Deve retornar empty quando checkout não existe")
    void testFindCheckoutByIdNotFound() {
        // Given
        Long nonExistentId = 999999L;

        // When
        Optional<Checkout> foundCheckout = checkoutRepository.find(nonExistentId);

        // Then
        assertFalse(foundCheckout.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("Deve buscar todos os checkouts")
    void testFindAllCheckouts() {
        // Given
        Checkout checkout1 = createTestCheckout(testUserOpener, null);
        Checkout checkout2 = createTestCheckout(testUserOpener, testUserCloser);
        checkout2.setStatus(CheckoutStatus.CLOSED);
        checkout2.setClosedAt(LocalDateTime.now());

        checkoutRepository.add(checkout1);
        checkoutRepository.add(checkout2);

        // When
        List<Checkout> allCheckouts = checkoutRepository.findAll();

        // Then
        assertNotNull(allCheckouts);
        assertEquals(2, allCheckouts.size());

        for (Checkout checkout : allCheckouts) {
            assertNotNull(checkout.getOpenedBy());
            assertNotNull(checkout.getOpenedBy().getId());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Deve atualizar um checkout existente")
    void testUpdateCheckout() {
        // Given
        Checkout newCheckout = createTestCheckout(testUserOpener, null);
        Checkout savedCheckout = checkoutRepository.add(newCheckout);

        savedCheckout.setStatus(CheckoutStatus.CLOSED);
        savedCheckout.setClosedAt(LocalDateTime.now());
        savedCheckout.setClosedBy(testUserCloser);
        savedCheckout.setTotalEntry(new BigDecimal("500.00"));
        savedCheckout.setTotalExit(new BigDecimal("50.00"));
        savedCheckout.setClosingBalance(new BigDecimal("650.00")); // 200 inicial + 500 entrada - 50 saída
        savedCheckout.setUpdatedAt(LocalDateTime.now());

        // When
        Checkout updatedCheckout = checkoutRepository.update(savedCheckout);

        // Then
        assertNotNull(updatedCheckout);
        assertEquals(savedCheckout.getId(), updatedCheckout.getId());
        assertEquals(CheckoutStatus.CLOSED, updatedCheckout.getStatus());
        assertNotNull(updatedCheckout.getClosedAt());
        assertEquals(new BigDecimal("500.00"), updatedCheckout.getTotalEntry());
        assertEquals(new BigDecimal("50.00"), updatedCheckout.getTotalExit());
        assertEquals(new BigDecimal("650.00"), updatedCheckout.getClosingBalance());

        Optional<Checkout> foundCheckout = checkoutRepository.find(updatedCheckout.getId());
        assertTrue(foundCheckout.isPresent());
        assertEquals(CheckoutStatus.CLOSED, foundCheckout.get().getStatus());
        assertNotNull(foundCheckout.get().getClosedAt());
        assertNotNull(foundCheckout.get().getClosedBy());
        assertEquals(testUserCloser.getId(), foundCheckout.get().getClosedBy().getId());
    }

    @Test
    @Order(6)
    @DisplayName("Deve remover um checkout por ID")
    void testRemoveCheckout() {
        // Given
        Checkout newCheckout = createTestCheckout(testUserOpener, null);
        Checkout savedCheckout = checkoutRepository.add(newCheckout);
        Long checkoutId = savedCheckout.getId();

        // Verifica que o checkout existe
        assertTrue(checkoutRepository.find(checkoutId).isPresent());

        // When
        boolean removed = checkoutRepository.remove(checkoutId);

        // Then
        assertTrue(removed);
        assertFalse(checkoutRepository.find(checkoutId).isPresent());
    }

    @Test
    @Order(7)
    @DisplayName("Deve retornar false ao tentar remover checkout inexistente")
    void testRemoveNonExistentCheckout() {
        // Given
        Long nonExistentId = 999999L;

        // When
        boolean removed = checkoutRepository.remove(nonExistentId);

        // Then
        assertFalse(removed);
    }

    @Test
    @Order(8)
    @DisplayName("Deve encontrar checkout aberto para hoje")
    void testFindOpenCheckoutForToday() {
        // Given - cria um checkout aberto para hoje
        Checkout todayCheckout = createTestCheckout(testUserOpener, null);
        todayCheckout.setOpenedAt(LocalDateTime.now().with(LocalTime.of(8, 0))); // Hoje às 8h
        checkoutRepository.add(todayCheckout);

        // Cria um checkout fechado para hoje (não deve aparecer)
        Checkout closedCheckout = createTestCheckout(testUserOpener, testUserCloser);
        closedCheckout.setStatus(CheckoutStatus.CLOSED);
        closedCheckout.setOpenedAt(LocalDateTime.now().with(LocalTime.of(7, 0))); // Hoje às 7h
        closedCheckout.setClosedAt(LocalDateTime.now().with(LocalTime.of(18, 0))); // Hoje às 18h
        checkoutRepository.add(closedCheckout);

        // When
        Optional<Checkout> openCheckout = checkoutRepository.findOpenCheckoutForToday();

        // Then
        assertTrue(openCheckout.isPresent());
        assertEquals(CheckoutStatus.OPEN, openCheckout.get().getStatus());
        assertEquals(todayCheckout.getId(), openCheckout.get().getId());
        assertNull(openCheckout.get().getClosedAt());
    }

    @Test
    @Order(9)
    @DisplayName("Deve retornar empty quando não há checkout aberto para hoje")
    void testFindOpenCheckoutForTodayNotFound() {
        // Given - apenas checkouts fechados
        Checkout closedCheckout = createTestCheckout(testUserOpener, testUserCloser);
        closedCheckout.setStatus(CheckoutStatus.CLOSED);
        closedCheckout.setClosedAt(LocalDateTime.now());
        checkoutRepository.add(closedCheckout);

        // When
        Optional<Checkout> openCheckout = checkoutRepository.findOpenCheckoutForToday();

        // Then
        assertFalse(openCheckout.isPresent());
    }

    @Test
    @Order(10)
    @DisplayName("Deve carregar checkout com usuários completos")
    void testCheckoutWithCompleteUsers() {
        // Given
        Checkout newCheckout = createTestCheckout(testUserOpener, testUserCloser);
        newCheckout.setStatus(CheckoutStatus.CLOSED);
        newCheckout.setClosedAt(LocalDateTime.now());
        Checkout savedCheckout = checkoutRepository.add(newCheckout);

        // When
        Optional<Checkout> foundCheckout = checkoutRepository.find(savedCheckout.getId());

        // Then
        assertTrue(foundCheckout.isPresent());
        Checkout checkout = foundCheckout.get();

        // Verifica usuário que abriu
        assertNotNull(checkout.getOpenedBy());
        assertEquals(testUserOpener.getId(), checkout.getOpenedBy().getId());
        assertEquals(testUserOpener.getFirstName(), checkout.getOpenedBy().getFirstName());
        assertEquals(testUserOpener.getEmail(), checkout.getOpenedBy().getEmail());

        // Verifica usuário que fechou
        assertNotNull(checkout.getClosedBy());
        assertEquals(testUserCloser.getId(), checkout.getClosedBy().getId());
        assertEquals(testUserCloser.getFirstName(), checkout.getClosedBy().getFirstName());
        assertEquals(testUserCloser.getEmail(), checkout.getClosedBy().getEmail());
    }

    @Test
    @Order(11)
    @DisplayName("Deve funcionar com usuário fechador nulo")
    void testCheckoutWithNullClosedBy() {
        // Given
        Checkout newCheckout = createTestCheckout(testUserOpener, null);

        // When
        Checkout savedCheckout = checkoutRepository.add(newCheckout);

        // Then
        assertNotNull(savedCheckout);
        Optional<Checkout> foundCheckout = checkoutRepository.find(savedCheckout.getId());
        assertTrue(foundCheckout.isPresent());
        assertNotNull(foundCheckout.get().getOpenedBy());
        assertNull(foundCheckout.get().getClosedBy());
    }

    @Test
    @Order(12)
    @DisplayName("Deve validar campos obrigatórios do checkout")
    void testCheckoutValidation() {
        // Given
        Checkout newCheckout = createTestCheckout(testUserOpener, null);
        newCheckout.setInitialCash(new BigDecimal("150.75"));
        newCheckout.setTotalEntry(new BigDecimal("0.00"));
        newCheckout.setTotalExit(new BigDecimal("0.00"));
        newCheckout.setClosingBalance(new BigDecimal("150.75"));

        // When
        Checkout savedCheckout = checkoutRepository.add(newCheckout);

        // Then
        assertNotNull(savedCheckout);
        assertEquals(new BigDecimal("150.75"), savedCheckout.getInitialCash());
        assertEquals(new BigDecimal("0.00"), savedCheckout.getTotalEntry());
        assertEquals(new BigDecimal("0.00"), savedCheckout.getTotalExit());
        assertEquals(new BigDecimal("150.75"), savedCheckout.getClosingBalance());
    }


    private static void setupTestData() {
        testRole = new Role("CASHIER", "Operador de Caixa");
        testRole.setId(1L);

        Permission permission = new Permission();
        permission.setId(1L);
        permission.setPermissionType(PermissionType.CREATE_USER);
        permission.setName("Criar Usuário");
        permission.setDescription("Permissão para criar novos usuários");
        permission.setCategory("USER_MANAGEMENT");
        permission.setActive(true);
        permission.setCreatedAt(LocalDateTime.now());

        testRole.addPermission(permission);

        testUserOpener = createTestUser("João", "Operador", "joao.operador@caixa.com");
        testUserCloser = createTestUser("Maria", "Supervisor", "maria.supervisor@caixa.com");

        testUserOpener = userRepository.add(testUserOpener);
        testUserCloser = userRepository.add(testUserCloser);
    }

    private static User createTestUser(String firstName, String lastName, String email) {
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

    private Checkout createTestCheckout(User openedBy, User closedBy) {
        Checkout checkout = new Checkout();
        checkout.setStatus(CheckoutStatus.OPEN);
        checkout.setOpenedAt(LocalDateTime.now());
        checkout.setClosedAt(closedBy != null ? LocalDateTime.now().plusHours(8) : null);
        checkout.setInitialCash(new BigDecimal("200.00"));
        checkout.setTotalEntry(new BigDecimal("0.00"));
        checkout.setTotalExit(new BigDecimal("0.00"));
        checkout.setClosingBalance(new BigDecimal("200.00"));
        checkout.setOpenedBy(openedBy);
        checkout.setClosedBy(closedBy);
        checkout.setCreatedAt(LocalDateTime.now());
        checkout.setUpdatedAt(LocalDateTime.now());
        return checkout;
    }

    private static void cleanDatabase() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.prepareStatement("TRUNCATE TABLE checkout CASCADE").executeUpdate();
            connection.prepareStatement("TRUNCATE TABLE users CASCADE").executeUpdate();
            connection.prepareStatement("TRUNCATE TABLE role_permissions RESTART IDENTITY CASCADE").executeUpdate();
            connection.prepareStatement("TRUNCATE TABLE roles RESTART IDENTITY CASCADE").executeUpdate();
            connection.prepareStatement("TRUNCATE TABLE permissions RESTART IDENTITY CASCADE").executeUpdate();

            insertBasicTestData(connection);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar banco de dados para testes", e);
        }
    }

    private static void cleanCheckoutTables() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.prepareStatement("DELETE FROM checkout").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar tabela checkouts", e);
        }
    }

    private static void insertBasicTestData(Connection connection) throws SQLException {
        String insertPermission = "INSERT INTO permissions (id, permission_type, name, description, category, active, created_at) " +
                "VALUES (1, 'CREATE_USER', 'Criar Usuário', 'Permissão para criar novos usuários', 'USER_MANAGEMENT', true, CURRENT_TIMESTAMP)";
        connection.prepareStatement(insertPermission).executeUpdate();

        String insertRole = "INSERT INTO roles (id, name, description, active, created_at, updated_at) " +
                "VALUES (1, 'CASHIER', 'Operador de Caixa', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        connection.prepareStatement(insertRole).executeUpdate();

        String insertRolePermission = "INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1)";
        connection.prepareStatement(insertRolePermission).executeUpdate();
    }

    @AfterAll
    static void tearDownClass() {
        cleanDatabase();
    }
}