package com.pelni.boarding.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "admin_new")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @Column(name = "nama", nullable = false, length = 50)
    private String name;

    @Column(name = "no_hp", nullable = false, length = 12)
    private String phoneNumber;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "role", nullable = false, length = 10)
    private String role;

    @Column(name = "tipe", nullable = false, length = 10)
    private String type;

    @Column(name = "printer")
    private String printer;

    @Column(name = "hapus")
    private Integer deleted;
}
