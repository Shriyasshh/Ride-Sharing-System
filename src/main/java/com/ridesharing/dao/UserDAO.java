package com.ridesharing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ridesharing.model.User;
import com.ridesharing.util.DBConnection;

public class UserDAO {

    // Create User
    public int registerUser(User user) throws SQLException {
        String sql = "INSERT  INTO users (phone_no,name,gender,email,govt_id,behaviour,password_hash) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getPhoneNo());
            statement.setString(2, user.getName());
            statement.setString(3, user.getGender());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getGovtId());
            statement.setString(6, user.getBehaviour());
            statement.setString(7, user.getPasswordHash());

            int rowInserted = statement.executeUpdate();

            if (rowInserted == 0) {
                throw new SQLException("User Registration Failed");
            }

            try (ResultSet key = statement.getGeneratedKeys()) {
                if (key.next()) {
                    int userId = key.getInt(1);
                    user.setUserId(userId);
                    return userId;
                }
            }
            return -1;
        }
    }

    // Find User by ID
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapUser(result);
                }
            }

        }
        return null;
    }

    // Find User by Phone Number
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

    // Check whether a phone number already exists 
    public boolean isPhoneNoExists(String phoneNo) throws SQLException{
        String sql = "SELECT 1 FROM users WHERE phone_no = ? LIMIT 1";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement statement =conn.prepareStatement(sql)){
                statement.setString(1, phoneNo);
                try(ResultSet result = statement.executeQuery()){
                    return result.next();
                }
            }
    }

    // Check whether Email already exists
    public boolean isEmailExists(String email) throws SQLException{
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement statement =conn.prepareStatement(sql)){
                statement.setString(1, email);
                try(ResultSet result = statement.executeQuery()){
                    return result.next();
                }
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
