package com.ridesharing.model;

import java.time.LocalDateTime;

public class Booking {

    private int bookingId;
    private int rideId;
    private int passengerId;
    private LocalDateTime bookingTime;
    private int seatsBooked;
    private BookingStatus bookingStatus;

    public enum BookingStatus{
        CONFIRMED,
        CANCELLED
    }

    public Booking() {
    }

    public Booking(int rideId, int passengerId,int seatsBooked) {
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.seatsBooked = seatsBooked;
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getRideId() {
        return rideId;
    }

    public int getPassengerId() {
        return passengerId;
    }    
    
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public int getSeatsBooked() {
        return seatsBooked;
    }
    
    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }
    
    // Setter
    public void setBookingId(int bookingId) {
            this.bookingId = bookingId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

}
