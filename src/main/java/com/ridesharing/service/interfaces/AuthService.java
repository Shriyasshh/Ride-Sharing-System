package com.ridesharing.service.interfaces;

import com.ridesharing.model.User;

/**
 * Authentication service interface.
 * The UI calls these methods instead of directly accessing the database.
 *
 * To integrate with real backend:
 * - Implement this interface using UserDAO for actual database lookups.
 * - Hash passwords with BCrypt or similar before comparing.
 * - Replace MockAuthService in ServiceFactory with the real implementation.
 */
public interface AuthService {

    /**
     * Attempt to log in with the given credentials.
     * @param emailOrPhone email or phone number
     * @param password raw password
     * @return the authenticated User, or null if credentials are invalid
     */
    User login(String emailOrPhone, String password);

    /**
     * Register a new user account.
     * @param user the User object to register (passwordHash should already be set)
     * @return the registered User with userId populated, or null on failure
     * @throws IllegalArgumentException if validation fails (e.g. duplicate email/phone)
     */
    User register(User user) throws IllegalArgumentException;

    /**
     * Get the currently logged-in user.
     * @return the current User, or null if not logged in
     */
    User getCurrentUser();

    /**
     * Log out the current user.
     */
    void logout();

    /**
     * Check if a user is currently logged in.
     */
    boolean isLoggedIn();
}
