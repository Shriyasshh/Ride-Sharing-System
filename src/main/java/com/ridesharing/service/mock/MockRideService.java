package com.ridesharing.service.mock;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.service.interfaces.RideService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MockRideService implements RideService {

    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public Ride publishRide(Ride ride) {
        ride.setRideId(store.nextRideId());
        ride.setRideStatus(Ride.RideStatus.AVAILABLE);
        store.getRides().add(ride);
        return ride;
    }

    @Override
    public List<Ride> searchRides(String source, String destination, LocalDate date, int minSeats) {
        List<Ride> results = new ArrayList<>();
        for (Ride r : store.getRides()) {
            if (r.getRideStatus() != Ride.RideStatus.AVAILABLE) continue;

            boolean sourceMatch = source == null || source.isEmpty() ||
                    r.getSourceAddress().toLowerCase().contains(source.toLowerCase());
            boolean destMatch = destination == null || destination.isEmpty() ||
                    r.getDestinationAddress().toLowerCase().contains(destination.toLowerCase());
            boolean dateMatch = date == null ||
                    r.getDepartureTime().toLocalDate().equals(date);
            boolean seatMatch = minSeats <= 0 || getRemainingSeats(r.getRideId()) >= minSeats;

            if (sourceMatch && destMatch && dateMatch && seatMatch) {
                results.add(r);
            }
        }
        return results;
    }

    @Override
    public List<Ride> getRidesByDriver(int driverId) {
        List<Ride> result = new ArrayList<>();
        for (Ride r : store.getRides()) {
            if (r.getDriverId() == driverId) result.add(r);
        }
        return result;
    }

    @Override
    public List<Ride> getAllRides() {
        return new ArrayList<>(store.getRides());
    }

    @Override
    public Ride findById(int rideId) {
        for (Ride r : store.getRides()) {
            if (r.getRideId() == rideId) return r;
        }
        return null;
    }

    @Override
    public boolean cancelRide(int rideId) {
        Ride ride = findById(rideId);
        if (ride == null) return false;
        ride.setRideStatus(Ride.RideStatus.CANCELLED);
        // Cancel all confirmed bookings for this ride
        for (Booking b : store.getBookings()) {
            if (b.getRideId() == rideId && b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                b.setBookingStatus(Booking.BookingStatus.CANCELLED);
            }
        }
        return true;
    }

    @Override
    public int getRemainingSeats(int rideId) {
        Ride ride = findById(rideId);
        if (ride == null) return 0;
        int confirmed = 0;
        for (Booking b : store.getBookings()) {
            if (b.getRideId() == rideId && b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                confirmed += b.getSeatsBooked();
            }
        }
        return ride.getNoOfSeats() - confirmed;
    }
}
