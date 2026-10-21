package com.ridesharing.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.ridesharing.dao.BookingDAO;
import com.ridesharing.dao.RideDAO;
import com.ridesharing.dao.UserDAO;
import com.ridesharing.dao.VehicleDAO;
import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.Vehicle;

/**
 * Handles publishing and searching for rides, driver verification, vehicle ownership validation,
 * status transitions, and cancellation with booking cascade.
 * Implements com.ridesharing.service.interfaces.RideService for UI compatibility.
 */
public class RideService implements com.ridesharing.service.interfaces.RideService {

    private final RideDAO rideDAO;
    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;
    private final BookingDAO bookingDAO;

    public RideService() {
        this(new RideDAO(), new UserDAO(), new VehicleDAO(), new BookingDAO());
    }

    public RideService(RideDAO rideDAO, UserDAO userDAO, VehicleDAO vehicleDAO, BookingDAO bookingDAO) {
        this.rideDAO = rideDAO;
        this.userDAO = userDAO;
        this.vehicleDAO = vehicleDAO;
        this.bookingDAO = bookingDAO;
    }

    /**
     * Create a new ride: validate the driver, vehicle ownership, seat count, fare, and departure time.
     * Enforces that the driver must own the selected vehicle.
     *
     * @param ride Ride entity to create
     * @return published Ride with generated rideId
     * @throws IllegalArgumentException if validation fails
     */
    public Ride createRide(Ride ride) throws IllegalArgumentException {
        if (ride == null) {
            throw new IllegalArgumentException("Ride details cannot be null.");
        }
        if (ride.getDriverId() <= 0) {
            throw new IllegalArgumentException("Valid driver ID is required.");
        }
        if (ride.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Valid vehicle ID is required.");
        }
        if (ride.getNoOfSeats() <= 0) {
            throw new IllegalArgumentException("Available seats must be greater than zero.");
        }
        if (ride.getSourceAddress() == null || ride.getSourceAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Source location is required.");
        }
        if (ride.getDestinationAddress() == null || ride.getDestinationAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination location is required.");
        }
        if (ride.getSourceAddress().trim().equalsIgnoreCase(ride.getDestinationAddress().trim())) {
            throw new IllegalArgumentException("Source and destination locations cannot be identical.");
        }
        if (ride.getDepartureTime() == null) {
            throw new IllegalArgumentException("Departure date and time are required.");
        }
        if (ride.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Departure time cannot be in the past.");
        }
        if (ride.getCostPerSeat() == null || ride.getCostPerSeat().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost per seat cannot be negative.");
        }

        try {
            // Business rule: driver must exist
            if (userDAO.findById(ride.getDriverId()) == null) {
                throw new IllegalArgumentException("Driver with ID " + ride.getDriverId() + " does not exist.");
            }

            // Business rule: driver must own the selected vehicle
            Vehicle vehicle = vehicleDAO.findVehicleById(ride.getVehicleId());
            if (vehicle == null) {
                throw new IllegalArgumentException("Vehicle with ID " + ride.getVehicleId() + " does not exist.");
            }
            if (vehicle.getOwnerId() != ride.getDriverId()) {
                throw new IllegalArgumentException("Driver must own the selected vehicle.");
            }

            ride.setSourceAddress(ride.getSourceAddress().trim());
            ride.setDestinationAddress(ride.getDestinationAddress().trim());
            ride.setRideStatus(Ride.RideStatus.AVAILABLE);

            int generatedId = rideDAO.createRide(ride);
            ride.setRideId(generatedId);
            return ride;

        } catch (SQLException e) {
            throw new RuntimeException("Database error creating ride: " + e.getMessage(), e);
        }
    }

    /**
     * Alias for createRide implementing interface publishRide.
     */
    @Override
    public Ride publishRide(Ride ride) throws IllegalArgumentException {
        return createRide(ride);
    }

    /**
     * Retrieve ride details by long rideId.
     *
     * @param rideId ride ID
     * @return Ride entity or null if not found
     */
    public Ride getRideById(long rideId) {
        if (rideId <= 0 || rideId > Integer.MAX_VALUE) {
            return null;
        }
        return findById((int) rideId);
    }

