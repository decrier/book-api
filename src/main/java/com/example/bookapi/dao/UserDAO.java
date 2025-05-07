package com.example.bookapi.dao;

import com.example.bookapi.model.User;

import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername (String username);
}
