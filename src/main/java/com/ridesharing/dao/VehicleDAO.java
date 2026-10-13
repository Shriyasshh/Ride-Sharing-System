package com.ridesharing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ridesharing.model.Vehicle;
import com.ridesharing.util.DBConnection;

public class VehicleDAO {

//  Create a Ride
    public int addVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (car_model, owner_id) VALUES(?,?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, vehicle.getCarModel());
            statement.setInt(2, vehicle.getOwnerId());

            int rowInserted = statement.executeUpdate();

            if (rowInserted == 0) {
                throw new SQLException("Vehicle Inseration Failed");
            }

            try (ResultSet key = statement.getGeneratedKeys()) {
                if (key.next()) {
                    int vehicleId = key.getInt(1);
                    vehicle.setVehicleId(vehicleId);
                    return vehicleId;
                }
            }
        }
        throw new SQLException("Vehicle ID was not generated");
    }

//    Find Vehicle by Id
    public Vehicle findVehicleById(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE vehicle_id =?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, vehicleId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapVehicle(result);
                }
            }
        }
        return null;
    }

//    Vehicles Owner by Single User
    public List<Vehicle> findVehiclesByOwner(int ownerId) throws SQLException {

        List<Vehicle> vehicles = new ArrayList<>();

        String sql = "SELECT * FROM vehicles WHERE owner_id = ? ORDER BY vehicle_id";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, ownerId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    vehicles.add(mapVehicle(result));
                }
            }
        }
        return vehicles;
    }

//  Delete a Vehicle
    public void deleteVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, vehicleId);

            int rowRemoved = statement.executeUpdate();
            if (rowRemoved == 0) {
                throw new SQLException("Row either not present or failed to delete");
            }
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
