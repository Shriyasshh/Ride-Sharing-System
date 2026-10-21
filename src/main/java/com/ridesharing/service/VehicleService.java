package com.ridesharing.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.ridesharing.dao.RideDAO;
import com.ridesharing.dao.UserDAO;
import com.ridesharing.dao.VehicleDAO;
import com.ridesharing.model.Vehicle;

/**
 * Handles vehicle registration, ownership rules, and lifecycle management.
 * Implements com.ridesharing.service.interfaces.VehicleService for UI compatibility.
 */
public class VehicleService implements com.ridesharing.service.interfaces.VehicleService {

    private final VehicleDAO vehicleDAO;
    private final UserDAO userDAO;
    private final RideDAO rideDAO;

    public VehicleService() {
        this(new VehicleDAO(), new UserDAO(), new RideDAO());
    }

    public VehicleService(VehicleDAO vehicleDAO, UserDAO userDAO) {
        this(vehicleDAO, userDAO, new RideDAO());
    }

    public VehicleService(VehicleDAO vehicleDAO, UserDAO userDAO, RideDAO rideDAO) {
        this.vehicleDAO = vehicleDAO;
        this.userDAO = userDAO;
        this.rideDAO = rideDAO;
    }

    /**
     * Add a new vehicle: validate the owner and vehicle details.
     * Enforces the business rule that the owner must exist in the system.
     *
     * @param vehicle the Vehicle entity to add
     * @return Vehicle with generated vehicleId populated
     * @throws IllegalArgumentException if validation fails or owner does not exist
     */
    @Override
    public Vehicle addVehicle(Vehicle vehicle) throws IllegalArgumentException {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle details cannot be null.");
        }
        if (vehicle.getCarModel() == null || vehicle.getCarModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle car model cannot be empty.");
        }
        if (vehicle.getOwnerId() <= 0) {
            throw new IllegalArgumentException("Valid vehicle owner ID is required.");
        }

        try {
            // Business rule: Owner must exist
            if (userDAO.findById(vehicle.getOwnerId()) == null) {
                throw new IllegalArgumentException("Owner with ID " + vehicle.getOwnerId() + " does not exist.");
            }

            vehicle.setCarModel(vehicle.getCarModel().trim());
            int generatedId = vehicleDAO.addVehicle(vehicle);
            vehicle.setVehicleId(generatedId);
            return vehicle;

        } catch (SQLException e) {
            throw new RuntimeException("Database error adding vehicle: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve a vehicle by long vehicleId.
     *
     * @param vehicleId vehicle ID
     * @return Vehicle entity or null if not found
     */
    public Vehicle getVehicleById(long vehicleId) {
        if (vehicleId <= 0 || vehicleId > Integer.MAX_VALUE) {
            return null;
        }
        return findById((int) vehicleId);
    }

    /**
     * Implementation of interface method findById.
     */
    @Override
    public Vehicle findById(int vehicleId) {
        if (vehicleId <= 0) {
            return null;
        }
        try {
            return vehicleDAO.findVehicleById(vehicleId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding vehicle: " + e.getMessage(), e);
        }
    }

    /**
     * List an owner's vehicles by long ownerId.
     *
     * @param ownerId user ID of the vehicle owner
     * @return list of vehicles owned by user
     */
    public List<Vehicle> getVehiclesByOwner(long ownerId) {
        if (ownerId <= 0 || ownerId > Integer.MAX_VALUE) {
            return Collections.emptyList();
        }
        return getVehiclesByOwner((int) ownerId);
    }

    /**
     * Implementation of interface method getVehiclesByOwner.
     */
    @Override
    public List<Vehicle> getVehiclesByOwner(int ownerId) {
        if (ownerId <= 0) {
            return Collections.emptyList();
        }
        try {
            return vehicleDAO.findVehiclesByOwner(ownerId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching vehicles by owner: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a vehicle by long vehicleId: checks whether the vehicle is associated
     * with rides before deletion.
     *
     * @param vehicleId vehicle ID to delete
     * @return true if deleted, false if not found
     * @throws IllegalStateException if vehicle is associated with rides
     */
    public boolean deleteVehicle(long vehicleId) {
        if (vehicleId <= 0 || vehicleId > Integer.MAX_VALUE) {
            return false;
        }
        return deleteVehicle((int) vehicleId);
    }

    /**
     * Delete a vehicle: checks whether the vehicle is associated with rides before deletion.
     *
     * @param vehicleId vehicle ID
     * @return true if deletion succeeded, false otherwise
     * @throws IllegalStateException if vehicle is associated with existing rides
     */
    @Override
    public boolean deleteVehicle(int vehicleId) {
        if (vehicleId <= 0) {
            return false;
        }
        try {
            // Business rule: check whether the vehicle is associated with rides before deletion
            if (rideDAO.isVehicleUsedInRides(vehicleId)) {
                throw new IllegalStateException("Cannot delete vehicle: it is associated with existing rides.");
            }

            return vehicleDAO.deleteVehicle(vehicleId);

        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting vehicle: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve all vehicles in the database.
     *
     * @return list of all Vehicles
     */
    public List<Vehicle> getAllVehicles() {
        try {
            return vehicleDAO.getAllVehicles();
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching all vehicles: " + e.getMessage(), e);
        }
    }
}
