package org.example.service;

import org.example.auth.Role;
import org.example.model.User;
import org.example.persistence.UserRepository;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public Optional<User> login(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    public void register(String username, String password, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(new User(username, password, role));
        }
    }
}

