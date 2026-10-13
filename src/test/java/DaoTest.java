import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.ridesharing.dao.UserDAO;
import com.ridesharing.dao.VehicleDAO;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;

public class DaoTest {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();

        try {
            // TEST 1: Register a user
            System.out.println("1.");
            String unique = UUID.randomUUID()
                    .toString().substring(0, 8);

            User user = new User();

            String testPhone = "9" + String.valueOf(
                    System.currentTimeMillis()).substring(3, 12);

            user.setPhoneNo(testPhone);

            user.setName("Shri");
            user.setGender("Male");
            user.setEmail("shri_" + unique + "@example.com");
            user.setGovtId("shri_" + unique);
            user.setBehaviour("Good");

            // Temporary test value only.
            // Replace with a real password hash if required.
            user.setPasswordHash("TEST_HASH");

            int userId = userDAO.registerUser(user);
            System.out.println("User registered: " + userId);

            // TEST 2: Find user by ID
            System.out.println("2.");
            User foundUser = userDAO.findById(userId);
            System.out.println("Find by ID: "
                    + (foundUser != null ? foundUser.getName() : "Not found"));

            // TEST 3: Find user by phone
            System.out.println("3.");
            User phoneUser = userDAO.findByPhoneNo(user.getPhoneNo());
            System.out.println("Find by phone: "
                    + (phoneUser != null ? phoneUser.getName() : "Not found"));

            // TEST 4: Check email and phone existence
            System.out.println("4.");
            System.out.println("Email exists: "
                    + userDAO.isEmailExists(user.getEmail()));

            System.out.println("Phone exists: "
                    + userDAO.isPhoneNoExists(user.getPhoneNo()));

            // TEST 5: Add a vehicle
            System.out.println("5.");
            Vehicle vehicle = new Vehicle();
            vehicle.setCarModel("Scorpio");
            vehicle.setOwnerId(userId);

            int vehicleId = vehicleDAO.addVehicle(vehicle);
            System.out.println("Vehicle added: " + vehicleId);

            // TEST 6: Find vehicle by ID
            System.out.println("6.");
            Vehicle foundVehicle = vehicleDAO.findVehicleById(vehicleId);

            System.out.println("Find vehicle: "
                    + (foundVehicle != null
                    ? foundVehicle.getCarModel()
                    : "Not found"));

            // TEST 7: Find vehicles by owner
            System.out.println("7.");
            List<Vehicle> vehicles = vehicleDAO.findVehiclesByOwner(userId);

            System.out.println("Vehicles owned: " + vehicles.size());

            // TEST 8: Delete vehicle
            System.out.println("8.");
            vehicleDAO.deleteVehicle(vehicleId);

            System.out.println("Vehicle deleted successfully.");

            // TEST 9: Confirm deletion
            System.out.println("9.");
            Vehicle deletedVehicle = vehicleDAO.findVehicleById(vehicleId);

            System.out.println("Vehicle exists after deletion: "
                    + (deletedVehicle != null));

        } catch (SQLException e) {
            System.err.println("DAO test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
