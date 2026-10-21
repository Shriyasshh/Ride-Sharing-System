package com.ridesharing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ridesharing.model.Vehicle;
import com.ridesharing.util.DBConnection;

public class VehicleDAO {

    /**
     * Add a new vehicle and retrieve its generated vehicle_id.
     *
     * @param vehicle the Vehicle entity to persist
     * @return the generated vehicle_id
     * @throws SQLException if a database error occurs or foreign key fails
     */
    public int addVehicle(Vehicle vehicle) throws SQLException {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        String sql = "INSERT INTO vehicles (car_model, owner_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, vehicle.getCarModel());
            statement.setInt(2, vehicle.getOwnerId());

            int rowInserted = statement.executeUpdate();
            if (rowInserted == 0) {
                throw new SQLException("Creating vehicle failed, no rows affected.");
            }

            try (ResultSet key = statement.getGeneratedKeys()) {
                if (key.next()) {
                    int vehicleId = key.getInt(1);
                    vehicle.setVehicleId(vehicleId);
                    return vehicleId;
                }
            }
        }
        throw new SQLException("Creating vehicle failed, vehicle ID was not generated.");
    }

    /**
     * Find a vehicle by its ID. Returns null if not found.
     *
     * @param vehicleId the vehicle ID
     * @return Vehicle entity or null
     * @throws SQLException if a database error occurs
     */
    public Vehicle findVehicleById(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, vehicleId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapVehicle(result);
                }
            }
        }
        return null;
    }

    /**
     * Alias for findVehicleById.
     */
    public Vehicle findById(int vehicleId) throws SQLException {
        return findVehicleById(vehicleId);
    }

    /**
     * Find all vehicles owned by a specific user ordered by vehicle_id ascending.
     * Returns an empty list if none found.
     *
     * @param ownerId user ID of the owner
     * @return list of vehicles owned by user
     * @throws SQLException if a database error occurs
     */
    public List<Vehicle> findVehiclesByOwner(int ownerId) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE owner_id = ? ORDER BY vehicle_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, ownerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    vehicles.add(mapVehicle(result));
                }
            }
        }
        return vehicles;
    }

    /**
     * Retrieve all vehicles in the database ordered by vehicle_id ascending.
     * Returns an empty list if none found.
     *
     * @return list of all Vehicles
     * @throws SQLException if a database error occurs
     */
    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles ORDER BY vehicle_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                vehicles.add(mapVehicle(result));
            }
        }
        return vehicles;
    }

    /**
     * Update vehicle details.
     *
     * @param vehicle the vehicle with updated information
     * @return true if updated, false if not found
     * @throws SQLException if a database error occurs
     */
    public boolean updateVehicle(Vehicle vehicle) throws SQLException {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        String sql = "UPDATE vehicles SET car_model = ?, owner_id = ? WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, vehicle.getCarModel());
            statement.setInt(2, vehicle.getOwnerId());
            statement.setInt(3, vehicle.getVehicleId());

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Delete a vehicle by its ID.
     *
     * @param vehicleId the ID of the vehicle to delete
     * @return true if deleted, false if not found
     * @throws SQLException if a database error occurs
     */
    public boolean deleteVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, vehicleId);
            return statement.executeUpdate() > 0;
        }
    }

    private Vehicle mapVehicle(ResultSet result) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(result.getInt("vehicle_id"));
        vehicle.setCarModel(result.getString("car_model"));
        vehicle.setOwnerId(result.getInt("owner_id"));
        return vehicle;
    }
}
