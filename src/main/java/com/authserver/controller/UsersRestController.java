package com.authserver.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController("/users")
public class UsersRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
   
    @GetMapping
    public Map<String, Object> getUsers() {
        return null;
    }

    @PostMapping
    public Map<String, Object> postUsers() {
        return null;
    }

    @PutMapping
    public Map<String, Object> putUsers() {
        return null;
    }

    @DeleteMapping
    public Map<String, Object> deleteUsers() {
        return null;
    }
}