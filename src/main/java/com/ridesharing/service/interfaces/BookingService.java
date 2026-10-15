package com.ridesharing.service.interfaces;

import com.ridesharing.model.Booking;
import java.util.List;

/**
 * Booking service interface.
 *
 * To integrate with real backend:
 * - Implement using a BookingDAO for actual database operations.
 * - Replace MockBookingService in ServiceFactory with the real implementation.
 */
public interface BookingService {

    /**
     * Create a new booking.
     * @return the Booking with bookingId and bookingTime populated
     * @throws IllegalArgumentException if booking validation fails
     */
    Booking createBooking(int rideId, int passengerId, int seatsBooked) throws IllegalArgumentException;

    /**
     * Cancel an existing booking.
     * @return true if cancellation was successful
     */
    boolean cancelBooking(int bookingId);

    /**
     * Get all bookings made by a specific passenger.
     */
    List<Booking> getBookingsByPassenger(int passengerId);

    /**
     * Get all bookings for a specific ride.
     */
    List<Booking> getBookingsByRide(int rideId);

    /**
     * Find a booking by its ID.
     */
    Booking findById(int bookingId);

    /**
     * Get the total confirmed seats booked for a ride.
     */
    int getConfirmedSeatsForRide(int rideId);
}
