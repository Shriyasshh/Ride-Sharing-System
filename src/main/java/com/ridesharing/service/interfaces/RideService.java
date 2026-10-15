package com.ridesharing.service.interfaces;

import com.ridesharing.model.Ride;
import java.time.LocalDate;
import java.util.List;

/**
 * Ride service interface.
 *
 * To integrate with real backend:
 * - Implement using a RideDAO for actual database operations.
 * - Replace MockRideService in ServiceFactory with the real implementation.
 */
public interface RideService {

    /**
     * Publish a new ride.
     * @return the Ride with rideId populated
     */
    Ride publishRide(Ride ride);

    /**
     * Search for available rides matching criteria.
     * @param source source address (partial match)
     * @param destination destination address (partial match)
     * @param date departure date (nullable for all dates)
     * @param minSeats minimum available seats required
     * @return matching rides
     */
    List<Ride> searchRides(String source, String destination, LocalDate date, int minSeats);

    /**
     * Get all rides published by a specific driver.
     */
    List<Ride> getRidesByDriver(int driverId);

    /**
     * Get all rides (for browsing).
     */
    List<Ride> getAllRides();

    /**
     * Find a ride by its ID.
     */
    Ride findById(int rideId);

    /**
     * Cancel a ride.
     * @return true if cancellation was successful
     */
    boolean cancelRide(int rideId);

    /**
     * Get the number of remaining seats for a ride.
     */
    int getRemainingSeats(int rideId);
}
