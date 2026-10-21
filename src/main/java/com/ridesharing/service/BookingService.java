package com.ridesharing.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.ridesharing.dao.BookingDAO;
import com.ridesharing.dao.RideDAO;
import com.ridesharing.dao.UserDAO;
import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;

/**
 * Handles passenger seat reservations, concurrency-safe capacity enforcement,
 * passenger ownership verification on cancellation, and seat calculations.
 * Implements com.ridesharing.service.interfaces.BookingService for UI compatibility.
 */
public class BookingService implements com.ridesharing.service.interfaces.BookingService {

    private final BookingDAO bookingDAO;
    private final RideDAO rideDAO;
    private final UserDAO userDAO;

    public BookingService() {
        this(new BookingDAO(), new RideDAO(), new UserDAO());
    }

    public BookingService(BookingDAO bookingDAO, RideDAO rideDAO, UserDAO userDAO) {
        this.bookingDAO = bookingDAO;
        this.rideDAO = rideDAO;
        this.userDAO = userDAO;
    }

    /**
     * Book a ride: validate the ride, passenger, available seats, departure time, and duplicate booking rules.
     * Enforces the business rules:
     * 1. Cannot book a cancelled or departed ride.
     * 2. Confirmed bookings must not exceed ride capacity.
     * 3. Driver cannot book their own ride.
     * 4. Seat-capacity enforcement is safe under concurrent requests via transactional locking.
     *
     * @param rideId ID of the ride
     * @param passengerId ID of the booking passenger
     * @param seats number of seats to reserve
     * @return populated Booking entity
     * @throws IllegalArgumentException or IllegalStateException if validation fails
     */
    public Booking bookRide(long rideId, long passengerId, int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Must book at least 1 seat.");
        }
        if (rideId <= 0 || rideId > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid ride ID.");
        }
        if (passengerId <= 0 || passengerId > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid passenger ID.");
        }

        try {
            // Business rule: Passenger must exist
            if (userDAO.findById((int) passengerId) == null) {
                throw new IllegalArgumentException("Passenger with ID " + passengerId + " does not exist.");
            }

            // Business rule: Ride must exist
            Ride ride = rideDAO.findById((int) rideId);
            if (ride == null) {
                throw new IllegalArgumentException("Ride with ID " + rideId + " does not exist.");
            }

            // Business rule: Cannot book a cancelled or departed ride
            if (ride.getRideStatus() != Ride.RideStatus.AVAILABLE) {
                throw new IllegalStateException("Cannot book a ride with status " + ride.getRideStatus() + ".");
            }
            if (ride.getDepartureTime() != null && ride.getDepartureTime().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Cannot book a departed ride.");
            }

            // Business rule: Driver cannot book their own ride
            if (ride.getDriverId() == passengerId) {
                throw new IllegalArgumentException("Driver cannot book their own ride.");
            }

            // Business rule: Confirmed bookings must not exceed ride capacity
            int remaining = getAvailableSeats(rideId);
            if (seats > remaining) {
                throw new IllegalStateException("Only " + remaining + " seat(s) remaining.");
            }

            Booking booking = new Booking((int) rideId, (int) passengerId, seats);
            booking.setBookingTime(LocalDateTime.now());
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);