    /**
     * Implementation of interface method findById.
     */
    @Override
    public Ride findById(int rideId) {
        if (rideId <= 0) {
            return null;
        }
        try {
            return rideDAO.findById(rideId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding ride: " + e.getMessage(), e);
        }
    }

    /**
     * Show rides published by a driver by long driverId.
     *
     * @param driverId driver user ID
     * @return list of rides
     */
    public List<Ride> getRidesByDriver(long driverId) {
        if (driverId <= 0 || driverId > Integer.MAX_VALUE) {
            return Collections.emptyList();
        }
        return getRidesByDriver((int) driverId);
    }

    /**
     * Implementation of interface method getRidesByDriver.
     */
    @Override
    public List<Ride> getRidesByDriver(int driverId) {
        if (driverId <= 0) {
            return Collections.emptyList();
        }
        try {
            return rideDAO.findRidesByDriver(driverId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching driver rides: " + e.getMessage(), e);
        }
    }

    /**
     * Search available rides matching source, destination, and departure date.
     *
     * @param source source address (partial match)
     * @param destination destination address (partial match)
     * @param date departure date filter (nullable for all dates)
     * @return list of matching available rides
     */
    public List<Ride> searchAvailableRides(String source, String destination, LocalDate date) {
        return searchRides(source, destination, date, 1);
    }

    /**
     * Search available rides with minimum seat requirement.
     */
    @Override
    public List<Ride> searchRides(String source, String destination, LocalDate date, int minSeats) {
        try {
            return rideDAO.searchAvailableRides(source, destination, date, minSeats);
        } catch (SQLException e) {
            throw new RuntimeException("Database error searching rides: " + e.getMessage(), e);
        }
    }

    /**
     * Get all rides in the system.
     *
     * @return list of all rides
     */
    @Override
    public List<Ride> getAllRides() {
        try {
            return rideDAO.getAllRides();
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching all rides: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel a ride: verify driver ownership and handle booking implications before cancellation.
     * Only the publishing driver can cancel their ride.
     *
     * @param rideId ride ID to cancel
     * @param driverId user ID of the requesting driver
     * @return true if cancelled successfully
     * @throws IllegalArgumentException if ride not found or user is not the publishing driver
     */
    public boolean cancelRide(long rideId, long driverId) {
        if (rideId <= 0 || driverId <= 0) {
            throw new IllegalArgumentException("Invalid ride or driver ID.");
        }

        try {
            Ride ride = rideDAO.findById((int) rideId);
            if (ride == null) {
                throw new IllegalArgumentException("Ride with ID " + rideId + " does not exist.");
            }

            // Business rule: verify ownership
            if (ride.getDriverId() != driverId) {
                throw new IllegalArgumentException("Only the publishing driver can cancel this ride.");
            }

            if (ride.getRideStatus() == Ride.RideStatus.CANCELLED) {
                return false;
            }

            // Update ride status to CANCELLED
            boolean updated = rideDAO.updateRideStatus((int) rideId, Ride.RideStatus.CANCELLED);
            if (!updated) {
                return false;
            }

            // Business rule: booking implications - cascade-cancel confirmed bookings
            List<Booking> rideBookings = bookingDAO.findBookingsByRide((int) rideId);
            for (Booking b : rideBookings) {
                if (b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                    bookingDAO.cancelBooking(b.getBookingId());
                }
            }

            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Database error cancelling ride: " + e.getMessage(), e);
        }
    }

    /**
     * Implementation of interface method cancelRide.
     */
    @Override
    public boolean cancelRide(int rideId) {
        if (rideId <= 0) {
            return false;
        }
        Ride ride = findById(rideId);
        if (ride == null) {
            return false;
        }
        return cancelRide(rideId, ride.getDriverId());
    }

    /**
     * Update ride status applying valid status transitions:
     * - AVAILABLE -> IN_PROGRESS or CANCELLED
     * - IN_PROGRESS -> CANCELLED
     * - CANCELLED cannot transition further
     *
     * @param rideId ride ID
     * @param newStatus requested RideStatus
     * @return true if updated, false otherwise
     * @throws IllegalStateException if the status transition is invalid
     */
    public boolean updateRideStatus(long rideId, Ride.RideStatus newStatus) {
        if (rideId <= 0 || newStatus == null) {
            throw new IllegalArgumentException("Valid ride ID and RideStatus are required.");
        }

        try {
            Ride ride = rideDAO.findById((int) rideId);
            if (ride == null) {
                throw new IllegalArgumentException("Ride with ID " + rideId + " not found.");
            }

            Ride.RideStatus current = ride.getRideStatus();
            if (current == newStatus) {
                return true;
            }

            // Business rule: apply valid status transitions
            if (current == Ride.RideStatus.CANCELLED) {
                throw new IllegalStateException("Cannot transition status: ride is already CANCELLED.");
            }

            if (current == Ride.RideStatus.IN_PROGRESS && newStatus == Ride.RideStatus.AVAILABLE) {
                throw new IllegalStateException("Cannot revert ride status from IN_PROGRESS back to AVAILABLE.");
            }

            boolean updated = rideDAO.updateRideStatus((int) rideId, newStatus);

            // If ride was cancelled, cascade-cancel confirmed bookings
            if (updated && newStatus == Ride.RideStatus.CANCELLED) {
                List<Booking> bookings = bookingDAO.findBookingsByRide((int) rideId);
                for (Booking b : bookings) {
                    if (b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                        bookingDAO.cancelBooking(b.getBookingId());
                    }
                }
            }

            return updated;

        } catch (SQLException e) {
            throw new RuntimeException("Database error updating ride status: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate remaining seats for a ride by long rideId.
     */
    public int getRemainingSeats(long rideId) {
        if (rideId <= 0 || rideId > Integer.MAX_VALUE) {
            return 0;
        }
        return getRemainingSeats((int) rideId);
    }

    /**
     * Implementation of interface method getRemainingSeats.
     */
    @Override
    public int getRemainingSeats(int rideId) {
        if (rideId <= 0) {
            return 0;
        }
        try {
            return rideDAO.getRemainingSeats(rideId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error calculating remaining seats: " + e.getMessage(), e);
        }
    }
}
