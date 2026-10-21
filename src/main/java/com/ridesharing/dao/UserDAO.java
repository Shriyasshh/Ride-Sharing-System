package com.ridesharing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ridesharing.model.User;
import com.ridesharing.util.DBConnection;

public class UserDAO {

    /**
     * Register a new user and retrieve the generated user_id.
     *
     * @param user User entity to persist
     * @return generated user_id
     * @throws SQLException if validation fails or a database error occurs
     */
    public int registerUser(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String sql = "INSERT INTO users (phone_no, name, gender, email, govt_id, behaviour, password_hash) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getPhoneNo());
            statement.setString(2, user.getName());
            statement.setString(3, user.getGender());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getGovtId());
            statement.setString(6, user.getBehaviour());
            statement.setString(7, user.getPasswordHash());

            int rowInserted = statement.executeUpdate();
            if (rowInserted == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet key = statement.getGeneratedKeys()) {
                if (key.next()) {
                    int userId = key.getInt(1);
                    user.setUserId(userId);
                    return userId;
                }
            }
        }
        throw new SQLException("Creating user failed, user ID was not generated.");
    }

    /**
     * Find user by ID. Returns null if not found.
     *
     * @param userId the user ID
     * @return User or null
     * @throws SQLException if a database error occurs
     */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapUser(result);
                }
            }
        }
        return null;
    }

    /**
     * Find user by phone number. Returns null if not found.
     *
     * @param phoneNo the phone number
     * @return User or null
     * @throws SQLException if a database error occurs
     */
    public User findByPhoneNo(String phoneNo) throws SQLException {
        String sql = "SELECT * FROM users WHERE phone_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, phoneNo);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapUser(result);
                }
            }
        }
        return null;
    }

    /**
     * Find user by email. Returns null if not found.
     *
     * @param email the email address
     * @return User or null
     * @throws SQLException if a database error occurs
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapUser(result);
                }
            }
        }
        return null;
    }

    /**
     * Check whether a phone number already exists.
     *
     * @param phoneNo the phone number
     * @return true if exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isPhoneNoExists(String phoneNo) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE phone_no = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, phoneNo);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * Check whether an email already exists.
     *
     * @param email the email
     * @return true if exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * Retrieve all users ordered by user_id ascending.
     * Returns an empty list if no records exist.
     *
     * @return list of Users
     * @throws SQLException if a database error occurs
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(mapUser(result));
            }
        }
        return users;
    }

    /**
     * Update user details.
     *
     * @param user User with updated details
     * @return true if updated, false if user not found
     * @throws SQLException if a database error occurs
     */
    public boolean updateUser(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String sql = "UPDATE users SET phone_no = ?, name = ?, gender = ?, email = ?, "
                   + "govt_id = ?, behaviour = ?, password_hash = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, user.getPhoneNo());
            statement.setString(2, user.getName());
            statement.setString(3, user.getGender());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getGovtId());
            statement.setString(6, user.getBehaviour());
            statement.setString(7, user.getPasswordHash());
            statement.setInt(8, user.getUserId());

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Delete user by ID.
     *
     * @param userId the ID of the user to delete
     * @return true if deleted, false if not found
     * @throws SQLException if a database error occurs
     */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private User mapUser(ResultSet result) throws SQLException {
        User user = new User();
        user.setUserId(result.getInt("user_id"));
        user.setPhoneNo(result.getString("phone_no"));
        user.setName(result.getString("name"));
        user.setGender(result.getString("gender"));
        user.setEmail(result.getString("email"));
        user.setGovtId(result.getString("govt_id"));
        user.setBehaviour(result.getString("behaviour"));
        user.setPasswordHash(result.getString("password_hash"));
        return user;
    }
}
