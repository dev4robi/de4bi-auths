package com.authserver.data.jpa.repository;

import com.authserver.data.jpa.table.Users;

import org.springframework.data.jpa.repository.JpaRepository;

public class UsersRepository extends JpaRepository<Users, Long> {
    
}