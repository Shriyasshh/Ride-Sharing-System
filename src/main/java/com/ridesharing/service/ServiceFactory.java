package com.ridesharing.service;

import com.ridesharing.service.interfaces.AuthService;
import com.ridesharing.service.interfaces.BookingService;
import com.ridesharing.service.interfaces.RideService;
import com.ridesharing.service.interfaces.UserService;
import com.ridesharing.service.interfaces.VehicleService;
import com.ridesharing.service.mock.MockAuthService;
import com.ridesharing.service.mock.MockBookingService;
import com.ridesharing.service.mock.MockRideService;
import com.ridesharing.service.mock.MockUserService;
import com.ridesharing.service.mock.MockVehicleService;

/**
 * Factory for obtaining service instances.
 *
 * HOW TO REPLACE MOCK SERVICES WITH REAL IMPLEMENTATIONS:
 * ─────────────────────────────────────────────────────────
 * 1. Create a class that implements the desired service interface,
 *    e.g.: public class RealAuthService implements AuthService { ... }
 *
 * 2. In that implementation, inject and use your DAO classes
 *    (UserDAO, VehicleDAO, etc.) to perform actual database operations.
 *
 * 3. In this factory, change the corresponding getter to return
 *    your real implementation instead of the mock, or call setUseRealServices(true).
 *
 * 4. The UI code will automatically use the real backend
 *    because it only references the service interfaces.
 */
public final class ServiceFactory {

    private static boolean useRealServices = false;

    private static AuthService authService;
    private static UserService userService;
    private static VehicleService vehicleService;
    private static RideService rideService;
    private static BookingService bookingService;

    private ServiceFactory() {}

    public static synchronized boolean isUseRealServices() {
        return useRealServices;
    }

    public static synchronized AuthService getAuthService() {
        if (authService == null) {
            authService = useRealServices
                    ? new com.ridesharing.service.RealAuthService((com.ridesharing.service.UserService) getUserService())
                    : new MockAuthService();
        }
        return authService;
    }

    public static synchronized UserService getUserService() {
        if (userService == null) {
            userService = useRealServices ? new com.ridesharing.service.UserService() : new MockUserService();
        }
        return userService;
    }

    public static synchronized VehicleService getVehicleService() {
        if (vehicleService == null) {
            vehicleService = useRealServices ? new com.ridesharing.service.VehicleService() : new MockVehicleService();
        }
        return vehicleService;
    }

    public static synchronized RideService getRideService() {
        if (rideService == null) {
            rideService = useRealServices ? new com.ridesharing.service.RideService() : new MockRideService();
        }
        return rideService;
    }

    public static synchronized BookingService getBookingService() {
        if (bookingService == null) {
            bookingService = useRealServices ? new com.ridesharing.service.BookingService() : new MockBookingService();
        }
        return bookingService;
    }

    public static synchronized void setUseRealServices(boolean real) {
        useRealServices = real;
        if (real) {
            userService = new com.ridesharing.service.UserService();
            authService = new com.ridesharing.service.RealAuthService((com.ridesharing.service.UserService) userService);
            vehicleService = new com.ridesharing.service.VehicleService();
            rideService = new com.ridesharing.service.RideService();
            bookingService = new com.ridesharing.service.BookingService();
        } else {
            authService = new MockAuthService();
            userService = new MockUserService();
            vehicleService = new MockVehicleService();
            rideService = new MockRideService();
            bookingService = new MockBookingService();
        }
    }
}

