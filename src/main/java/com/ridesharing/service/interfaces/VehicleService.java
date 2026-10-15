package com.ridesharing.service.interfaces;

import com.ridesharing.model.Vehicle;
import java.util.List;

/**
 * Vehicle service interface.
 *
 * To integrate with real backend:
 * - Implement using VehicleDAO for actual database operations.
 * - Replace MockVehicleService in ServiceFactory with the real implementation.
 */
public interface VehicleService {

    /**
     * Get all vehicles belonging to a specific owner.
     */
    List<Vehicle> getVehiclesByOwner(int ownerId);

    /**
     * Add a new vehicle.
     * @return the Vehicle with vehicleId populated
     */
    Vehicle addVehicle(Vehicle vehicle);

    /**
     * Find a vehicle by its ID.
     */
    Vehicle findById(int vehicleId);

    /**
     * Delete a vehicle by its ID.
     * @return true if deletion was successful
     */
    boolean deleteVehicle(int vehicleId);
}
