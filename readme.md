# RideShare — Java Swing Desktop Application

A commercial-grade, database-driven **PC Desktop Application** for ride sharing built with **Java 26**, **Java Swing**, **FlatLaf**, and **JDBC**. It allows drivers to publish journeys and passengers to search, book, and manage ride reservations with real-time seat availability calculation.

---

## 1. Visual Identity & UI/UX Design

The application follows a curated desktop design system:
- **Primary Color:** Deep Emerald (`#10553F`) & Dark Emerald (`#0A3C2C`)
- **Accent Color:** Restrained Mint (`#34C788`) and Light Mint (`#D1FAE5`)
- **Background:** Warm off-white (`#F5F5F0`) with crisp white card surfaces
- **Typography:** Segoe UI with hierarchical scaling and proper contrast
- **Status Indicators:** Pill badges with distinct colors:
  - `AVAILABLE` (Forest Green)
  - `CONFIRMED` (Steel Blue)
  - `CANCELLED` (Ruby Red)
  - `IN_PROGRESS` (Amber)
- **Responsive Layout:** Fixed left sidebar navigation, top header with profile menu and notifications, card metrics, sortable/filterable tables, and confirmation dialogs.

---

## 2. Complete Application Architecture

The project strictly follows layered architecture and separation of concerns:

```text
com.ridesharing
├── dao/                        # JDBC Database Access Objects
│   ├── RideDao.java            # Ride database operations (in progress)
│   ├── UserDAO.java            # User registration & queries
│   └── VehicleDAO.java         # Vehicle ownership & persistence
├── model/                      # Domain entities
│   ├── Booking.java            # Booking entity & BookingStatus enum
│   ├── Ride.java               # Ride entity & RideStatus enum
│   ├── User.java               # User account entity
│   └── Vehicle.java            # Vehicle entity
├── service/
│   ├── interfaces/             # Business logic contracts (UI calls these)
│   │   ├── AuthService.java
│   │   ├── BookingService.java
│   │   ├── RideService.java
│   │   ├── UserService.java
│   │   └── VehicleService.java
│   ├── mock/                   # In-memory realistic implementations
│   │   ├── MockAuthService.java
│   │   ├── MockBookingService.java
│   │   ├── MockDataStore.java  # Shared cross-screen data store
│   │   ├── MockRideService.java
│   │   ├── MockUserService.java
│   │   └── MockVehicleService.java
│   └── ServiceFactory.java     # Decoupled provider for service instances
├── ui/
│   ├── app/
│   │   └── MainFrame.java      # Root desktop frame with CardLayout routing
│   ├── components/             # Reusable UI widgets
│   │   ├── MetricCard.java     # KPI summary cards
│   │   └── TableActionCell.java# Table action buttons & dash renderers
│   ├── dialogs/
│   │   └── RideDetailsDialog.java # Booking review & fare calculation
│   ├── navigation/
│   │   ├── HeaderBar.java      # Top bar with profile menu & notifications
│   │   └── Sidebar.java        # Emerald left navigation sidebar
│   ├── screens/                # Interconnected desktop screens
│   │   ├── DashboardScreen.java
│   │   ├── FindRidesScreen.java
│   │   ├── LoginScreen.java
│   │   ├── MyBookingsScreen.java
│   │   ├── MyRidesScreen.java
│   │   ├── MyVehiclesScreen.java
│   │   ├── ProfileScreen.java
│   │   ├── PublishRideScreen.java
│   │   ├── RegisterScreen.java
│   │   ├── SettingsScreen.java
│   │   └── WelcomeScreen.java
│   └── theme/
│       └── AppTheme.java       # Centralized styling, colors, and fonts
├── util/
│   └── DBConnection.java       # JDBC connection manager
└── Main.java                   # Application launcher (EDT + FlatLaf)
```

---

## 3. Screen Overview & Functionality

1. **Welcome Screen:** Application branding, tagline, introduction, and direct actions to Sign In or Create Account.
2. **Registration Screen:** Full name, phone, email, gender dropdown, government ID, password confirmation, and input validation.
3. **Login Screen:** Email or phone number login, password visibility toggle, error feedback, and loading simulation.
4. **Dashboard:** Welcome greeting, 4 metric KPI cards (Active Bookings, Published Rides, My Vehicles, Total Bookings), quick action buttons, and split view for Recent Bookings and Upcoming Rides with status badges.
5. **Find Rides:** Search form with swap source/destination button, departure date filter, seat requirement filter, sortable results table, status badges, and "View Details" action.
6. **Ride Details Dialog:** Complete booking workflow showing route, departure time, driver, vehicle, and remaining seats. Includes an interactive seat spinner, real-time fare calculator, booking confirmation review, and demo booking confirmation.
7. **Publish a Ride:** Structured form with source, destination, departure date/time, vehicle selection dropdown (with quick `+ Add Vehicle` button), seats, and fare validation (rejects past departures and negative costs).
8. **My Published Rides:** Table of user's published rides with total vs confirmed vs remaining seats, status badges, ride cancellation with cascade booking cancellation, and double-click to view details.
9. **My Bookings:** Passenger reservation history with route, departure time, seats, total fare, booked timestamp, status badges, cancellation confirmation dialog, and double-click to view details.
10. **My Vehicles:** Vehicle inventory table showing vehicle ID and car model, add vehicle dialog with validation, vehicle deletion with confirmation, and empty state messaging.
11. **Profile:** Editable user details (name, phone, behaviour), read-only credentials (user ID, email, government ID), and lifetime activity summary grid.
12. **Settings:** Simulated notification toggles, application version metadata, mock mode notice, and account logout with confirmation.

