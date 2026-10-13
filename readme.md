# Ride Sharing System

A Java-based ride-sharing application that allows drivers to publish rides and passengers to book available seats. Built using Java, JDBC, and MariaDB, following object-oriented programming principles and a layered architecture.

## Tech Stack

- **Java:** 26
- **Database:** MariaDB (XAMPP)
- **Database Connectivity:** JDBC
- **Build Tool:** Maven
- **IDE:** IntelliJ IDEA
- **GUI:** Java Swing *(planned)*

## Features

- User registration and management
- Vehicle management
- Ride creation and management
- Ride search and seat availability tracking
- Passenger booking and cancellation
- Database-backed data persistence

*Features are being implemented progressively.*

## Project Structure

```text
ride-sharing-system/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/ridesharing/
│   │           ├── dao/
│   │           ├── model/
│   │           ├── service/
│   │           ├── util/
│   │           └── Main.java
│   └── test/
├── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- JDK 26
- IntelliJ IDEA
- XAMPP
- Maven

### Installation

1. Clone the repository:

   ```bash
   git clone <your-repository-url>
   cd ride-sharing-system
   ```

2. Start the database service from the XAMPP Control Panel.

3. Open phpMyAdmin at `http://localhost/phpmyadmin`.

4. Create the database:

   ```sql
   CREATE DATABASE ride_sharing;
   ```

5. Create the required tables using the project's database schema.

6. Configure `DBConnection.java` with your local database URL, username, and password.

7. Reload the Maven project in IntelliJ IDEA.

8. Run `Main.java` or your DAO test class.

## Database Schema

The application uses four main entities:

- **Users** — stores user and account information.
- **Vehicles** — stores vehicles owned by users.
- **Rides** — stores ride details, routes, departure times, seats, and fares.
- **Bookings** — stores passenger reservations and booking statuses.

Foreign keys maintain relationships between users, vehicles, rides, and bookings.

## Development Status

- [x] Database setup
- [x] Model classes
- [x] JDBC database connection
- [x] User DAO
- [x] Vehicle DAO
- [ ] Ride DAO testing and completion
- [ ] Booking DAO
- [ ] Service layer
- [ ] Authentication
- [ ] Java Swing GUI
- [ ] Integration testing

## Contributing

Contributions, suggestions, and bug reports are welcome. Feel free to open an issue or submit a pull request.

## License

This project is currently developed for academic and learning purposes. A license has not yet been selected.