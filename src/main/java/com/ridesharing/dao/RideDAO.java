package com.ridesharing.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ridesharing.model.Ride;
import com.ridesharing.util.DBConnection;

public class RideDAO {

    /**
     * DTO containing a Ride and its dynamically computed remaining seat count.
     */
    public static class RideWithRemainingSeats {
        private final Ride ride;
        private final int remainingSeats;

        public RideWithRemainingSeats(Ride ride, int remainingSeats) {
            this.ride = ride;
            this.remainingSeats = remainingSeats;
        }

        public Ride getRide() {
            return ride;
        }

        public int getRemainingSeats() {
            return remainingSeats;
        }

        @Override
        public String toString() {
            return "RideWithRemainingSeats{"
                    + "rideId=" + (ride != null ? ride.getRideId() : 0)
                    + ", source='" + (ride != null ? ride.getSourceAddress() : "") + '\''
                    + ", destination='" + (ride != null ? ride.getDestinationAddress() : "") + '\''
                    + ", totalSeats=" + (ride != null ? ride.getNoOfSeats() : 0)
                    + ", remainingSeats=" + remainingSeats
                    + ", status=" + (ride != null ? ride.getRideStatus() : null)
                    + '}';
        }
    }

    /**
     * Create a new ride and retrieve its generated ID.
     *
     * @param ride the Ride entity to persist
     * @return the generated ride_id
     * @throws SQLException if a database access error occurs
     */
    public int createRide(Ride ride) throws SQLException {
        String sql = "INSERT INTO rides (driver_id, vehicle_id, no_of_seats, source_address, "
                   + "destination_address, departure_time, cost_per_seat, ride_status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, ride.getDriverId());
            statement.setInt(2, ride.getVehicleId());
            statement.setInt(3, ride.getNoOfSeats());
            statement.setString(4, ride.getSourceAddress());
            statement.setString(5, ride.getDestinationAddress());
            statement.setTimestamp(6, ride.getDepartureTime() != null ? Timestamp.valueOf(ride.getDepartureTime()) : null);
            statement.setBigDecimal(7, ride.getCostPerSeat());
            statement.setString(8, ride.getRideStatus() != null ? ride.getRideStatus().name() : Ride.RideStatus.AVAILABLE.name());

            int rowInserted = statement.executeUpdate();
            if (rowInserted == 0) {
                throw new SQLException("Creating ride failed, no rows affected.");
            }

            try (ResultSet key = statement.getGeneratedKeys()) {
                if (key.next()) {
                    int rideId = key.getInt(1);
                    ride.setRideId(rideId);
                    return rideId;
                }
            }
        }
        throw new SQLException("Creating ride failed, ride ID was not generated.");
    }

    /**
     * Alias for createRide.
     */
    public int addRide(Ride ride) throws SQLException {
        return createRide(ride);
    }

    /**
     * Find a ride by its ID.
     *
     * @param rideId the ID of the ride
     * @return the Ride entity or null if not found
     * @throws SQLException if a database ac
     * cess error occurs
     */
    public Ride findById(int rideId) throws SQLException {
        String sql = "SELECT * FROM rides WHERE ride_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapRide(result);
                }
            }
        }
        return null;
    }

    /**
     * Alias for findById.
     */
    public Ride findRideById(int rideId) throws SQLException {
        return findById(rideId);
    }

    /**
     * Find all rides published by a particular driver.
     *
     * @param driverId the user ID of the driver
     * @return list of rides published by the driver
     * @throws SQLException if a database access error occurs
     */
    public List<Ride> findRidesByDriver(int driverId) throws SQLException {
        List<Ride> rides = new ArrayList<>();
        String sql = "SELECT * FROM rides WHERE driver_id = ? ORDER BY departure_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, driverId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rides.add(mapRide(result));
                }
            }
        }
        return rides;
    }

    /**
     * Alias for findRidesByDriver.
     */
    public List<Ride> getRidesByDriver(int driverId) throws SQLException {
        return findRidesByDriver(driverId);
    }

    /**
     * Search available rides by source, destination, and departure date.
     * All criteria are optional (pass null or empty string to ignore).
     *
     * @param source partial match for source address (case-insensitive)
     * @param destination partial match for destination address (case-insensitive)
     * @param departureDate departure date filter
     * @return list of matching available rides
     * @throws SQLException if a database access error occurs
     */
    public List<Ride> searchAvailableRides(String source, String destination, LocalDate departureDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM rides WHERE ride_status = ?");
        List<Object> params = new ArrayList<>();
        params.add(Ride.RideStatus.AVAILABLE.name());

        if (source != null && !source.trim().isEmpty()) {
            sql.append(" AND LOWER(source_address) LIKE ?");
            params.add("%" + source.trim().toLowerCase() + "%");
        }

        if (destination != null && !destination.trim().isEmpty()) {
            sql.append(" AND LOWER(destination_address) LIKE ?");
            params.add("%" + destination.trim().toLowerCase() + "%");
        }

        if (departureDate != null) {
            sql.append(" AND departure_time >= ? AND departure_time < ?");
            params.add(Timestamp.valueOf(departureDate.atStartOfDay()));
            params.add(Timestamp.valueOf(departureDate.plusDays(1).atStartOfDay()));
        }

        sql.append(" ORDER BY departure_time ASC");

        List<Ride> rides = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rides.add(mapRide(result));
                }
            }
        }
        return rides;
    }

    /**
     * Search available rides filtering by source, destination, departure date, and minimum remaining seats.
     *
     * @param source partial match for source address (optional)
     * @param destination partial match for destination address (optional)
     * @param departureDate departure date filter (optional)
     * @param minSeats minimum available seats required
     * @return list of rides that have at least minSeats available
     * @throws SQLException if a database access error occurs
     */
    public List<Ride> searchAvailableRides(String source, String destination, LocalDate departureDate, int minSeats) throws SQLException {
        List<RideWithRemainingSeats> results = retrieveAvailableRidesWithRemainingSeats(source, destination, departureDate, minSeats);
        List<Ride> rides = new ArrayList<>();
        for (RideWithRemainingSeats item : results) {
            rides.add(item.getRide());
        }
        return rides;
    }

    /**
     * Calculate remaining seats for a specific ride.
     * Total seats minus confirmed bookings. Cancelled bookings do not reduce seats.
     *
     * @param rideId the ID of the ride
     * @return number of remaining seats (>= 0)
     * @throws SQLException if a database access error occurs
     */
    public int getRemainingSeats(int rideId) throws SQLException {
        String sql = "SELECT (r.no_of_seats - COALESCE(SUM(b.seats_booked), 0)) AS remaining_seats "
                   + "FROM rides r "
                   + "LEFT JOIN bookings b ON r.ride_id = b.ride_id AND b.booking_status = 'CONFIRMED' "
                   + "WHERE r.ride_id = ? "
                   + "GROUP BY r.ride_id, r.no_of_seats";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Math.max(0, result.getInt("remaining_seats"));
                }
            }
        }
        return 0;
    }

    /**
     * Retrieve available rides along with their calculated remaining seats.
     * Only rides with status 'AVAILABLE' and remaining seats >= minSeats are returned.
     *
     * @param source partial match for source address (optional)
     * @param destination partial match for destination address (optional)
     * @param departureDate departure date filter (optional)
     * @param minSeats minimum available seats required (defaults to 1 if <= 0)
     * @return list of RideWithRemainingSeats pairs
     * @throws SQLException if a database access error occurs
     */
    public List<RideWithRemainingSeats> retrieveAvailableRidesWithRemainingSeats(
            String source, String destination, LocalDate departureDate, int minSeats) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.ride_id, r.driver_id, r.vehicle_id, r.no_of_seats, ")
           .append("r.source_address, r.destination_address, r.departure_time, ")
           .append("r.cost_per_seat, r.ride_status, ")
           .append("(r.no_of_seats - COALESCE(SUM(b.seats_booked), 0)) AS remaining_seats ")
           .append("FROM rides r ")
           .append("LEFT JOIN bookings b ON r.ride_id = b.ride_id AND b.booking_status = 'CONFIRMED' ")
           .append("WHERE r.ride_status = ? ");

        List<Object> params = new ArrayList<>();
        params.add(Ride.RideStatus.AVAILABLE.name());

        if (source != null && !source.trim().isEmpty()) {
            sql.append(" AND LOWER(r.source_address) LIKE ? ");
            params.add("%" + source.trim().toLowerCase() + "%");
        }

        if (destination != null && !destination.trim().isEmpty()) {
            sql.append(" AND LOWER(r.destination_address) LIKE ? ");
            params.add("%" + destination.trim().toLowerCase() + "%");
        }

        if (departureDate != null) {
            sql.append(" AND r.departure_time >= ? AND r.departure_time < ? ");
            params.add(Timestamp.valueOf(departureDate.atStartOfDay()));
            params.add(Timestamp.valueOf(departureDate.plusDays(1).atStartOfDay()));
        }

        sql.append("GROUP BY r.ride_id, r.driver_id, r.vehicle_id, r.no_of_seats, ")
           .append("r.source_address, r.destination_address, r.departure_time, ")
           .append("r.cost_per_seat, r.ride_status ");

        int requiredMinSeats = minSeats > 0 ? minSeats : 1;
        sql.append("HAVING (r.no_of_seats - COALESCE(SUM(b.seats_booked), 0)) >= ? ");
        params.add(requiredMinSeats);

        sql.append("ORDER BY r.departure_time ASC");

        List<RideWithRemainingSeats> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Ride ride = mapRide(result);
                    int remainingSeats = Math.max(0, result.getInt("remaining_seats"));
                    results.add(new RideWithRemainingSeats(ride, remainingSeats));
                }
            }
        }
        return results;
    }

    /**
     * Retrieve all available rides with remaining seats calculation (at least 1 seat available).
     *
     * @return list of RideWithRemainingSeats
     * @throws SQLException if a database access error occurs
     */
    public List<RideWithRemainingSeats> getAvailableRidesWithRemainingSeats() throws SQLException {
        return retrieveAvailableRidesWithRemainingSeats(null, null, null, 1);
    }

    /**
     * Update ride status using Ride.RideStatus enum.
     *
     * @param rideId the ID of the ride to update
     * @param status the new RideStatus
     * @return true if updated successfully, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean updateRideStatus(int rideId, Ride.RideStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("RideStatus cannot be null");
        }
        String sql = "UPDATE rides SET ride_status = ? WHERE ride_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, rideId);

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Convenience method to cancel a ride.
     *
     * @param rideId the ID of the ride to cancel
     * @return true if cancelled successfully, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean cancelRide(int rideId) throws SQLException {
        return updateRideStatus(rideId, Ride.RideStatus.CANCELLED);
    }

    /**
     * Retrieve all rides in the system ordered by departure time descending.
     *
     * @return list of all rides
     * @throws SQLException if a database access error occurs
     */
    public List<Ride> getAllRides() throws SQLException {
        List<Ride> rides = new ArrayList<>();
        String sql = "SELECT * FROM rides ORDER BY departure_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                rides.add(mapRide(result));
            }
        }
        return rides;
    }

    /**
     * Delete a ride by ID.
     *
     * @param rideId the ID of the ride to delete
     * @return true if deleted, false if not found
     * @throws SQLException if a database access error occurs
     */
    public boolean deleteRide(int rideId) throws SQLException {
        String sql = "DELETE FROM rides WHERE ride_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, rideId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Check whether a vehicle is referenced by any rides.
     *
     * @param vehicleId vehicle ID to check
     * @return true if vehicle is associated with at least one ride, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isVehicleUsedInRides(int vehicleId) throws SQLException {
        String sql = "SELECT 1 FROM rides WHERE vehicle_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, vehicleId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * Find all rides associated with a specific vehicle.
     *
     * @param vehicleId vehicle ID
     * @return list of rides
     * @throws SQLException if a database error occurs
     */
    public List<Ride> findRidesByVehicle(int vehicleId) throws SQLException {
        List<Ride> rides = new ArrayList<>();
        String sql = "SELECT * FROM rides WHERE vehicle_id = ? ORDER BY departure_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, vehicleId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rides.add(mapRide(result));
                }
            }
        }
        return rides;
    }

    /**
     * Map a ResultSet row to a Ride entity.
     */
    private Ride mapRide(ResultSet result) throws SQLException {
        Ride ride = new Ride();
        ride.setRideId(result.getInt("ride_id"));
        ride.setDriverId(result.getInt("driver_id"));
        ride.setVehicleId(result.getInt("vehicle_id"));
        ride.setNoOfSeats(result.getInt("no_of_seats"));
        ride.setSourceAddress(result.getString("source_address"));
        ride.setDestinationAddress(result.getString("destination_address"));

        Timestamp ts = result.getTimestamp("departure_time");
        if (ts != null) {
            ride.setDepartureTime(ts.toLocalDateTime());
        }

        ride.setCostPerSeat(result.getBigDecimal("cost_per_seat"));

        String statusStr = result.getString("ride_status");
        if (statusStr != null) {
            try {
                ride.setRideStatus(Ride.RideStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                ride.setRideStatus(Ride.RideStatus.AVAILABLE);
            }
        }
        return ride;
    }
}
