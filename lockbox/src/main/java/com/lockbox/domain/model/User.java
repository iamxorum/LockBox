package com.lockbox.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Password> passwords = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SecureNote> secureNotes = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AuditLog> auditLogs = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tag> tags = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserRole> roles = new HashSet<>();
    
    /**
     * Adds a role to the user
     * @param roleName the name of the role to add
     */
    public void addRole(String roleName) {
        UserRole role = new UserRole(roleName, this);
        roles.add(role);
    }
    
    /**
     * Checks if the user has a specific role
     * @param roleName the role name to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Checks if the user is an admin
     * @return true if the user has the ADMIN role, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Custom toString implementation to avoid lazy loading issues
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) &&
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }
} 