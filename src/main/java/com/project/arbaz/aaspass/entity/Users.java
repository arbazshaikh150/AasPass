package com.project.arbaz.aaspass.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String provider;

    private String email;

    private String name;

    @Column(nullable = false)
    private String providerSubject;

    private String role; // keeping it simple user , Admin

}
