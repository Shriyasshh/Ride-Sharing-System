package com.ridesharing.service.mock;

import com.ridesharing.model.Vehicle;
import com.ridesharing.service.interfaces.VehicleService;

import java.util.ArrayList;
import java.util.List;

public class MockVehicleService implements VehicleService {

    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public List<Vehicle> getVehiclesByOwner(int ownerId) {
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : store.getVehicles()) {
            if (v.getOwnerId() == ownerId) result.add(v);
        }
        return result;
    }

    @Override
    public Vehicle addVehicle(Vehicle vehicle) {
        vehicle.setVehicleId(store.nextVehicleId());
        store.getVehicles().add(vehicle);
        return vehicle;
    }

    @Override
    public Vehicle findById(int vehicleId) {
        for (Vehicle v : store.getVehicles()) {
            if (v.getVehicleId() == vehicleId) return v;
        }
        return null;
    }

    @Override
    public boolean deleteVehicle(int vehicleId) {
        return store.getVehicles().removeIf(v -> v.getVehicleId() == vehicleId);
    }

    @Override
    public boolean deleteVehicle(int vehicleId, int ownerId) {
        Vehicle v = findById(vehicleId);
        if (v == null) return false;
        if (v.getOwnerId() != ownerId) {
            throw new IllegalArgumentException("Unauthorized: You do not own this vehicle.");
        }
        return store.getVehicles().removeIf(veh -> veh.getVehicleId() == vehicleId);
    }
}

