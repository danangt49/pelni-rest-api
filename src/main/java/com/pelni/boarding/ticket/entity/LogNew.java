package com.pelni.boarding.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "log_new")
public class LogNew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "level")
    private String level;

    @Column(name = "aktivitas")
    private String activity;

    @Column(name = "request")
    private String request;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "ip")
    private String ipAddress;

    @Column(name = "datetime")
    private LocalDateTime datetime;

    @Column(name = "vm")
    private String vm;
}
