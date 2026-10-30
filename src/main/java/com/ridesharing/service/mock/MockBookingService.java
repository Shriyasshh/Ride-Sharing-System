package com.ridesharing.service.mock;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.service.interfaces.BookingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockBookingService implements BookingService {

    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public Booking createBooking(int rideId, int passengerId, int seatsBooked) throws IllegalArgumentException {
        Ride ride = null;
        for (Ride r : store.getRides()) {
            if (r.getRideId() == rideId) { ride = r; break; }
        }
        if (ride == null)
            throw new IllegalArgumentException("Ride not found.");
        if (ride.getRideStatus() != Ride.RideStatus.AVAILABLE)
            throw new IllegalArgumentException("This ride is not available for booking.");
        if (ride.getDriverId() == passengerId)
            throw new IllegalArgumentException("You cannot book your own ride.");
        if (seatsBooked <= 0)
            throw new IllegalArgumentException("Must book at least 1 seat.");

        int remaining = getRemainingSeats(rideId);
        if (seatsBooked > remaining)
            throw new IllegalArgumentException("Only " + remaining + " seat(s) remaining.");

        Booking booking = new Booking(rideId, passengerId, seatsBooked);
        booking.setBookingId(store.nextBookingId());
        booking.setBookingTime(LocalDateTime.now());
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        store.getBookings().add(booking);
        return booking;
    }

    @Override
    public boolean cancelBooking(int bookingId) {
        Booking b = findById(bookingId);
        if (b == null) return false;
        b.setBookingStatus(Booking.BookingStatus.CANCELLED);
        return true;
    }

    @Override
    public List<Booking> getBookingsByPassenger(int passengerId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : store.getBookings()) {
            if (b.getPassengerId() == passengerId) result.add(b);
        }
        return result;
    }

    @Override
    public List<Booking> getBookingsByRide(int rideId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : store.getBookings()) {
            if (b.getRideId() == rideId) result.add(b);
        }
        return result;
    }

    @Override
    public Booking findById(int bookingId) {
        for (Booking b : store.getBookings()) {
            if (b.getBookingId() == bookingId) return b;
        }
        return null;
    }

    @Override
    public int getConfirmedSeatsForRide(int rideId) {
        int total = 0;
        for (Booking b : store.getBookings()) {
            if (b.getRideId() == rideId && b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                total += b.getSeatsBooked();
            }
        }
        return total;
    }

    private int getRemainingSeats(int rideId) {
        Ride ride = null;
        for (Ride r : store.getRides()) {
            if (r.getRideId() == rideId) { ride = r; break; }
        }
        if (ride == null) return 0;
        return ride.getNoOfSeats() - getConfirmedSeatsForRide(rideId);
    }

    @Override
    public List<Booking> getPassengersByRide(int rideId, int driverId) {
        Ride ride = null;
        for (Ride r : store.getRides()) {
            if (r.getRideId() == rideId) { ride = r; break; }
        }
        if (ride == null) {
            throw new IllegalArgumentException("Ride with ID " + rideId + " does not exist.");
        }
        if (ride.getDriverId() != driverId) {
            throw new IllegalArgumentException("Unauthorized: Only the publishing driver can view passenger details for this ride.");
        }

        List<Booking> result = new ArrayList<>();
        for (Booking b : store.getBookings()) {
            if (b.getRideId() == rideId) {
                com.ridesharing.model.User passenger = null;
                for (com.ridesharing.model.User u : store.getUsers()) {
                    if (u.getUserId() == b.getPassengerId()) {
                        passenger = u;
                        break;
                    }
                }
                if (passenger != null) {
                    b.setPassengerName(passenger.getName());
                    b.setPassengerPhone(passenger.getPhoneNo());
                    b.setPassengerEmail(passenger.getEmail());
                }
                result.add(b);
            }
        }
        return result;
    }
}

