package com.ridesharing.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ride {

    private int rideId;
    private int driverId;
    private int vehicleId;
    private int noOfSeats;
    private String sourceAddress;
    private String destinationAddress;
    private LocalDateTime departureTime;
    private BigDecimal costPerSeat;
    private RideStatus rideStatus;
    public enum RideStatus{
        AVAILABLE,
        CANCELLED,
        IN_PROGRESS
    }

    public Ride() {
    }

    public Ride(int driverId, int vehicleId,
                int noOfSeats, String sourceAddress,
                String destinationAddress,
                LocalDateTime departureTime,
                BigDecimal costPerSeat) {

        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.noOfSeats = noOfSeats;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.departureTime = departureTime;
        this.costPerSeat = costPerSeat;
        this.rideStatus = RideStatus.AVAILABLE;
    }

    // Getter
    public int getRideId() {
        return rideId;
    }

    public int getDriverId() {
        return driverId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public int getNoOfSeats() {
        return noOfSeats;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }


    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public BigDecimal getCostPerSeat() {
        return costPerSeat;
    }


    public RideStatus getRideStatus() {
        return rideStatus;
    }



    //Setter
    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setNoOfSeats(int noOfSeats) {
        this.noOfSeats = noOfSeats;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public void setCostPerSeat(BigDecimal costPerSeat) {
        this.costPerSeat = costPerSeat;
    }

    public void setRideStatus(RideStatus rideStatus) {
        this.rideStatus = rideStatus;
    }
}
