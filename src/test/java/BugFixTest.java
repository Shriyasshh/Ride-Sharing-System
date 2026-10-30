import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.service.interfaces.AuthService;
import com.ridesharing.service.interfaces.BookingService;
import com.ridesharing.service.interfaces.RideService;
import com.ridesharing.service.interfaces.UserService;
import com.ridesharing.service.interfaces.VehicleService;

/**
 * Verification test suite for Bug 1 and Bug 2 fixes.
 *
 * Scenarios tested:
 * 1. Deleting a vehicle with no rides (owner authorized -> succeeds).
 * 2. Attempting to delete a vehicle associated with an existing ride (fails with clear message).
 * 3. Unauthorized vehicle deletion (user A attempting to delete user B's vehicle -> fails).
 * 4. Driver viewing passengers for their own ride (succeeds, returns passenger name, phone, email, seats, status).
 * 5. Driver attempting to view passengers for another driver's ride (unauthorized -> fails).
 * 6. Ride with no bookings (returns empty list gracefully).
 * 7. Correct handling of confirmed and cancelled bookings.
 */
public class BugFixTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("            RUNNING BUG FIX VERIFICATION TEST SUITE              ");
        System.out.println("=================================================================");

        // Enable real PostgreSQL backend services
        ServiceFactory.setUseRealServices(true);

        AuthService authService = ServiceFactory.getAuthService();
        UserService userService = ServiceFactory.getUserService();
        VehicleService vehicleService = ServiceFactory.getVehicleService();
        RideService rideService = ServiceFactory.getRideService();
        BookingService bookingService = ServiceFactory.getBookingService();

        long timestamp = System.currentTimeMillis() % 10000000;
        String unique = UUID.randomUUID().toString().substring(0, 6);

        try {
            // Setup Drivers & Passenger
            System.out.println("\n[SETUP] Creating test users in PostgreSQL...");
            String d1Phone = "93" + String.format("%08d", timestamp % 100000000);
            String d2Phone = "94" + String.format("%08d", timestamp % 100000000);
            String p1Phone = "95" + String.format("%08d", timestamp % 100000000);

            User driver1 = authService.register(new User(d1Phone, "Driver One", "Male", "d1_" + unique + "@test.com", "GID-D1-" + unique, "Good", "Pass1234"));
            authService.logout();
            User driver2 = authService.register(new User(d2Phone, "Driver Two", "Female", "d2_" + unique + "@test.com", "GID-D2-" + unique, "Good", "Pass1234"));
            authService.logout();
            User passenger1 = authService.register(new User(p1Phone, "Passenger One", "Female", "p1_" + unique + "@test.com", "GID-P1-" + unique, "Friendly", "Pass1234"));
            authService.logout();

            int driver1Id = driver1.getUserId();
            int driver2Id = driver2.getUserId();
            int passenger1Id = passenger1.getUserId();

            System.out.println("✓ Driver 1 created (ID: " + driver1Id + ")");
            System.out.println("✓ Driver 2 created (ID: " + driver2Id + ")");
            System.out.println("✓ Passenger 1 created (ID: " + passenger1Id + ")");

            // -----------------------------------------------------------------
            // BUG 1 - TEST 1: Deleting a vehicle with no rides (owner authorized)
            // -----------------------------------------------------------------
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 1] Deleting a vehicle with no rides (Owner authorized)");
            System.out.println("-------------------------------------------------------------");
            Vehicle freeVehicle = vehicleService.addVehicle(new Vehicle(0, "Honda City Standalone", driver1Id));
            int freeVehicleId = freeVehicle.getVehicleId();
            assertTrue(freeVehicleId > 0, "Standalone vehicle must have valid ID");
            System.out.println("Created vehicle with no rides: ID=" + freeVehicleId);

            boolean deleteResult = vehicleService.deleteVehicle(freeVehicleId, driver1Id);
            assertTrue(deleteResult, "Deleting vehicle with no rides by owner must return true");
            Vehicle postDelete = vehicleService.findById(freeVehicleId);
            assertNull(postDelete, "Vehicle must no longer exist after deletion");
            System.out.println("✓ TEST 1 PASSED: Vehicle with no rides successfully deleted by owner.");

            // -----------------------------------------------------------------
            // BUG 1 - TEST 2: Attempting to delete a vehicle associated with a ride
            // -----------------------------------------------------------------
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 2] Attempting to delete a vehicle associated with a ride");
            System.out.println("-------------------------------------------------------------");
            Vehicle rideVehicle = vehicleService.addVehicle(new Vehicle(0, "Hyundai Creta Linked", driver1Id));
            int rideVehicleId = rideVehicle.getVehicleId();

            LocalDateTime depTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
            Ride ride1 = new Ride(driver1Id, rideVehicleId, 4, "Koramangala", "Electronic City", depTime, new BigDecimal("200.00"));
            Ride publishedRide1 = rideService.publishRide(ride1);
            int ride1Id = publishedRide1.getRideId();
            System.out.println("Created ride (ID: " + ride1Id + ") linked to vehicle (ID: " + rideVehicleId + ")");

            boolean caughtAssociatedError = false;
            try {
                vehicleService.deleteVehicle(rideVehicleId, driver1Id);
            } catch (IllegalStateException e) {
                caughtAssociatedError = true;
                System.out.println("Caught expected exception: " + e.getMessage());
                assertTrue(e.getMessage().contains("associated with existing rides"),
                        "Message must state vehicle cannot be deleted because it is associated with existing rides");
            }
            assertTrue(caughtAssociatedError, "Must throw IllegalStateException when deleting vehicle associated with rides");

            // Verify vehicle and ride are still in database intact
            Vehicle stillExistingVehicle = vehicleService.findById(rideVehicleId);
            assertNotNull(stillExistingVehicle, "Vehicle must NOT be deleted from database");
            Ride stillExistingRide = rideService.findById(ride1Id);
            assertNotNull(stillExistingRide, "Associated ride must NOT be deleted");
            System.out.println("✓ TEST 2 PASSED: Vehicle linked to ride deletion blocked and referential integrity preserved.");

            // -----------------------------------------------------------------
            // BUG 1 - TEST 3: Unauthorized vehicle deletion
            // -----------------------------------------------------------------
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 3] Unauthorized vehicle deletion attempt");
            System.out.println("-------------------------------------------------------------");
            Vehicle driver2Vehicle = vehicleService.addVehicle(new Vehicle(0, "Kia Seltos Driver2", driver2Id));
            int driver2VehicleId = driver2Vehicle.getVehicleId();

            boolean caughtUnauthorized = false;
            try {
                // Driver 1 attempts to delete Driver 2's vehicle
                vehicleService.deleteVehicle(driver2VehicleId, driver1Id);
            } catch (IllegalArgumentException e) {
                caughtUnauthorized = true;
                System.out.println("Caught expected unauthorized exception: " + e.getMessage());
                assertTrue(e.getMessage().contains("Unauthorized"), "Exception must state unauthorized");
            }
            assertTrue(caughtUnauthorized, "Must throw IllegalArgumentException on unauthorized vehicle deletion attempt");

            Vehicle driver2VehicleStillExists = vehicleService.findById(driver2VehicleId);
            assertNotNull(driver2VehicleStillExists, "Target vehicle must NOT be deleted by unauthorized user");
            System.out.println("✓ TEST 3 PASSED: Unauthorized deletion rejected; vehicle remains intact.");

            // -----------------------------------------------------------------
            // BUG 2 - TEST 6: A ride with no bookings (empty state)
            // -----------------------------------------------------------------
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 6] Ride with no bookings (empty state handling)");
            System.out.println("-------------------------------------------------------------");
            List<Booking> emptyPassengers = bookingService.getPassengersByRide(ride1Id, driver1Id);
            assertNotNull(emptyPassengers, "Passenger list must not be null");
            assertTrue(emptyPassengers.isEmpty(), "Passenger list must be empty for ride with no bookings");
            System.out.println("✓ TEST 6 PASSED: Ride with no bookings returned clean empty list (" + emptyPassengers.size() + " items).");

            // -----------------------------------------------------------------
            // BUG 2 - TEST 4: Driver viewing passengers for their own ride
            // -----------------------------------------------------------------
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 4] Driver viewing passengers for their own ride");
            System.out.println("-------------------------------------------------------------");
            // Passenger 1 books 2 seats on ride 1
            Booking b1 = bookingService.createBooking(ride1Id, passenger1Id, 2);
            int booking1Id = b1.getBookingId();
            System.out.println("Passenger 1 booked 2 seats (Booking ID: " + booking1Id + ")");

            List<Booking> passengers = bookingService.getPassengersByRide(ride1Id, driver1Id);
            assertNotNull(passengers, "Passenger list must not be null");
            assertEquals(1, passengers.size(), "Should have 1 booking returned");

            Booking retrievedBooking = passengers.get(0);
            assertEquals(booking1Id, retrievedBooking.getBookingId(), "Booking ID must match");
            assertEquals(2, retrievedBooking.getSeatsBooked(), "Seats booked must match");
            assertEquals(Booking.BookingStatus.CONFIRMED, retrievedBooking.getBookingStatus(), "Status must be CONFIRMED");

            // Verify passenger fields retrieved via 3-table join
            assertEquals("Passenger One", retrievedBooking.getPassengerName(), "Passenger name must match");
            assertEquals(p1Phone, retrievedBooking.getPassengerPhone(), "Passenger phone must match");
            assertEquals("p1_" + unique + "@test.com", retrievedBooking.getPassengerEmail(), "Passenger email must match");
            assertNotNull(retrievedBooking.getBookingTime(), "Booking time must not be null");
            System.out.println("Retrieved passenger details: Name=" + retrievedBooking.getPassengerName() +
                    ", Phone=" + retrievedBooking.getPassengerPhone() +
                    ", Email=" + retrievedBooking.getPassengerEmail() +
                    ", Seats=" + retrievedBooking.getSeatsBooked() +
                    ", Status=" + retrievedBooking.getBookingStatus() +
                    ", Time=" + retrievedBooking.getBookingTime());
            System.out.println("✓ TEST 4 PASSED: Driver successfully viewed passenger details for their own ride.");

            // -----------------------------------------------------------------
            // BUG 2 - TEST 5: Driver attempting to view passengers for another driver's ride
            // -----------------------------------------------------------------
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 5] Driver viewing passengers for another driver's ride");
            System.out.println("-------------------------------------------------------------");
            boolean caughtUnauthorizedPassengerView = false;
            try {
                // Driver 2 attempts to view passengers for Driver 1's ride
                bookingService.getPassengersByRide(ride1Id, driver2Id);
            } catch (IllegalArgumentException e) {
                caughtUnauthorizedPassengerView = true;
                System.out.println("Caught expected unauthorized exception: " + e.getMessage());
                assertTrue(e.getMessage().contains("Unauthorized"), "Must state unauthorized");
            }
            assertTrue(caughtUnauthorizedPassengerView, "Must throw IllegalArgumentException when non-driver attempts to view passengers");
            System.out.println("✓ TEST 5 PASSED: Unauthorized passenger view attempt rejected.");

            // -----------------------------------------------------------------
            // BUG 2 - TEST 7: Correct handling of confirmed and cancelled bookings
            // -----------------------------------------------------------------
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 7] Handling of confirmed and cancelled bookings");
            System.out.println("-------------------------------------------------------------");
            // Register a second passenger and make a booking
            String p2Phone = "96" + String.format("%08d", timestamp % 100000000);
            User passenger2 = authService.register(new User(p2Phone, "Passenger Two", "Male", "p2_" + unique + "@test.com", "GID-P2-" + unique, "Quiet", "Pass1234"));
            authService.logout();

            Booking b2 = bookingService.createBooking(ride1Id, passenger2.getUserId(), 1);
            int booking2Id = b2.getBookingId();
            System.out.println("Passenger 2 booked 1 seat (Booking ID: " + booking2Id + ")");

            // Now cancel booking 2
            boolean cancelSuccess = bookingService.cancelBooking(booking2Id);
            assertTrue(cancelSuccess, "Booking cancellation must succeed");
            System.out.println("Passenger 2 cancelled booking ID: " + booking2Id);

            // Fetch all bookings for ride 1
            List<Booking> allPassengers = bookingService.getPassengersByRide(ride1Id, driver1Id);
            assertEquals(2, allPassengers.size(), "Should have 2 bookings total (1 confirmed, 1 cancelled)");

            long confirmedCount = allPassengers.stream()
                    .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                    .count();
            long cancelledCount = allPassengers.stream()
                    .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CANCELLED)
                    .count();

            assertEquals(1L, confirmedCount, "Should have exactly 1 confirmed booking");
            assertEquals(1L, cancelledCount, "Should have exactly 1 cancelled booking");

            System.out.println("Confirmed bookings count: " + confirmedCount);
            System.out.println("Cancelled bookings count: " + cancelledCount);
            System.out.println("✓ TEST 7 PASSED: Confirmed and cancelled bookings properly tracked and distinguishable.");

            System.out.println("\n=================================================================");
            System.out.println("            ALL 7 BUG FIX TEST SCENARIOS PASSED!                 ");
            System.out.println("=================================================================");

        } catch (Throwable t) {
            System.err.println("\nTEST FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError("Assertion Failed: " + message + " (was " + obj + ")");
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion Failed: " + message + " (expected: " + expected + ", actual: " + actual + ")");
    }
}
