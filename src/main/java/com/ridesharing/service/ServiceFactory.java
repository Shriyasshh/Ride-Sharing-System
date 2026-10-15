package com.ridesharing.service;

import com.ridesharing.service.interfaces.*;
import com.ridesharing.service.mock.*;

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
 *    your real implementation instead of the mock.
 *    For example:
 *      public static AuthService getAuthService() {
 *          return new RealAuthService();  // instead of MockAuthService
 *      }
 *
 * 4. The UI code will automatically use the real backend
 *    because it only references the service interfaces.
 */
public final class ServiceFactory {

    private static AuthService authService;
    private static UserService userService;
    private static VehicleService vehicleService;
    private static RideService rideService;
    private static BookingService bookingService;

    private ServiceFactory() {}

    public static synchronized AuthService getAuthService() {
        if (authService == null) authService = new MockAuthService();
        return authService;
    }

    public static synchronized UserService getUserService() {
        if (userService == null) userService = new MockUserService();
        return userService;
    }

    public static synchronized VehicleService getVehicleService() {
        if (vehicleService == null) vehicleService = new MockVehicleService();
        return vehicleService;
    }

    public static synchronized RideService getRideService() {
        if (rideService == null) rideService = new MockRideService();
        return rideService;
    }

    public static synchronized BookingService getBookingService() {
        if (bookingService == null) bookingService = new MockBookingService();
        return bookingService;
    }
}
