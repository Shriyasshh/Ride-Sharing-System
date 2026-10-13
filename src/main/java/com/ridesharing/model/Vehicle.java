package com.ridesharing.model;

public class Vehicle {

    private int vehicleId;
    private String carModel;
    private int ownerId;


    public Vehicle(){
    }
    public Vehicle(int vehicleId, String carModel, int ownerId){
        this.vehicleId = vehicleId;
        this.carModel = carModel;
        this.ownerId = ownerId;
    }

    // Getters
    public int getVehicleId(){
        return vehicleId;
    }

    public String getCarModel(){
        return carModel;
    }

    public  int getOwnerId(){
        return ownerId;
    }

    //Setter
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
