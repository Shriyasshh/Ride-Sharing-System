package com.ridesharing.service.mock;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared mock data store that persists state across screens.
 * All mock services read/write to the same lists here so changes
 * (bookings, ride publishing, vehicle additions) are immediately
 * reflected everywhere in the UI.
 */
public final class MockDataStore {

    private static MockDataStore instance;

    private final List<User> users = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Ride> rides = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    private int nextUserId = 100;
    private int nextVehicleId = 100;
    private int nextRideId = 100;
    private int nextBookingId = 100;

    private User currentUser;

    private MockDataStore() {
        seedData();
    }

    public static synchronized MockDataStore getInstance() {
        if (instance == null) {
            instance = new MockDataStore();
        }
        return instance;
    }

    /** Reset data store (useful for logout) */
    public void reset() {
        users.clear();
        vehicles.clear();
        rides.clear();
        bookings.clear();
        nextUserId = 100;
        nextVehicleId = 100;
        nextRideId = 100;
        nextBookingId = 100;
        currentUser = null;
        seedData();
    }

    // ── Accessors ──

    public List<User> getUsers() { return users; }
    public List<Vehicle> getVehicles() { return vehicles; }
    public List<Ride> getRides() { return rides; }
    public List<Booking> getBookings() { return bookings; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public int nextUserId() { return nextUserId++; }
    public int nextVehicleId() { return nextVehicleId++; }
    public int nextRideId() { return nextRideId++; }
    public int nextBookingId() { return nextBookingId++; }

    // ── Seed Data ──

    private void seedData() {
        // ── Users ──
        User u1 = createUser(1, "Aarav Sharma", "9876543210", "Male",
                "aarav@example.com", "AADHAAR-1234", "Friendly", "demo123");
        User u2 = createUser(2, "Priya Patel", "9876543211", "Female",
                "priya@example.com", "AADHAAR-5678", "Calm", "demo123");
        User u3 = createUser(3, "Rohan Gupta", "9876543212", "Male",
                "rohan@example.com", "AADHAAR-9012", "Punctual", "demo123");
        User u4 = createUser(4, "Ananya Singh", "9876543213", "Female",
                "ananya@example.com", "AADHAAR-3456", "Helpful", "demo123");
        User u5 = createUser(5, "Vikram Reddy", "9876543214", "Male",
                "vikram@example.com", "AADHAAR-7890", "Experienced", "demo123");

        // ── Vehicles ──
        Vehicle v1 = createVehicle(1, "Maruti Suzuki Swift", 1);
        Vehicle v2 = createVehicle(2, "Hyundai i20", 1);
        Vehicle v3 = createVehicle(3, "Tata Nexon EV", 2);
        Vehicle v4 = createVehicle(4, "Honda City", 3);
        Vehicle v5 = createVehicle(5, "Toyota Innova", 4);
        Vehicle v6 = createVehicle(6, "Mahindra XUV700", 5);

        // ── Rides ── (mix of future rides for demo)
        LocalDateTime now = LocalDateTime.now();

        createRide(1, 2, 3, 3, "Bangalore", "Mysore",
                now.plusHours(4), new BigDecimal("250.00"), Ride.RideStatus.AVAILABLE);
        createRide(2, 3, 4, 4, "Mumbai", "Pune",
                now.plusDays(1).withHour(8).withMinute(0), new BigDecimal("350.00"), Ride.RideStatus.AVAILABLE);
        createRide(3, 4, 5, 6, "Delhi", "Agra",
                now.plusDays(1).withHour(6).withMinute(30), new BigDecimal("400.00"), Ride.RideStatus.AVAILABLE);
        createRide(4, 5, 6, 4, "Chennai", "Pondicherry",
                now.plusDays(2).withHour(9).withMinute(0), new BigDecimal("200.00"), Ride.RideStatus.AVAILABLE);
        createRide(5, 1, 1, 3, "Hyderabad", "Warangal",
                now.plusHours(6), new BigDecimal("180.00"), Ride.RideStatus.AVAILABLE);
        createRide(6, 1, 2, 4, "Bangalore", "Chennai",
                now.plusDays(3).withHour(7).withMinute(0), new BigDecimal("500.00"), Ride.RideStatus.AVAILABLE);
        createRide(7, 2, 3, 2, "Pune", "Mumbai",
                now.minusDays(1), new BigDecimal("300.00"), Ride.RideStatus.IN_PROGRESS);
        createRide(8, 3, 4, 3, "Jaipur", "Delhi",
                now.minusDays(3), new BigDecimal("450.00"), Ride.RideStatus.CANCELLED);

        // ── Bookings ──
        createBooking(1, 1, 1, 2, now.minusHours(2), Booking.BookingStatus.CONFIRMED); // user 1 booked ride 1
        createBooking(2, 2, 3, 1, now.minusHours(5), Booking.BookingStatus.CONFIRMED); // user 3 booked ride 2
        createBooking(3, 3, 1, 2, now.minusDays(1), Booking.BookingStatus.CONFIRMED);  // user 1 booked ride 3
        createBooking(4, 5, 2, 1, now.minusHours(1), Booking.BookingStatus.CONFIRMED); // user 2 booked ride 5
        createBooking(5, 4, 3, 1, now.minusDays(2), Booking.BookingStatus.CANCELLED);  // cancelled booking
    }

    // ── Helper Creators ──

    private User createUser(int id, String name, String phone, String gender,
                            String email, String govtId, String behaviour, String password) {
        User u = new User(phone, name, gender, email, govtId, behaviour, password);
        u.setUserId(id);
        users.add(u);
        if (id >= nextUserId) nextUserId = id + 1;
        return u;
    }

    private Vehicle createVehicle(int id, String model, int ownerId) {
        Vehicle v = new Vehicle(id, model, ownerId);
        vehicles.add(v);
        if (id >= nextVehicleId) nextVehicleId = id + 1;
        return v;
    }

    private Ride createRide(int id, int driverId, int vehicleId, int seats,
                            String source, String dest, LocalDateTime departure,
                            BigDecimal cost, Ride.RideStatus status) {
        Ride r = new Ride(driverId, vehicleId, seats, source, dest, departure, cost);
        r.setRideId(id);
        r.setRideStatus(status);
        rides.add(r);
        if (id >= nextRideId) nextRideId = id + 1;
        return r;
    }

    private Booking createBooking(int id, int rideId, int passengerId, int seats,
                                  LocalDateTime time, Booking.BookingStatus status) {
        Booking b = new Booking(rideId, passengerId, seats);
        b.setBookingId(id);
        b.setBookingTime(time);
        b.setBookingStatus(status);
        bookings.add(b);
        if (id >= nextBookingId) nextBookingId = id + 1;
        return b;
    }
}