            // Safe concurrency reservation via transactional row-level lock in BookingDAO
            int generatedId = bookingDAO.createBooking(booking);
            booking.setBookingId(generatedId);
            return booking;

        } catch (SQLException e) {
            throw new RuntimeException("Database error booking ride: " + e.getMessage(), e);
        }
    }

    /**
     * Alias for bookRide implementing interface createBooking.
     */
    @Override
    public Booking createBooking(int rideId, int passengerId, int seatsBooked) throws IllegalArgumentException {
        return bookRide(rideId, passengerId, seatsBooked);
    }

    /**
     * Retrieve booking details by long bookingId.
     *
     * @param bookingId booking ID
     * @return Booking entity or null if not found
     */
    public Booking getBookingById(long bookingId) {
        if (bookingId <= 0 || bookingId > Integer.MAX_VALUE) {
            return null;
        }
        return findById((int) bookingId);
    }

    /**
     * Implementation of interface method findById.
     */
    @Override
    public Booking findById(int bookingId) {
        if (bookingId <= 0) {
            return null;
        }
        try {
            return bookingDAO.findById(bookingId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding booking: " + e.getMessage(), e);
        }
    }

    /**
     * Show a passenger's bookings by long passengerId.
     *
     * @param passengerId passenger user ID
     * @return list of bookings
     */
    public List<Booking> getBookingsByPassenger(long passengerId) {
        if (passengerId <= 0 || passengerId > Integer.MAX_VALUE) {
            return Collections.emptyList();
        }
        return getBookingsByPassenger((int) passengerId);
    }

    /**
     * Implementation of interface method getBookingsByPassenger.
     */
    @Override
    public List<Booking> getBookingsByPassenger(int passengerId) {
        if (passengerId <= 0) {
            return Collections.emptyList();
        }
        try {
            return bookingDAO.findBookingsByPassenger(passengerId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching bookings by passenger: " + e.getMessage(), e);
        }
    }

    /**
     * List bookings for a ride by long rideId.
     *
     * @param rideId ride ID
     * @return list of bookings
     */
    public List<Booking> getBookingsByRide(long rideId) {
        if (rideId <= 0 || rideId > Integer.MAX_VALUE) {
            return Collections.emptyList();
        }
        return getBookingsByRide((int) rideId);
    }

    /**
     * Implementation of interface method getBookingsByRide.
     */
    @Override
    public List<Booking> getBookingsByRide(int rideId) {
        if (rideId <= 0) {
            return Collections.emptyList();
        }
        try {
            return bookingDAO.findBookingsByRide(rideId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching bookings by ride: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel a booking: verify passenger ownership and update booking status.
     * Enforces the business rule that only the relevant passenger can cancel the booking.
     *
     * @param bookingId booking ID to cancel
     * @param passengerId user ID of the requesting passenger
     * @return true if cancelled successfully
     * @throws IllegalArgumentException if booking not found or user is not the booking passenger
     */
    public boolean cancelBooking(long bookingId, long passengerId) {
        if (bookingId <= 0 || passengerId <= 0) {
            throw new IllegalArgumentException("Invalid booking or passenger ID.");
        }

        try {
            Booking booking = bookingDAO.findById((int) bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking with ID " + bookingId + " does not exist.");
            }

            // Business rule: Only the relevant passenger can cancel it
            if (booking.getPassengerId() != passengerId) {
                throw new IllegalArgumentException("Only the passenger who booked this ride can cancel it.");
            }

            if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                return false;
            }

            return bookingDAO.cancelBooking((int) bookingId);

        } catch (SQLException e) {
            throw new RuntimeException("Database error cancelling booking: " + e.getMessage(), e);
        }
    }

    /**
     * Implementation of interface method cancelBooking.
     */
    @Override
    public boolean cancelBooking(int bookingId) {
        if (bookingId <= 0) {
            return false;
        }
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null || booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                return false;
            }
            return bookingDAO.cancelBooking(bookingId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error cancelling booking: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate remaining seats for a ride by long rideId.
     *
     * @param rideId ride ID
     * @return remaining seat count
     */
    public int getAvailableSeats(long rideId) {
        if (rideId <= 0 || rideId > Integer.MAX_VALUE) {
            return 0;
        }
        return getAvailableSeats((int) rideId);
    }

    /**
     * Calculate remaining seats for a ride by integer rideId.
     *
     * @param rideId ride ID
     * @return remaining seat count
     */
    public int getAvailableSeats(int rideId) {
        if (rideId <= 0) {
            return 0;
        }
        try {
            return bookingDAO.getRemainingSeatsForRide(rideId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error calculating available seats: " + e.getMessage(), e);
        }
    }

    /**
     * Total confirmed seats booked for a ride by long rideId.
     */
    public int getConfirmedSeatsForRide(long rideId) {
        if (rideId <= 0 || rideId > Integer.MAX_VALUE) {
            return 0;
        }
        return getConfirmedSeatsForRide((int) rideId);
    }

    /**
     * Implementation of interface method getConfirmedSeatsForRide.
     */
    @Override
    public int getConfirmedSeatsForRide(int rideId) {
        if (rideId <= 0) {
            return 0;
        }
        try {
            return bookingDAO.getConfirmedSeatsForRide(rideId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching confirmed seats: " + e.getMessage(), e);
        }
    }
}
