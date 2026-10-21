import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ridesharing.dao.BookingDAO;
import com.ridesharing.dao.RideDAO;
import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.BookingService;
import com.ridesharing.service.RideService;
import com.ridesharing.service.UserService;
import com.ridesharing.service.VehicleService;

public class ServiceTest {

    public static void main(String[] args) {
        UserService userService = new UserService();
        VehicleService vehicleService = new VehicleService();
        RideService rideService = new RideService();
        BookingService bookingService = new BookingService();

        try {
            System.out.println("=================================================================");
            System.out.println("             RUNNING SERVICE BUSINESS RULES TESTS                ");
            System.out.println("=================================================================");

            // -------------------------------------------------------------
            // SECTION 1: USER SERVICE & PASSWORD HASHING
            // -------------------------------------------------------------
            System.out.println("\n[SECTION 1] USER SERVICE (REGISTRATION, HASHING & LOGIN)");
            String unique = UUID.randomUUID().toString().substring(0, 8);
            String phone = "9" + String.valueOf(System.currentTimeMillis() + 11111).substring(3, 12);
            String email = "driver_" + unique + "@example.com";

            User driverUser = new User(phone, "Vikram Driver", "Male", email, "gid_" + unique, "Professional", "superSecret123");
            User registeredDriver = userService.registerUser(driverUser);
            assert registeredDriver.getUserId() > 0 : "Generated user ID must be > 0";
            assert registeredDriver.getPasswordHash().contains(":") : "Password must be securely stored as salt:hash PBKDF2";
            System.out.println("1.1 Driver registered with securely hashed password: " + registeredDriver.getPasswordHash().substring(0, 20) + "...");

            // Test login with hashed password verification
            User loggedIn = userService.login(phone, "superSecret123");
            assert loggedIn != null && loggedIn.getUserId() == registeredDriver.getUserId() : "Login with correct password must succeed";
            assert userService.login(phone, "wrongPassword") == null : "Login with incorrect password must fail";
            System.out.println("1.2 Login authentication with PBKDF2 hash verified successfully");

            // -------------------------------------------------------------
            // SECTION 2: VEHICLE SERVICE & OWNERSHIP
            // -------------------------------------------------------------
            System.out.println("\n[SECTION 2] VEHICLE SERVICE (OWNERSHIP & RIDE ASSOCIATIONS)");
            // Rule: Owner must exist
            try {
                vehicleService.addVehicle(new Vehicle(0, "Toyota Fortuner", 9999999));
                throw new AssertionError("Should fail when owner does not exist!");
            } catch (IllegalArgumentException e) {
                System.out.println("2.1 Owner existence validation caught: " + e.getMessage());
            }

            Vehicle vehicle = new Vehicle(0, "Toyota Fortuner", registeredDriver.getUserId());
            Vehicle addedVehicle = vehicleService.addVehicle(vehicle);
            int vehicleId = addedVehicle.getVehicleId();
            assert vehicleId > 0 : "Vehicle ID must be generated";
            System.out.println("2.2 Vehicle registered for driver: ID=" + vehicleId + " (" + addedVehicle.getCarModel() + ")");

            // -------------------------------------------------------------
            // SECTION 3: RIDE SERVICE (DRIVER VEHICLE OWNERSHIP & TRANSITIONS)
            // -------------------------------------------------------------
            System.out.println("\n[SECTION 3] RIDE SERVICE (VEHICLE OWNERSHIP, STATUS & CANCEL)");
            // Create another user who owns a different vehicle
            String otherPhone = "9" + String.valueOf(System.currentTimeMillis() + 22222).substring(3, 12);
            User otherUser = userService.registerUser(new User(otherPhone, "Other Owner", "Female", "other_" + unique + "@example.com", "gid_o_" + unique, "Good", "pass123"));
            Vehicle otherVehicle = vehicleService.addVehicle(new Vehicle(0, "Maruti Swift", otherUser.getUserId()));

            // Rule: Driver must own the selected vehicle
            try {
                Ride unownedVehicleRide = new Ride(registeredDriver.getUserId(), otherVehicle.getVehicleId(), 4, "Indiranagar", "Whitefield", LocalDateTime.now().plusDays(2), new BigDecimal("150.00"));
                rideService.createRide(unownedVehicleRide);
                throw new AssertionError("Should fail when driver does not own the vehicle!");
            } catch (IllegalArgumentException e) {
                System.out.println("3.1 Vehicle ownership validation caught: " + e.getMessage());
            }

            // Rule: Cannot publish ride with departure in the past
            try {
                Ride pastRide = new Ride(registeredDriver.getUserId(), vehicleId, 4, "Indiranagar", "Whitefield", LocalDateTime.now().minusHours(1), new BigDecimal("150.00"));
                rideService.createRide(pastRide);
                throw new AssertionError("Should fail for past departure!");
            } catch (IllegalArgumentException e) {
                System.out.println("3.2 Past departure validation caught: " + e.getMessage());
            }

            // Publish valid ride
            LocalDateTime departureTime = LocalDateTime.now().plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0);
            Ride validRide = new Ride(registeredDriver.getUserId(), vehicleId, 3, "Indiranagar, Bengaluru", "Whitefield, Bengaluru", departureTime, new BigDecimal("150.00"));
            Ride publishedRide = rideService.createRide(validRide);
            int rideId = publishedRide.getRideId();
            assert rideId > 0 : "Ride ID must be generated";
            System.out.println("3.3 Ride published successfully: ID=" + rideId + " (Capacity: 3 seats)");

            // Rule: Check whether vehicle is associated with rides before deletion
            try {
                vehicleService.deleteVehicle(vehicleId);
                throw new AssertionError("Vehicle deletion must be prevented when associated with rides!");
            } catch (IllegalStateException e) {
                System.out.println("3.4 Prevented vehicle deletion because it is associated with rides: " + e.getMessage());
            }

            // -------------------------------------------------------------
            // SECTION 4: BOOKING SERVICE (CAPACITY, CONCURRENCY & CANCEL)
            // -------------------------------------------------------------
            System.out.println("\n[SECTION 4] BOOKING SERVICE (CAPACITY, DEPARTURE & PASSENGER RULES)");
            String pPhone = "9" + String.valueOf(System.currentTimeMillis() + 33333).substring(3, 12);
            User passenger = userService.registerUser(new User(pPhone, "Ananya Passenger", "Female", "ananya_" + unique + "@example.com", "gid_p_" + unique, "Polite", "pass123"));
            int passengerId = passenger.getUserId();

            // Rule: Driver cannot book own ride
            try {
                bookingService.bookRide(rideId, registeredDriver.getUserId(), 1);
                throw new AssertionError("Driver cannot book own ride!");
            } catch (IllegalArgumentException e) {
                System.out.println("4.1 Driver self-booking prevented: " + e.getMessage());
            }

            // Rule: Book ride
            Booking booking = bookingService.bookRide(rideId, passengerId, 2);
            int bookingId = booking.getBookingId();
            assert bookingId > 0 : "Booking ID must be generated";
            assert bookingService.getAvailableSeats(rideId) == 1 : "Remaining seats should be 1";
            System.out.println("4.2 Booking confirmed: ID=" + bookingId + " for 2 seats. Remaining seats: 1");

            // Rule: Confirmed bookings must not exceed ride capacity
            try {
                String p2Phone = "9" + String.valueOf(System.currentTimeMillis() + 44444).substring(3, 12);
                User passenger2 = userService.registerUser(new User(p2Phone, "P2", "Male", "p2_" + unique + "@example.com", "gid_p2_" + unique, "Good", "pass123"));
                bookingService.bookRide(rideId, passenger2.getUserId(), 2); // only 1 left!
                throw new AssertionError("Overbooking must fail!");
            } catch (IllegalStateException e) {
                System.out.println("4.3 Overbooking prevented: " + e.getMessage());
            }

            // Rule: Cancel booking - only the relevant passenger can cancel it
            try {
                bookingService.cancelBooking(bookingId, otherUser.getUserId()); // unauthorized user!
                throw new AssertionError("Unauthorized user cannot cancel booking!");
            } catch (IllegalArgumentException e) {
                System.out.println("4.4 Unauthorized booking cancellation prevented: " + e.getMessage());
            }

            // Passenger cancels own booking
            boolean bookingCancelled = bookingService.cancelBooking(bookingId, passengerId);
            assert bookingCancelled : "Authorized cancellation must succeed";
            assert bookingService.getAvailableSeats(rideId) == 3 : "Remaining seats must be restored to 3";
            System.out.println("4.5 Passenger cancelled own booking; seats restored to 3");

            // Re-book 1 seat so we can test ride cancellation cascade
            Booking rebooked = bookingService.bookRide(rideId, passengerId, 1);
            int rebookedId = rebooked.getBookingId();
            assert bookingService.getConfirmedSeatsForRide(rideId) == 1 : "Confirmed seats should be 1";

            // -------------------------------------------------------------
            // SECTION 5: RIDE CANCELLATION & STATUS TRANSITIONS
            // -------------------------------------------------------------
            System.out.println("\n[SECTION 5] RIDE CANCELLATION (OWNERSHIP & BOOKING IMPLICATIONS)");
            // Rule: Only the publishing driver can cancel the ride
            try {
                rideService.cancelRide(rideId, otherUser.getUserId());
                throw new AssertionError("Unauthorized driver cannot cancel ride!");
            } catch (IllegalArgumentException e) {
                System.out.println("5.1 Unauthorized ride cancellation prevented: " + e.getMessage());
            }

            // Driver cancels own ride: checks existing bookings and cascade-cancels them
            boolean rideCancelled = rideService.cancelRide(rideId, registeredDriver.getUserId());
            assert rideCancelled : "Driver cancellation must succeed";
            Ride cancelledRide = rideService.getRideById(rideId);
            assert cancelledRide.getRideStatus() == Ride.RideStatus.CANCELLED : "Ride status must be CANCELLED";
            Booking cascadeCancelledBooking = bookingService.getBookingById(rebookedId);
            assert cascadeCancelledBooking.getBookingStatus() == Booking.BookingStatus.CANCELLED : "Passenger booking must be cascade-cancelled";
            System.out.println("5.2 Ride cancelled by driver: Status=CANCELLED; Associated booking cascade-cancelled to CANCELLED");

            // Rule: Cannot book a cancelled ride
            try {
                bookingService.bookRide(rideId, passengerId, 1);
                throw new AssertionError("Cannot book a cancelled ride!");
            } catch (IllegalStateException e) {
                System.out.println("5.3 Booking on CANCELLED ride prevented: " + e.getMessage());
            }

            // Rule: Invalid status transitions from CANCELLED
            try {
                rideService.updateRideStatus(rideId, Ride.RideStatus.AVAILABLE);
                throw new AssertionError("Cannot transition out of terminal CANCELLED status!");
            } catch (IllegalStateException e) {
                System.out.println("5.4 Invalid status transition from CANCELLED prevented: " + e.getMessage());
            }

            // -------------------------------------------------------------
            // CLEANUP
            // -------------------------------------------------------------
            System.out.println("\n--- CLEANUP ---");
            new BookingDAO().deleteBooking(rebookedId);
            new RideDAO().deleteRide(rideId);
            vehicleService.deleteVehicle(vehicleId);
            vehicleService.deleteVehicle(otherVehicle.getVehicleId());
            System.out.println("Test records cleaned up successfully.");

            System.out.println("\n=================================================================");
            System.out.println("       ALL SERVICE BUSINESS RULES VALIDATED & PASSED!            ");
            System.out.println("=================================================================");

        } catch (Throwable t) {
            System.err.println("TEST FAILURE: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