---

## 4. Pre-seeded Demo Credentials & Data

The shared `MockDataStore` comes pre-populated with realistic users, vehicles, rides, and bookings:

| Name | Email | Phone | Password | Vehicles Owned |
| :--- | :--- | :--- | :--- | :--- |
| **Aarav Sharma** | `aarav@example.com` | `9876543210` | `demo123` | Maruti Swift, Hyundai i20 |
| **Priya Patel** | `priya@example.com` | `9876543211` | `demo123` | Tata Nexon EV |
| **Rohan Gupta** | `rohan@example.com` | `9876543212` | `demo123` | Honda City |
| **Ananya Singh** | `ananya@example.com` | `9876543213` | `demo123` | Toyota Innova |
| **Vikram Reddy** | `vikram@example.com` | `9876543214` | `demo123` | Mahindra XUV700 |

*You can sign in with any of the accounts above or register a brand-new account.*

---

## 5. Getting Started & Running the Application

### Prerequisites
- **JDK 26** (or JDK 21+)
- **Maven 3.8+**
- **IntelliJ IDEA** (recommended)

### Running from IntelliJ IDEA
1. Open the project directory `RideSharingSystem` in IntelliJ IDEA.
2. Let Maven import the dependencies (`pom.xml`).
3. Navigate to `src/main/java/com/ridesharing/Main.java`.
4. Click the green **Run** icon next to `main(String[] args)`.

### Running from Command Line (Maven)
```bash
# Compile and package
mvn clean compile

# Execute the application
mvn exec:java -Dexec.mainClass="com.ridesharing.Main"
```

---

## 6. How to Replace Mock Services with Real Backend Services

The UI never communicates with SQL or DAOs directly. All screen interactions go through the interfaces in `com.ridesharing.service.interfaces.*` via `ServiceFactory`.

When your backend DAOs (`RideDao`, `BookingDao`, etc.) are completed, follow these steps to switch to the real database:

### Step 1: Implement the Service Interface
Create a new class that implements the interface and calls your DAO:

```java
package com.ridesharing.service.real;

import com.ridesharing.dao.UserDAO;
import com.ridesharing.model.User;
import com.ridesharing.service.interfaces.AuthService;

public class RealAuthService implements AuthService {

    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    @Override
    public User login(String emailOrPhone, String password) {
        try {
            User user = userDAO.findByPhoneNo(emailOrPhone);
            if (user == null) {
                // query by email if implemented in UserDAO
            }
            if (user != null && password.equals(user.getPasswordHash())) {
                this.currentUser = user;
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User register(User user) throws IllegalArgumentException {
        try {
            int id = userDAO.registerUser(user);
            user.setUserId(id);
            this.currentUser = user;
            return user;
        } catch (Exception e) {
            throw new IllegalArgumentException("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public User getCurrentUser() { return currentUser; }

    @Override
    public void logout() { this.currentUser = null; }

    @Override
    public boolean isLoggedIn() { return currentUser != null; }
}
```

### Step 2: Swap the Implementation in `ServiceFactory.java`
In `com.ridesharing.service.ServiceFactory.java`, simply return your real service:

```java
public static synchronized AuthService getAuthService() {
    if (authService == null) {
        authService = new RealAuthService(); // Swapped from MockAuthService!
    }
    return authService;
}
```

**Zero UI changes are required.** The entire frontend will immediately interact with your real database.

---

## 7. Assumptions & Unfinished Integrations

- **Authentication:** In mock mode, passwords are stored and matched directly. In production, `RealAuthService` should use `BCrypt` or `Argon2` password hashing before storing or validating `password_hash`.
- **Database Schema Entities:**
  - `User`: `user_id`, `phone_no`, `name`, `gender`, `email`, `govt_id`, `behaviour`, `password_hash`
  - `Vehicle`: `vehicle_id`, `car_model`, `owner_id`
  - `Ride`: `ride_id`, `driver_id`, `vehicle_id`, `no_of_seats`, `source_address`, `destination_address`, `departure_time`, `cost_per_seat`, `ride_status` (`AVAILABLE`, `CANCELLED`, `IN_PROGRESS`)
  - `Booking`: `booking_id`, `ride_id`, `passenger_id`, `booking_time`, `seats_booked`, `booking_status` (`CONFIRMED`, `CANCELLED`)
- **DAO Status:**
  - `UserDAO`: Implemented (`registerUser`, `findById`, `findByPhoneNo`, `isPhoneNoExists`, `isEmailExists`).
  - `VehicleDAO`: Implemented.
  - `RideDao`: Interface and SQL queries under active development.
  - `BookingDao`: To be implemented to support `BookingService`.
- **Background DB Probe:** When launching `Main.java`, a non-blocking daemon thread probes the PostgreSQL / MariaDB connection via `DBConnection.getConnection()`. If the database is offline, the application seamlessly runs in self-contained Mock Mode without crashing or hanging.