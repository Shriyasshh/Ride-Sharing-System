package com.ridesharing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.util.DBConnection;

public class BookingDAO {

    /**
     * Creates a new booking using a database transaction with exclusive row-level locking
     * (`FOR UPDATE`) to prevent concurrent overbooking.
     *
     * Validates:
     * 1. Ride existence and AVAILABLE status.
     * 2. Driver cannot book their own ride.
     * 3. Number of seats requested > 0 and <= remaining seats.
     * 4. Reactivates a prior CANCELLED booking if the passenger previously booked this ride,
     *    satisfying the UNIQUE(ride_id, passenger_id) database constraint.
     *
     * Automatically rolls back the transaction on any failure using try-with-resources.
     *
     * @param booking the Booking entity to create
     * @return the generated or updated booking_id
     * @throws SQLException if validation fails or a database error occurs
     */
    public int createBooking(Booking booking) throws SQLException {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (booking.getSeatsBooked() <= 0) {
            throw new IllegalArgumentException("Seats booked must be greater than 0");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Lock ride row exclusively to serialize concurrent booking attempts
                String lockRideSql = "SELECT ride_id, driver_id, no_of_seats, ride_status "
                                   + "FROM rides WHERE ride_id = ? FOR UPDATE";
                int totalSeats;
                int driverId;
                String rideStatus;

                try (PreparedStatement lockStmt = conn.prepareStatement(lockRideSql)) {
                    lockStmt.setInt(1, booking.getRideId());
                    try (ResultSet rs = lockStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Ride with ID " + booking.getRideId() + " not found.");
                        }
                        driverId = rs.getInt("driver_id");
                        totalSeats = rs.getInt("no_of_seats");
                        rideStatus = rs.getString("ride_status");
                    }
                }

                // 2. Validate ride status
                if (!Ride.RideStatus.AVAILABLE.name().equalsIgnoreCase(rideStatus)) {
                    throw new SQLException("Ride is not available for booking (Current status: " + rideStatus + ").");
                }

                // 3. Driver cannot book their own ride
                if (driverId == booking.getPassengerId()) {
                    throw new SQLException("Driver cannot book their own ride.");
                }

                // 4. Calculate confirmed seats within the transaction lock
                String seatsSql = "SELECT COALESCE(SUM(seats_booked), 0) AS confirmed_seats "
                                + "FROM bookings WHERE ride_id = ? AND booking_status = 'CONFIRMED'";
                int confirmedSeats = 0;
                try (PreparedStatement seatsStmt = conn.prepareStatement(seatsSql)) {
                    seatsStmt.setInt(1, booking.getRideId());
                    try (ResultSet rs = seatsStmt.executeQuery()) {
                        if (rs.next()) {
                            confirmedSeats = rs.getInt("confirmed_seats");
                        }
                    }
                }

                int remainingSeats = totalSeats - confirmedSeats;
                if (booking.getSeatsBooked() > remainingSeats) {
                    throw new SQLException("Cannot book " + booking.getSeatsBooked()
                            + " seat(s). Only " + remainingSeats + " seat(s) remaining.");
                }

                // 5. Check if passenger already has a record for this ride (handles UNIQUE constraint)
                String checkExistingSql = "SELECT booking_id, booking_status FROM bookings "
                                        + "WHERE ride_id = ? AND passenger_id = ?";
                Integer existingBookingId = null;
                String existingStatus = null;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkExistingSql)) {
                    checkStmt.setInt(1, booking.getRideId());
                    checkStmt.setInt(2, booking.getPassengerId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            existingBookingId = rs.getInt("booking_id");
                            existingStatus = rs.getString("booking_status");
                        }
                    }
                }

                int generatedBookingId;
                LocalDateTime now = booking.getBookingTime() != null ? booking.getBookingTime() : LocalDateTime.now();

                if (existingBookingId != null) {
                    if ("CONFIRMED".equalsIgnoreCase(existingStatus)) {
                        throw new SQLException("Passenger already has an active CONFIRMED booking for this ride.");
                    }
                    // Reactivate prior CANCELLED booking
                    String updateBookingSql = "UPDATE bookings SET seats_booked = ?, booking_status = ?, booking_time = ? "
                                            + "WHERE booking_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateBookingSql)) {
                        updateStmt.setInt(1, booking.getSeatsBooked());
                        updateStmt.setString(2, Booking.BookingStatus.CONFIRMED.name());
                        updateStmt.setTimestamp(3, Timestamp.valueOf(now));
                        updateStmt.setInt(4, existingBookingId);
                        updateStmt.executeUpdate();
                    }
                    generatedBookingId = existingBookingId;
                } else {
                    // 6. Insert new booking
                    String insertSql = "INSERT INTO bookings (ride_id, passenger_id, seats_booked, booking_status, booking_time) "
                                     + "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setInt(1, booking.getRideId());
                        insertStmt.setInt(2, booking.getPassengerId());
                        insertStmt.setInt(3, booking.getSeatsBooked());
                        String status = booking.getBookingStatus() != null
                                ? booking.getBookingStatus().name()
                                : Booking.BookingStatus.CONFIRMED.name();
                        insertStmt.setString(4, status);
                        insertStmt.setTimestamp(5, Timestamp.valueOf(now));

                        int rows = insertStmt.executeUpdate();
                        if (rows == 0) {
                            throw new SQLException("Creating booking failed, no rows inserted.");
                        }

                        try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                generatedBookingId = keys.getInt(1);
                            } else {
                                throw new SQLException("Creating booking failed, no ID obtained.");
                            }
                        }
                    }
                }

                conn.commit();
                booking.setBookingId(generatedBookingId);
                booking.setBookingTime(now);
                booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
                return generatedBookingId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Convenience method to create a booking with parameters.
     */
    public Booking bookRide(int rideId, int passengerId, int seatsBooked) throws SQLException {
        Booking booking = new Booking(rideId, passengerId, seatsBooked);
        int id = createBooking(booking);
        booking.setBookingId(id);
        return booking;
    }

    /**
     * Find a booking by its ID. Returns null if not found.
     *
     * @param bookingId the ID of the booking
     * @return Booking entity or null if not found
     * @throws SQLException if a database access error occurs
     */
    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find all bookings made by a specific passenger ordered by booking time descending.
     * Returns an empty list if none found.
     *
     * @param passengerId the user ID of the passenger
     * @return list of bookings made by the passenger
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> findBookingsByPassenger(int passengerId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE passenger_id = ? ORDER BY booking_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, passengerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        }
        return list;
    }

    /**
     * Alias for findBookingsByPassenger.
     */
    public List<Booking> getBookingsByPassenger(int passengerId) throws SQLException {
        return findBookingsByPassenger(passengerId);
    }

    /**
     * Find all bookings for a particular ride ordered by booking time ascending.
     * Returns an empty list if none found.
     *
     * @param rideId the ID of the ride
     * @return list of bookings for the ride
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> findBookingsByRide(int rideId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE ride_id = ? ORDER BY booking_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        }
        return list;
    }

    /**
     * Alias for findBookingsByRide.
     */
    public List<Booking> getBookingsByRide(int rideId) throws SQLException {
        return findBookingsByRide(rideId);
    }

    /**
     * Find all bookings with passenger contact details for a specific ride by joining
     * bookings, rides, and users tables.
     * Excludes sensitive fields like password hashes or government IDs.
     *
     * @param rideId ID of the ride
     * @return list of bookings with passenger details populated
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> findBookingsWithPassengersByRide(int rideId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.ride_id, b.passenger_id, b.booking_time, b.seats_booked, b.booking_status, "
                   + "u.name AS passenger_name, u.phone_no AS passenger_phone, u.email AS passenger_email "
                   + "FROM bookings b "
                   + "JOIN rides r ON b.ride_id = r.ride_id "
                   + "JOIN users u ON b.passenger_id = u.user_id "
                   + "WHERE b.ride_id = ? "
                   + "ORDER BY b.booking_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Booking booking = mapBooking(rs);
                    booking.setPassengerName(rs.getString("passenger_name"));
                    booking.setPassengerPhone(rs.getString("passenger_phone"));
                    booking.setPassengerEmail(rs.getString("passenger_email"));
                    list.add(booking);
                }
            }
        }
        return list;
    }

    /**
     * Retrieve all bookings in the system ordered by booking_id ascending.
     * Returns an empty list if none found.
     *
     * @return list of all Bookings
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY booking_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        }
        return list;
    }

    /**
     * Cancel a booking by updating its status to CANCELLED.
     *
     * @param bookingId the ID of the booking to cancel
     * @return true if updated, false if booking not found
     * @throws SQLException if a database access error occurs
     */
    public boolean cancelBooking(int bookingId) throws SQLException {
        return updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);
    }

    /**
     * Update the status of a booking.
     *
     * @param bookingId the ID of the booking
     * @param status the new BookingStatus
     * @return true if updated, false if booking not found
     * @throws SQLException if a database access error occurs
     */
    public boolean updateBookingStatus(int bookingId, Booking.BookingStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("BookingStatus cannot be null");
        }
        String sql = "UPDATE bookings SET booking_status = ? WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, bookingId);

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Calculate the total confirmed seats booked for a ride.
     *
     * @param rideId the ID of the ride
     * @return sum of seats_booked for CONFIRMED bookings (>= 0)
     * @throws SQLException if a database access error occurs
     */
    public int getConfirmedSeatsForRide(int rideId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(seats_booked), 0) AS total_confirmed "
                   + "FROM bookings WHERE ride_id = ? AND booking_status = 'CONFIRMED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_confirmed");
                }
            }
        }
        return 0;
    }

    /**
     * Check ride availability and remaining seats before booking.
     *
     * @param rideId the ID of the ride
     * @return remaining seat count (>= 0)
     * @throws SQLException if a database access error occurs
     */
    public int getRemainingSeatsForRide(int rideId) throws SQLException {
        String sql = "SELECT (r.no_of_seats - COALESCE(SUM(b.seats_booked), 0)) AS remaining_seats "
                   + "FROM rides r "
                   + "LEFT JOIN bookings b ON r.ride_id = b.ride_id AND b.booking_status = 'CONFIRMED' "
                   + "WHERE r.ride_id = ? "
                   + "GROUP BY r.ride_id, r.no_of_seats";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Math.max(0, rs.getInt("remaining_seats"));
                }
            }
        }
        return 0;
    }

    /**
     * Check if a ride is AVAILABLE and has at least requiredSeats available.
     *
     * @param rideId the ID of the ride
     * @param requiredSeats number of seats required
     * @return true if available with sufficient seats, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isRideAvailable(int rideId, int requiredSeats) throws SQLException {
        if (requiredSeats <= 0) return false;

        String sql = "SELECT r.ride_status, (r.no_of_seats - COALESCE(SUM(b.seats_booked), 0)) AS remaining_seats "
                   + "FROM rides r "
                   + "LEFT JOIN bookings b ON r.ride_id = b.ride_id AND b.booking_status = 'CONFIRMED' "
                   + "WHERE r.ride_id = ? "
                   + "GROUP BY r.ride_id, r.no_of_seats, r.ride_status";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("ride_status");
                    int remaining = rs.getInt("remaining_seats");
                    return Ride.RideStatus.AVAILABLE.name().equalsIgnoreCase(status) && remaining >= requiredSeats;
                }
            }
        }
        return false;
    }

    /**
     * Delete a booking by ID.
     *
     * @param bookingId the ID of the booking to delete
     * @return true if deleted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean deleteBooking(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Map a ResultSet row to a Booking entity.
     */
    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setRideId(rs.getInt("ride_id"));
        booking.setPassengerId(rs.getInt("passenger_id"));

        Timestamp ts = rs.getTimestamp("booking_time");
        if (ts != null) {
            booking.setBookingTime(ts.toLocalDateTime());
        }

        booking.setSeatsBooked(rs.getInt("seats_booked"));

        String status = rs.getString("booking_status");
        if (status != null) {
            try {
                booking.setBookingStatus(Booking.BookingStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
            }
        }
        return booking;
    }
}
