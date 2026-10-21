import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.ridesharing.dao.BookingDAO;
import com.ridesharing.dao.RideDAO;
import com.ridesharing.dao.UserDAO;
import com.ridesharing.dao.VehicleDAO;
import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.util.DBConnection;

public class DaoTest {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        RideDAO rideDAO = new RideDAO();
        BookingDAO bookingDAO = new BookingDAO();

        try {
            System.out.println("=================================================================");
            System.out.println("           COMPREHENSIVE ALL-DAO INTEGRITY & CONSTRAINT TESTS    ");
            System.out.println("=================================================================");

            // -----------------------------------------------------------------
            // SECTION 1: USER DAO TESTS
            // -----------------------------------------------------------------
            System.out.println("\n[SECTION 1] USER DAO & CONSTRAINTS");
            User driver = createTestUser(userDAO, "Driver");
            int driverId = driver.getUserId();
            System.out.println("1.1 User registered with generated PK: " + driverId);
            assert driverId > 0 : "Generated user_id must be > 0";

            // Find by ID, Phone, Email
            User foundUser = userDAO.findById(driverId);
            assert foundUser != null : "User must be found by ID";
            assert foundUser.getEmail().equals(driver.getEmail()) : "Email must match";
            System.out.println("1.2 Found user by ID: " + foundUser.getName() + " (" + foundUser.getEmail() + ")");

            User foundByPhone = userDAO.findByPhoneNo(driver.getPhoneNo());
            assert foundByPhone != null && foundByPhone.getUserId() == driverId : "Must find by phone";

            User foundByEmail = userDAO.findByEmail(driver.getEmail());
            assert foundByEmail != null && foundByEmail.getUserId() == driverId : "Must find by email";
            System.out.println("1.3 Found user by phone & email successfully");

            // Missing record handling -> returns null
            User nonExistentUser = userDAO.findById(9999999);
            assert nonExistentUser == null : "Missing user must return null";
            System.out.println("1.4 Missing user returns null: true");

            // Test UNIQUE constraint on phone_no
            System.out.println("1.5 Testing UNIQUE constraint on users.phone_no");
            try {
                User dupPhoneUser = new User();
                dupPhoneUser.setPhoneNo(driver.getPhoneNo()); // duplicate!
                dupPhoneUser.setName("DupPhone");
                dupPhoneUser.setGender("Male");
                dupPhoneUser.setEmail("diff_email_" + UUID.randomUUID() + "@example.com");
                dupPhoneUser.setGovtId("diff_govt_" + UUID.randomUUID());
                dupPhoneUser.setPasswordHash("HASH");
                userDAO.registerUser(dupPhoneUser);
                throw new AssertionError("Should have failed on duplicate phone number!");
            } catch (SQLException e) {
                System.out.println("    Caught expected UNIQUE violation on phone: " + e.getMessage());
            }

            // Test UNIQUE constraint on email
            System.out.println("1.6 Testing UNIQUE constraint on users.email");
            try {
                User dupEmailUser = new User();
                dupEmailUser.setPhoneNo("9" + String.valueOf(System.currentTimeMillis() + 8888).substring(3, 12));
                dupEmailUser.setName("DupEmail");
                dupEmailUser.setGender("Female");
                dupEmailUser.setEmail(driver.getEmail()); // duplicate!
                dupEmailUser.setGovtId("diff_govt_" + UUID.randomUUID());
                dupEmailUser.setPasswordHash("HASH");
                userDAO.registerUser(dupEmailUser);
                throw new AssertionError("Should have failed on duplicate email!");
            } catch (SQLException e) {
                System.out.println("    Caught expected UNIQUE violation on email: " + e.getMessage());
            }

            // Test UNIQUE constraint on govt_id
            System.out.println("1.7 Testing UNIQUE constraint on users.govt_id");
            try {
                User dupGovtUser = new User();
                dupGovtUser.setPhoneNo("9" + String.valueOf(System.currentTimeMillis() + 7777).substring(3, 12));
                dupGovtUser.setName("DupGovt");
                dupGovtUser.setGender("Other");
                dupGovtUser.setEmail("diff_email_" + UUID.randomUUID() + "@example.com");
                dupGovtUser.setGovtId(driver.getGovtId()); // duplicate!
                dupGovtUser.setPasswordHash("HASH");
                userDAO.registerUser(dupGovtUser);
                throw new AssertionError("Should have failed on duplicate govt_id!");
            } catch (SQLException e) {
                System.out.println("    Caught expected UNIQUE violation on govt_id: " + e.getMessage());
            }

            // -----------------------------------------------------------------
            // SECTION 2: VEHICLE DAO TESTS
            // -----------------------------------------------------------------
            System.out.println("\n[SECTION 2] VEHICLE DAO & FOREIGN KEY CONSTRAINTS");
            // Test FOREIGN KEY constraint: owner_id must exist in users
            System.out.println("2.1 Testing FOREIGN KEY constraint: vehicles.owner_id -> users(user_id)");
            try {
                Vehicle invalidOwnerVehicle = new Vehicle();
                invalidOwnerVehicle.setCarModel("Ghost Car");
                invalidOwnerVehicle.setOwnerId(9999999); // non-existent user
                vehicleDAO.addVehicle(invalidOwnerVehicle);
                throw new AssertionError("Should have failed on invalid foreign key owner_id!");
            } catch (SQLException e) {
                System.out.println("    Caught expected FOREIGN KEY violation on vehicles.owner_id: " + e.getMessage());
            }

            // Add valid vehicle with generated PK
            Vehicle vehicle = new Vehicle();
            vehicle.setCarModel("Tata Nexon EV");
            vehicle.setOwnerId(driverId);
            int vehicleId = vehicleDAO.addVehicle(vehicle);
            assert vehicleId > 0 : "Generated vehicle_id must be > 0";
            System.out.println("2.2 Vehicle added with generated PK: " + vehicleId);

            // Find by ID
            Vehicle foundVehicle = vehicleDAO.findVehicleById(vehicleId);
            assert foundVehicle != null && foundVehicle.getCarModel().equals("Tata Nexon EV") : "Must find vehicle by ID";
            System.out.println("2.3 Found vehicle by ID: " + foundVehicle.getCarModel());

            // Missing vehicle returns null
            assert vehicleDAO.findVehicleById(9999999) == null : "Missing vehicle must return null";
            System.out.println("2.4 Missing vehicle returns null: true");

            // Collection query returns non-empty list for owner, empty list for non-owner
            List<Vehicle> ownerVehicles = vehicleDAO.findVehiclesByOwner(driverId);
            assert !ownerVehicles.isEmpty() : "Owner vehicles must not be empty";
            List<Vehicle> nonOwnerVehicles = vehicleDAO.findVehiclesByOwner(9999999);
            assert nonOwnerVehicles.isEmpty() : "Non-owner vehicles must return empty list";
            System.out.println("2.5 Collection queries returned expected lists (found " + ownerVehicles.size() + ", empty list for unknown)");

            // -----------------------------------------------------------------
            // SECTION 3: RIDE DAO TESTS
            // -----------------------------------------------------------------
            System.out.println("\n[SECTION 3] RIDE DAO & CONSTRAINTS");
            // Test FOREIGN KEY: driver_id must exist in users
            System.out.println("3.1 Testing FOREIGN KEY constraint: rides.driver_id -> users(user_id)");
            try {
                Ride invalidDriverRide = createRideObject(9999999, vehicleId, 4);
                rideDAO.createRide(invalidDriverRide);
                throw new AssertionError("Should have failed on invalid driver_id foreign key!");
            } catch (SQLException e) {
                System.out.println("    Caught expected FOREIGN KEY violation on rides.driver_id: " + e.getMessage());
            }

            // Test FOREIGN KEY: vehicle_id must exist in vehicles
            System.out.println("3.2 Testing FOREIGN KEY constraint: rides.vehicle_id -> vehicles(vehicle_id)");
            try {
                Ride invalidVehicleRide = createRideObject(driverId, 9999999, 4);
                rideDAO.createRide(invalidVehicleRide);
                throw new AssertionError("Should have failed on invalid vehicle_id foreign key!");
            } catch (SQLException e) {
                System.out.println("    Caught expected FOREIGN KEY violation on rides.vehicle_id: " + e.getMessage());
            }

            // Create valid ride
            Ride validRide = createRideObject(driverId, vehicleId, 4);
            int rideId = rideDAO.createRide(validRide);
            assert rideId > 0 : "Generated ride_id must be > 0";
            System.out.println("3.3 Ride created with generated PK: " + rideId);

            // Find by ID
            Ride foundRide = rideDAO.findById(rideId);
            assert foundRide != null && foundRide.getNoOfSeats() == 4 : "Must find ride by ID";
            assert foundRide.getRideStatus() == Ride.RideStatus.AVAILABLE : "Status must be AVAILABLE";
            System.out.println("3.4 Found ride by ID: " + foundRide.getSourceAddress() + " -> " + foundRide.getDestinationAddress());

            // Missing ride returns null
            assert rideDAO.findById(9999999) == null : "Missing ride must return null";
            System.out.println("3.5 Missing ride returns null: true");

            // Search rides
            LocalDate rideDate = validRide.getDepartureTime().toLocalDate();
            List<Ride> searchedRides = rideDAO.searchAvailableRides("Koramangala", "Indiranagar", rideDate);
            assert !searchedRides.isEmpty() : "Searched rides must not be empty";
            List<Ride> emptySearch = rideDAO.searchAvailableRides("Nowhere", "UnknownCity", null);
            assert emptySearch.isEmpty() : "No matches must return empty list";
            System.out.println("3.6 Search available rides tested: exact match found, unknown query returns empty list");

            // Driver rides collection
            List<Ride> driverRides = rideDAO.findRidesByDriver(driverId);
            assert !driverRides.isEmpty() : "Driver rides must not be empty";
            assert rideDAO.findRidesByDriver(9999999).isEmpty() : "Unknown driver rides must return empty list";
            System.out.println("3.7 Driver rides collection verified: " + driverRides.size() + " ride(s)");

            // -----------------------------------------------------------------
            // SECTION 4: BOOKING DAO TESTS & TRANSACTIONS
            // -----------------------------------------------------------------
            System.out.println("\n[SECTION 4] BOOKING DAO, TRANSACTIONS & CONCURRENCY");
            User passenger1 = createTestUser(userDAO, "Passenger1");
            int passenger1Id = passenger1.getUserId();
            User passenger2 = createTestUser(userDAO, "Passenger2");
            int passenger2Id = passenger2.getUserId();

            // Test FOREIGN KEY: booking with non-existent ride_id
            System.out.println("4.1 Testing FOREIGN KEY constraint: bookings.ride_id -> rides(ride_id)");
            try {
                Booking invalidRideBooking = new Booking(9999999, passenger1Id, 1);
                bookingDAO.createBooking(invalidRideBooking);
                throw new AssertionError("Should have failed on invalid ride_id!");
            } catch (SQLException e) {
                System.out.println("    Caught expected error on non-existent ride_id: " + e.getMessage());
            }

            // Test FOREIGN KEY: booking with non-existent passenger_id
            System.out.println("4.2 Testing FOREIGN KEY constraint: bookings.passenger_id -> users(user_id)");
            try {
                Booking invalidPassBooking = new Booking(rideId, 9999999, 1);
                bookingDAO.createBooking(invalidPassBooking);
                throw new AssertionError("Should have failed on invalid passenger_id!");
            } catch (SQLException e) {
                System.out.println("    Caught expected error on non-existent passenger_id: " + e.getMessage());
            }

            // Check availability before booking
            assert bookingDAO.isRideAvailable(rideId, 2) : "Ride must be available for 2 seats";
            assert !bookingDAO.isRideAvailable(rideId, 5) : "Ride cannot be available for 5 seats (capacity 4)";
            assert bookingDAO.getRemainingSeatsForRide(rideId) == 4 : "Initial remaining seats must be 4";
            assert bookingDAO.getConfirmedSeatsForRide(rideId) == 0 : "Initial confirmed seats must be 0";
            System.out.println("4.3 Pre-booking availability verified (4 remaining, 0 confirmed)");

            // Create valid booking (2 seats)
            Booking booking1 = new Booking(rideId, passenger1Id, 2);
            int booking1Id = bookingDAO.createBooking(booking1);
            assert booking1Id > 0 : "Generated booking_id must be > 0";
            System.out.println("4.4 Booking created with generated PK: " + booking1Id);

            // Find by ID
            Booking foundBooking = bookingDAO.findById(booking1Id);
            assert foundBooking != null && foundBooking.getSeatsBooked() == 2 : "Must find booking by ID";
            assert foundBooking.getBookingStatus() == Booking.BookingStatus.CONFIRMED : "Status must be CONFIRMED";
            assert foundBooking.getBookingTime() != null : "Booking time must be populated";
            System.out.println("4.5 Found booking by ID: " + foundBooking.getBookingId() + ", Status=" + foundBooking.getBookingStatus());

            // Missing booking returns null
            assert bookingDAO.findById(9999999) == null : "Missing booking must return null";
            System.out.println("4.6 Missing booking returns null: true");

            // Seats calculation after booking
            assert bookingDAO.getConfirmedSeatsForRide(rideId) == 2 : "Confirmed seats must be 2";
            assert bookingDAO.getRemainingSeatsForRide(rideId) == 2 : "Remaining seats must be 2";
            System.out.println("4.7 Seat calculations verified (2 confirmed, 2 remaining)");

            // Test UNIQUE constraint on bookings(ride_id, passenger_id) - active duplicate
            System.out.println("4.8 Testing UNIQUE constraint / duplicate active booking prevention");
            try {
                Booking duplicateBooking = new Booking(rideId, passenger1Id, 1);
                bookingDAO.createBooking(duplicateBooking);
                throw new AssertionError("Should have failed because passenger already has active booking!");
            } catch (SQLException e) {
                System.out.println("    Caught expected active booking duplicate exception: " + e.getMessage());
            }

            // Test Transaction Rollback on Overbooking Attempt
            System.out.println("4.9 Testing Transaction Rollback on overbooking attempt");
            try {
                // 2 seats left, requesting 3 seats
                Booking overbooking = new Booking(rideId, passenger2Id, 3);
                bookingDAO.createBooking(overbooking);
                throw new AssertionError("Overbooking must fail and roll back!");
            } catch (SQLException e) {
                System.out.println("    Caught expected overbooking exception: " + e.getMessage());
            }
            // Verify rollback leaves database unchanged
            assert bookingDAO.getConfirmedSeatsForRide(rideId) == 2 : "Confirmed seats must remain 2";
            assert bookingDAO.getRemainingSeatsForRide(rideId) == 2 : "Remaining seats must remain 2";
            System.out.println("    Verified state unchanged after rollback: Confirmed=2, Remaining=2");

            // Test Concurrent Overbooking Simulation (Multithreaded with lock)
            System.out.println("4.10 Testing Concurrent Overbooking prevention with multithreading");
            User passenger3 = createTestUser(userDAO, "Passenger3");
            int passenger3Id = passenger3.getUserId();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // Both threads try to book the remaining 2 seats simultaneously
            Thread t1 = new Thread(() -> {
                try {
                    latch.await();
                    bookingDAO.createBooking(new Booking(rideId, passenger2Id, 2));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    latch.await();
                    bookingDAO.createBooking(new Booking(rideId, passenger3Id, 2));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });

            t1.start();
            t2.start();
            latch.countDown();
            t1.join();
            t2.join();

            assert successCount.get() == 1 : "Exactly 1 thread should succeed";
            assert failCount.get() == 1 : "Exactly 1 thread should fail";
            assert bookingDAO.getConfirmedSeatsForRide(rideId) == 4 : "Ride should now be 100% full (4/4)";
            assert bookingDAO.getRemainingSeatsForRide(rideId) == 0 : "Remaining seats must be 0";
            System.out.println("     Concurrent execution result: Success=" + successCount.get() + ", Rejected=" + failCount.get() + " (Capacity 4/4 filled without overbooking)");

            // Test Cancellation and Seat Availability Updates
            System.out.println("4.11 Testing Booking Cancellation and Seat Restoration");
            boolean cancelSuccess = bookingDAO.cancelBooking(booking1Id);
            assert cancelSuccess : "Cancellation should succeed";
            Booking cancelledBooking = bookingDAO.findById(booking1Id);
            assert cancelledBooking.getBookingStatus() == Booking.BookingStatus.CANCELLED : "Status must be CANCELLED";
            // Confirmed seats should drop from 4 to 2, remaining seats increase from 0 to 2
            assert bookingDAO.getConfirmedSeatsForRide(rideId) == 2 : "Confirmed seats should drop to 2";
            assert bookingDAO.getRemainingSeatsForRide(rideId) == 2 : "Remaining seats should increase to 2";
            System.out.println("     Cancellation successful: Status=CANCELLED, Confirmed seats=2, Remaining seats=2");

            // Test Reactivating Previously Cancelled Booking (Satisfies UNIQUE constraint)
            System.out.println("4.12 Testing re-booking after cancellation (UNIQUE constraint reuse)");
            Booking rebooking = new Booking(rideId, passenger1Id, 1);
            int rebookedId = bookingDAO.createBooking(rebooking);
            assert rebookedId == booking1Id : "Re-booking should update and reactivate existing booking row";
            assert bookingDAO.getConfirmedSeatsForRide(rideId) == 3 : "Confirmed seats should now be 3";
            assert bookingDAO.getRemainingSeatsForRide(rideId) == 1 : "Remaining seats should now be 1";
            System.out.println("     Re-booking reactivated ID " + rebookedId + ": Confirmed=3, Remaining=1");

            // Test Ride Status Updates via RideStatus Enum
            System.out.println("\n[SECTION 5] RIDE STATUS UPDATES");
            rideDAO.updateRideStatus(rideId, Ride.RideStatus.IN_PROGRESS);
            assert rideDAO.findById(rideId).getRideStatus() == Ride.RideStatus.IN_PROGRESS : "Status must be IN_PROGRESS";
            System.out.println("5.1 Updated status to IN_PROGRESS: Verified");

            rideDAO.updateRideStatus(rideId, Ride.RideStatus.CANCELLED);
            assert rideDAO.findById(rideId).getRideStatus() == Ride.RideStatus.CANCELLED : "Status must be CANCELLED";
            // Check that booking fails on CANCELLED ride
            try {
                User latePassenger = createTestUser(userDAO, "LatePassenger");
                bookingDAO.createBooking(new Booking(rideId, latePassenger.getUserId(), 1));
                throw new AssertionError("Should not be able to book a CANCELLED ride!");
            } catch (SQLException e) {
                System.out.println("5.2 Booking on CANCELLED ride rejected as expected: " + e.getMessage());
            }

            // -----------------------------------------------------------------
            // CLEANUP
            // -----------------------------------------------------------------
            System.out.println("\n--- CLEANUP ---");
            deleteBookingsForRide(rideId);
            rideDAO.deleteRide(rideId);
            vehicleDAO.deleteVehicle(vehicleId);
            System.out.println("Cleanup completed successfully.");

            System.out.println("\n=================================================================");
            System.out.println("       ALL 4 DAO CLASSES PASSED ALL AUDIT & INTEGRITY TESTS!     ");
            System.out.println("=================================================================");

        } catch (Throwable t) {
            System.err.println("TEST FAILURE: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static User createTestUser(UserDAO userDAO, String prefix) throws SQLException {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setPhoneNo("9" + String.valueOf(System.currentTimeMillis() + (long)(Math.random() * 100000)).substring(3, 12));
        user.setName(prefix + "_" + unique);
        user.setGender("Other");
        user.setEmail(prefix.toLowerCase() + "_" + unique + "@example.com");
        user.setGovtId("gid_" + unique);
        user.setBehaviour("Good");
        user.setPasswordHash("HASH_" + unique);
        int id = userDAO.registerUser(user);
        user.setUserId(id);
        return user;
    }

    private static Ride createRideObject(int driverId, int vehicleId, int seats) {
        Ride ride = new Ride();
        ride.setDriverId(driverId);
        ride.setVehicleId(vehicleId);
        ride.setNoOfSeats(seats);
        ride.setSourceAddress("Koramangala, Bengaluru");
        ride.setDestinationAddress("Indiranagar, Bengaluru");
        ride.setDepartureTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0));
        ride.setCostPerSeat(new BigDecimal("250.00"));
        ride.setRideStatus(Ride.RideStatus.AVAILABLE);
        return ride;
    }

    private static void deleteBookingsForRide(int rideId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE ride_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, rideId);
            statement.executeUpdate();
        }
    }
}
