package com.ridesharing.service;

import java.sql.SQLException;

import com.ridesharing.dao.UserDAO;
import com.ridesharing.model.User;
import com.ridesharing.util.PasswordUtil;

/**
 * Handles registration, user lookup, credential verification, and account validation.
 * Implements com.ridesharing.service.interfaces.UserService for UI compatibility.
 */
public class UserService implements com.ridesharing.service.interfaces.UserService {

    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAO());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Register a new user: validate required fields, hash password securely,
     * and check phone/email uniqueness before calling UserDAO.
     *
     * @param user the User entity to register
     * @return the registered User with generated userId
     * @throws IllegalArgumentException if validation fails or email/phone is already taken
     */
    public User registerUser(User user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("User details cannot be null.");
        }

        // Validate required fields
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (user.getPhoneNo() == null || user.getPhoneNo().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        String cleanPhone = user.getPhoneNo().trim();
        if (cleanPhone.length() < 10) {
            throw new IllegalArgumentException("Phone number must contain at least 10 digits.");
        }
        if (user.getGender() == null || user.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required.");
        }
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String cleanEmail = user.getEmail().trim();
            if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                throw new IllegalArgumentException("Invalid email format.");
            }
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        try {
            // Uniqueness pre-check before calling UserDAO
            if (userDAO.isPhoneNoExists(cleanPhone)) {
                throw new IllegalArgumentException("Phone number is already registered.");
            }
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                if (userDAO.isEmailExists(user.getEmail().trim())) {
                    throw new IllegalArgumentException("Email is already registered.");
                }
            }

            user.setPhoneNo(cleanPhone);
            if (user.getEmail() != null) {
                user.setEmail(user.getEmail().trim());
            }

            // Securely hash password if not already hashed
            String rawOrHashed = user.getPasswordHash().trim();
            if (!rawOrHashed.contains(":")) {
                user.setPasswordHash(PasswordUtil.hashPassword(rawOrHashed));
            }

            int generatedId = userDAO.registerUser(user);
            user.setUserId(generatedId);
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Database error during user registration: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve a user by their ID.
     *
     * @param userId user ID
     * @return User entity or null if not found
     */
    public User getUserById(long userId) {
        if (userId <= 0 || userId > Integer.MAX_VALUE) {
            return null;
        }
        return getUserById((int) userId);
    }

    /**
     * Retrieve a user by integer ID.
     *
     * @param userId user ID
     * @return User entity or null if not found
     */
    public User getUserById(int userId) {
        if (userId <= 0) {
            return null;
        }
        try {
            return userDAO.findById(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching user by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Implementation of interface method findById.
     */
    @Override
    public User findById(int userId) {
        return getUserById(userId);
    }

    /**
     * Retrieve a user by phone number.
     *
     * @param phone phone number
     * @return User entity or null if not found
     */
    public User getUserByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        try {
            return userDAO.findByPhoneNo(phone.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching user by phone: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve a user by email address.
     *
     * @param email email address
     * @return User entity or null if not found
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        try {
            return userDAO.findByEmail(email.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching user by email: " + e.getMessage(), e);
        }
    }

    /**
     * Verify credentials securely using a hashed password comparison, never a plaintext password.
     * Supports login via phone or email identifier.
     *
     * @param phoneOrEmail phone number or email address
     * @param password raw password to verify
     * @return the authenticated User if valid, or null if credentials do not match
     */
    public User login(String phoneOrEmail, String password) {
        if (phoneOrEmail == null || phoneOrEmail.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }

        String identifier = phoneOrEmail.trim();
        User user = getUserByPhone(identifier);
        if (user == null) {
            user = getUserByEmail(identifier);
        }

        if (user == null) {
            return null;
        }

        // Verify securely hashed password
        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return user;
        }

        return null;
    }

    /**
     * Update user profile fields (name, phone, behaviour).
     *
     * @param user user entity with updated information
     * @return true if update succeeded, false otherwise
     */
    @Override
    public boolean updateProfile(User user) {
        if (user == null || user.getUserId() <= 0) {
            return false;
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }

        try {
            // Check if phone changed and belongs to another user
            if (user.getPhoneNo() != null && !user.getPhoneNo().trim().isEmpty()) {
                User existingWithPhone = userDAO.findByPhoneNo(user.getPhoneNo().trim());
                if (existingWithPhone != null && existingWithPhone.getUserId() != user.getUserId()) {
                    throw new IllegalArgumentException("Phone number is already used by another account.");
                }
            }

            // Check if email changed and belongs to another user
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                User existingWithEmail = userDAO.findByEmail(user.getEmail().trim());
                if (existingWithEmail != null && existingWithEmail.getUserId() != user.getUserId()) {
                    throw new IllegalArgumentException("Email is already used by another account.");
                }
            }

            return userDAO.updateUser(user);

        } catch (SQLException e) {
            throw new RuntimeException("Database error updating user profile: " + e.getMessage(), e);
        }
    }
}
