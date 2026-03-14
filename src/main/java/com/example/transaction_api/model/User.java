package com.example.transaction_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", 
       indexes = {
           @Index(name = "idx_username", columnList = "username"),
           @Index(name = "idx_email", columnList = "email")
       })
@EntityListeners(AuditingEntityListener.class)

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled"})
public class User  implements UserDetails{    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;    
    
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // Prevent circular reference in JSON serialization
    private List<Transaction> transactions = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper method to add transactions
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setUser(this);
    }
    
    // Helper method to remove transactions
    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setUser(null);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }
    
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
