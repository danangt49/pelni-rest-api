package com.pelni.boarding.ticket.repository;

import com.pelni.boarding.ticket.entity.Admin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    @Value("SELECT a FROM Admin a WHERE a.username = ?1")
    Optional<Admin> findByUsername(String username);
}