package com.ridesharing.service.mock;

import com.ridesharing.model.User;
import com.ridesharing.service.interfaces.AuthService;

/**
 * Mock authentication service using in-memory data.
 * Passwords are compared as plain text in mock mode.
 * Real implementation should use BCrypt hashing.
 */
public class MockAuthService implements AuthService {

    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public User login(String emailOrPhone, String password) {
        if (emailOrPhone == null || password == null) return null;
        String input = emailOrPhone.trim().toLowerCase();

        for (User u : store.getUsers()) {
            boolean emailMatch = u.getEmail() != null && u.getEmail().toLowerCase().equals(input);
            boolean phoneMatch = u.getPhoneNo() != null && u.getPhoneNo().equals(input);
            if ((emailMatch || phoneMatch) && password.equals(u.getPasswordHash())) {
                store.setCurrentUser(u);
                return u;
            }
        }
        return null;
    }

    @Override
    public User register(User user) throws IllegalArgumentException {
        // Validate required fields
        if (user.getName() == null || user.getName().trim().isEmpty())
            throw new IllegalArgumentException("Name is required.");
        if (user.getEmail() == null || user.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email is required.");
        if (user.getPhoneNo() == null || user.getPhoneNo().trim().isEmpty())
            throw new IllegalArgumentException("Phone number is required.");
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty())
            throw new IllegalArgumentException("Password is required.");

        // Check duplicates
        for (User u : store.getUsers()) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(user.getEmail()))
                throw new IllegalArgumentException("An account with this email already exists.");
            if (u.getPhoneNo() != null && u.getPhoneNo().equals(user.getPhoneNo()))
                throw new IllegalArgumentException("An account with this phone number already exists.");
        }

        user.setUserId(store.nextUserId());
        store.getUsers().add(user);
        store.setCurrentUser(user);
        return user;
    }

    @Override
    public User getCurrentUser() {
        return store.getCurrentUser();
    }

    @Override
    public void logout() {
        store.setCurrentUser(null);
    }

    @Override
    public boolean isLoggedIn() {
        return store.getCurrentUser() != null;
    }
}
