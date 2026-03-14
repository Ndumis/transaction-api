package com.example.transaction_api.repository;

import com.example.transaction_api.model.User;
import com.example.transaction_api.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private User user1;
    private User user2;
    private String userId1;
    private String userId2;
    
    @BeforeEach
    void setUp() {
        // Clean up
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        LocalDateTime now = LocalDateTime.now();
        
        // Create test users
        user1 = User.builder()
                .username("john_doe")
                .email("john.doe@email.com")
                .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE4lBo6oIWtJ/WsDO")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+27123456789")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        user2 = User.builder()
                .username("jane_smith")
                .email("jane.smith@email.com")
                .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE4lBo6oIWtJ/WsDO")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+27987654321")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        
        userId1 = user1.getId();
        userId2 = user2.getId();
    }
    
    @Test
    void findByUsername_ReturnsUser() {
        Optional<User> found = userRepository.findByUsername("john_doe");
        
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john_doe");
        assertThat(found.get().getEmail()).isEqualTo("john.doe@email.com");
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }
    
    @Test
    void findByUsername_NotFound_ReturnsEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        
        assertThat(found).isEmpty();
    }
    
    @Test
    void findByEmail_ReturnsUser() {
        Optional<User> found = userRepository.findByEmail("jane.smith@email.com");
        
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jane_smith");
        assertThat(found.get().getEmail()).isEqualTo("jane.smith@email.com");
    }
    
    @Test
    void findByEmail_NotFound_ReturnsEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@email.com");
        
        assertThat(found).isEmpty();
    }
    
    @Test
    void findByStatus_ReturnsUsers() {
        Page<User> users = userRepository.findByStatus(UserStatus.ACTIVE, PageRequest.of(0, 10));
        
        assertThat(users).isNotNull();
        assertThat(users.getContent()).hasSize(2);
        assertThat(users.getContent()).allMatch(u -> u.getStatus() == UserStatus.ACTIVE);
    }
    
    @Test
    void findByStatus_WithInactive_ReturnsEmpty() {
        // Create an inactive user
        User inactiveUser = User.builder()
                .username("inactive_user")
                .email("inactive@email.com")
                .password("password")
                .firstName("Inactive")
                .lastName("User")
                .status(UserStatus.INACTIVE)
                .build();
        
        entityManager.persist(inactiveUser);
        entityManager.flush();
        
        Page<User> users = userRepository.findByStatus(UserStatus.INACTIVE, PageRequest.of(0, 10));
        
        assertThat(users).isNotNull();
        assertThat(users.getContent()).hasSize(1);
        assertThat(users.getContent().get(0).getUsername()).isEqualTo("inactive_user");
    }
    
    @Test
    void searchUsers_ByFirstName_ReturnsMatches() {
        Page<User> users = userRepository.searchUsers("John", PageRequest.of(0, 10));
        
        assertThat(users).isNotNull();
        assertThat(users.getContent()).hasSize(1);
        assertThat(users.getContent().get(0).getFirstName()).isEqualTo("John");
    }
    
    @Test
    void searchUsers_ByLastName_ReturnsMatches() {
        Page<User> users = userRepository.searchUsers("Smith", PageRequest.of(0, 10));
        
        assertThat(users).isNotNull();
        assertThat(users.getContent()).hasSize(1);
        assertThat(users.getContent().get(0).getLastName()).isEqualTo("Smith");
    }
    
    @Test
    void searchUsers_ByEmail_ReturnsMatches() {
        Page<User> users = userRepository.searchUsers("john.doe", PageRequest.of(0, 10));
        
        assertThat(users).isNotNull();
        assertThat(users.getContent()).hasSize(1);
        assertThat(users.getContent().get(0).getEmail()).isEqualTo("john.doe@email.com");
    }
    
    @Test
    void searchUsers_WithPartialMatch_ReturnsMatches() {
        Page<User> users = userRepository.searchUsers("john", PageRequest.of(0, 10));
        
        assertThat(users).isNotNull();
        assertThat(users.getContent()).hasSize(1);
        assertThat(users.getContent().get(0).getUsername()).isEqualTo("john_doe");
    }
    
    @Test
    void searchUsers_WithNoMatch_ReturnsEmpty() {
        Page<User> users = userRepository.searchUsers("xyzabc", PageRequest.of(0, 10));
        
        assertThat(users).isNotNull();
        assertThat(users.getContent()).isEmpty();
    }
    
    @Test
    void existsByUsername_ReturnsTrue_WhenExists() {
        boolean exists = userRepository.existsByUsername("john_doe");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    void existsByUsername_ReturnsFalse_WhenNotExists() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        
        assertThat(exists).isFalse();
    }
    
    @Test
    void existsByEmail_ReturnsTrue_WhenExists() {
        boolean exists = userRepository.existsByEmail("jane.smith@email.com");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    void existsByEmail_ReturnsFalse_WhenNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@email.com");
        
        assertThat(exists).isFalse();
    }
    
    @Test
    void save_UserWithAllFields_Success() {
        User newUser = User.builder()
                .username("new_user")
                .email("new@email.com")
                .password("password")
                .firstName("New")
                .lastName("User")
                .phoneNumber("+27000000000")
                .status(UserStatus.ACTIVE)
                .build();
        
        User saved = userRepository.save(newUser);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("new_user");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void delete_User_Success() {
        userRepository.deleteById(userId1);
        
        Optional<User> found = userRepository.findById(userId1);
        assertThat(found).isEmpty();
    }
}