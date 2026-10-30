package com.ridesharing.service;

import com.ridesharing.model.User;
import com.ridesharing.service.interfaces.AuthService;

/**
 * Production authentication service connected to PostgreSQL via UserService.
 * Manages active user session and performs secure credential verification.
 */
public class RealAuthService implements AuthService {

    private final UserService userService;
    private User currentUser;

    public RealAuthService() {
        this(new UserService());
    }

    public RealAuthService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User login(String emailOrPhone, String password) {
        if (emailOrPhone == null || password == null) {
            return null;
        }
        User authenticated = userService.login(emailOrPhone.trim(), password);
        if (authenticated != null) {
            this.currentUser = authenticated;
        }
        return authenticated;
    }

    @Override
    public User register(User user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("User details cannot be null.");
        }
        User registered = userService.registerUser(user);
        if (registered != null) {
            this.currentUser = registered;
        }
        return registered;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public boolean isLoggedIn() {
        return this.currentUser != null;
    }
}
