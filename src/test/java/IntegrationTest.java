import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.service.interfaces.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * End-to-end integration test verifying UI-facing service calls with the real PostgreSQL backend.
 */
public class IntegrationTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("       RUNNING UI-BACKEND END-TO-END INTEGRATION TEST            ");
        System.out.println("=================================================================");

        // Enable real backend services in ServiceFactory
        ServiceFactory.setUseRealServices(true);

        AuthService auth = ServiceFactory.getAuthService();
        UserService userSvc = ServiceFactory.getUserService();
        VehicleService vehicleSvc = ServiceFactory.getVehicleService();
        RideService rideSvc = ServiceFactory.getRideService();
        BookingService bookingSvc = ServiceFactory.getBookingService();

        long timestamp = System.currentTimeMillis() % 10000000;
        String driverPhone = "91" + String.format("%08d", timestamp);
        String passengerPhone = "92" + String.format("%08d", timestamp);
        String driverEmail = "driver_" + timestamp + "@test.com";
        String passengerEmail = "pass_" + timestamp + "@test.com";

        try {
            // ── TEST 1: Driver Registration ──
            System.out.println("\n[1] Registering Driver Account...");
            User driver = new User(driverPhone, "E2E Driver", "Male", driverEmail, "GOVT-DRV", "Safe driver", "DriverSecret123");
            User registeredDriver = auth.register(driver);
            assertNotNull(registeredDriver, "Driver registration failed");
            assertTrue(registeredDriver.getUserId() > 0, "Driver ID must be generated");
            assertEquals(registeredDriver.getUserId(), auth.getCurrentUser().getUserId(), "AuthService current user must be set to driver");
            System.out.println("✓ Driver registered with ID: " + registeredDriver.getUserId() + ", Logged in: " + auth.isLoggedIn());

            // ── TEST 2: Driver Logout & Login ──
            System.out.println("\n[2] Testing Logout & Login with Phone / Email...");
            auth.logout();
            assertFalse(auth.isLoggedIn(), "AuthService must reflect logged-out state");
            assertNull(auth.getCurrentUser(), "Current user must be null after logout");

            // Login with phone
            User loggedInByPhone = auth.login(driverPhone, "DriverSecret123");
            assertNotNull(loggedInByPhone, "Login with phone must succeed");
            assertEquals(registeredDriver.getUserId(), loggedInByPhone.getUserId(), "User ID must match");
            System.out.println("✓ Logged in successfully using phone number");

            auth.logout();
            // Login with email
            User loggedInByEmail = auth.login(driverEmail, "DriverSecret123");
            assertNotNull(loggedInByEmail, "Login with email must succeed");
            assertEquals(registeredDriver.getUserId(), loggedInByEmail.getUserId(), "User ID must match");
            System.out.println("✓ Logged in successfully using email address");

            // ── TEST 3: Vehicle Registration ──
            System.out.println("\n[3] Adding Vehicle for Driver...");
            Vehicle vehicle = new Vehicle(0, "Honda City ZX", registeredDriver.getUserId());
            Vehicle addedVehicle = vehicleSvc.addVehicle(vehicle);
            assertNotNull(addedVehicle, "Vehicle addition failed");
            assertTrue(addedVehicle.getVehicleId() > 0, "Vehicle ID must be generated");
            List<Vehicle> driverVehicles = vehicleSvc.getVehiclesByOwner(registeredDriver.getUserId());
            assertEquals(1, driverVehicles.size(), "Driver must have 1 registered vehicle");
            System.out.println("✓ Vehicle added: ID=" + addedVehicle.getVehicleId() + ", Model=" + addedVehicle.getCarModel());

            // ── TEST 4: Publish Ride ──
            System.out.println("\n[4] Publishing a Ride...");
            LocalDateTime departureTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
            Ride ride = new Ride(registeredDriver.getUserId(), addedVehicle.getVehicleId(),
                    4, "Koramangala, Bangalore", "Whitefield, Bangalore", departureTime, new BigDecimal("150.00"));
            Ride publishedRide = rideSvc.publishRide(ride);
            assertNotNull(publishedRide, "Ride publish failed");
            assertTrue(publishedRide.getRideId() > 0, "Ride ID must be generated");
            assertEquals(4, publishedRide.getNoOfSeats(), "Seats must be 4");
            System.out.println("✓ Ride published: ID=" + publishedRide.getRideId() + " (" + publishedRide.getSourceAddress() + " -> " + publishedRide.getDestinationAddress() + ")");

            // ── TEST 5: Search Rides ──
            System.out.println("\n[5] Searching Available Rides...");
            List<Ride> searchResults = rideSvc.searchRides("Koramangala", "Whitefield", LocalDate.now().plusDays(2), 1);
            assertTrue(!searchResults.isEmpty(), "Search must return the published ride");
            System.out.println("✓ Search found " + searchResults.size() + " ride(s) matching route and date");

            // ── TEST 6: Register Passenger & Book Seats ──
            System.out.println("\n[6] Registering Passenger & Booking Seats...");
            auth.logout();
            User passenger = new User(passengerPhone, "E2E Passenger", "Female", passengerEmail, "GOVT-PASS", "Punctual", "PassSecret123");
            User registeredPassenger = auth.register(passenger);
            System.out.println("✓ Passenger registered: ID=" + registeredPassenger.getUserId());

            Booking booking = bookingSvc.createBooking(publishedRide.getRideId(), registeredPassenger.getUserId(), 2);
            assertNotNull(booking, "Booking must succeed");
            assertTrue(booking.getBookingId() > 0, "Booking ID must be generated");
            assertEquals(Booking.BookingStatus.CONFIRMED, booking.getBookingStatus(), "Status must be CONFIRMED");
            System.out.println("✓ Booking confirmed: ID=" + booking.getBookingId() + " for 2 seats");

            // Verify remaining seats
            int remainingSeats = rideSvc.getRemainingSeats(publishedRide.getRideId());
            assertEquals(2, remainingSeats, "Remaining seats must be 4 - 2 = 2");
            System.out.println("✓ Remaining seats updated accurately: " + remainingSeats + " / 4");

            // ── TEST 7: Query My Rides & My Bookings ──
            System.out.println("\n[7] Verifying User Views (My Rides & My Bookings)...");
            List<Booking> passengerBookings = bookingSvc.getBookingsByPassenger(registeredPassenger.getUserId());
            assertEquals(1, passengerBookings.size(), "Passenger must have 1 booking");
            assertEquals(booking.getBookingId(), passengerBookings.get(0).getBookingId(), "Booking ID must match");

            List<Ride> driverRides = rideSvc.getRidesByDriver(registeredDriver.getUserId());
            assertEquals(1, driverRides.size(), "Driver must have 1 published ride");
            System.out.println("✓ Passenger and driver dashboard views populated correctly");

            // ── TEST 8: Passenger Cancels Booking ──
            System.out.println("\n[8] Passenger Cancelling Booking...");
            boolean cancelBookingSuccess = bookingSvc.cancelBooking(booking.getBookingId());
            assertTrue(cancelBookingSuccess, "Booking cancellation must succeed");
            int restoredSeats = rideSvc.getRemainingSeats(publishedRide.getRideId());
            assertEquals(4, restoredSeats, "Remaining seats must be restored to 4 after cancellation");
            System.out.println("✓ Booking cancelled and seats restored to: " + restoredSeats);

            // Re-book 1 seat to test ride cancellation cascade
            Booking booking2 = bookingSvc.createBooking(publishedRide.getRideId(), registeredPassenger.getUserId(), 1);
            assertEquals(3, rideSvc.getRemainingSeats(publishedRide.getRideId()), "Remaining seats should now be 3");

            // ── TEST 9: Driver Cancels Ride (Cascade Test) ──
            System.out.println("\n[9] Driver Cancelling Ride (with booking cascade)...");
            boolean cancelRideSuccess = rideSvc.cancelRide(publishedRide.getRideId());
            assertTrue(cancelRideSuccess, "Ride cancellation must succeed");

            Ride cancelledRide = rideSvc.findById(publishedRide.getRideId());
            assertEquals(Ride.RideStatus.CANCELLED, cancelledRide.getRideStatus(), "Ride status must be CANCELLED");

            Booking cascadeCancelledBooking = bookingSvc.findById(booking2.getBookingId());
            assertEquals(Booking.BookingStatus.CANCELLED, cascadeCancelledBooking.getBookingStatus(), "Associated booking must be cascade-cancelled");
            System.out.println("✓ Ride cancelled and associated passenger booking cascade-cancelled");

            // ── TEST 10: Profile Update ──
            System.out.println("\n[10] Updating User Profile...");
            registeredDriver.setName("Updated Driver Name");
            registeredDriver.setBehaviour("Expert highway driver");
            boolean updateSuccess = userSvc.updateProfile(registeredDriver);
            assertTrue(updateSuccess, "Profile update must succeed");
            User updatedDriver = userSvc.findById(registeredDriver.getUserId());
            assertEquals("Updated Driver Name", updatedDriver.getName(), "Name must be updated");
            assertEquals("Expert highway driver", updatedDriver.getBehaviour(), "Behaviour must be updated");
            System.out.println("✓ Profile updated and verified in database");

            System.out.println("\n=================================================================");
            System.out.println("       ALL UI-BACKEND INTEGRATION TESTS PASSED SUCCESSFULLY!     ");
            System.out.println("=================================================================");

        } catch (Exception e) {
            System.err.println("\nTEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new AssertionError("Assertion failed: " + msg);
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError("Assertion failed: " + msg);
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) throw new AssertionError("Assertion failed: " + msg);
    }

    private static void assertNull(Object obj, String msg) {
        if (obj != null) throw new AssertionError("Assertion failed: " + msg);
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: " + msg + " (expected: " + expected + ", actual: " + actual + ")");
    }
}
