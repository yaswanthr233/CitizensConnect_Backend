package com.citizensconnect.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(min = 12, max = 12)
    @Column(unique = true)
    private String aadhaar;

    @NotBlank
    @Size(max = 120)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status = UserStatus.APPROVED;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public User(String name, String aadhaar, String password, Role role) {
        this.name = name;
        this.aadhaar = aadhaar;
        this.password = password;
        this.role = role;
        this.status = UserStatus.APPROVED;
    }
}
