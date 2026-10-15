package com.ridesharing.service.interfaces;

import com.ridesharing.model.User;

/**
 * User service interface for profile operations.
 *
 * To integrate with real backend:
 * - Implement using UserDAO for actual database operations.
 * - Replace MockUserService in ServiceFactory with the real implementation.
 */
public interface UserService {

    /**
     * Find a user by their ID.
     */
    User findById(int userId);

    /**
     * Update user profile fields.
     * @return true if the update was successful
     */
    boolean updateProfile(User user);
}
