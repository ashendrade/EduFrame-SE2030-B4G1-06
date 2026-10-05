package com.eduframepackage.eduframe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * <h1>User Domain Entity</h1>
 * 
 * <p>
 * Represents platform users (Students, Lecturers, Administrators) in EduFrame,
 * mapped to the MSSQL database "users" table.
 * </p>
 */
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "passwordhash", nullable = false, length = 255)
    private String password;

    @Column(name = "firstname", length = 100)
    private String firstName;

    @Column(name = "lastname", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.STUDENT;

    @Column(name = "registereddate")
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String email, String password, String firstName, String lastName, UserRole role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName != null ? firstName : "User";
        this.lastName = lastName != null ? lastName : "";
        this.role = role != null ? role : UserRole.STUDENT;
    }

    public User(String email, String password, String fullName, UserRole role) {
        this.email = email;
        this.password = password;
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split(" ", 2);
            this.firstName = parts[0];
            this.lastName = parts.length > 1 ? parts[1] : "";
        } else {
            this.firstName = "User";
            this.lastName = "";
        }
        this.role = role != null ? role : UserRole.STUDENT;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.role == null) {
            this.role = UserRole.STUDENT;
        }
        if (this.firstName == null || this.firstName.trim().isEmpty()) {
            this.firstName = "User";
        }
        if (this.lastName == null) {
            this.lastName = "";
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
