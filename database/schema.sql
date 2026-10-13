CREATE DATABASE IF NOT EXISTS ride_sharing;


--        USE ride_sharing;

-- CREATE TABLE users (
--     user_id INT PRIMARY KEY AUTO_INCREMENT,
--     phone_no VARCHAR(15) NOT NULL UNIQUE,
--     name VARCHAR(100) NOT NULL,
--     gender VARCHAR(20) NOT NULL,
--     email VARCHAR(100) UNIQUE,
--     govt_id VARCHAR(50) UNIQUE,
--     behaviour VARCHAR(100) NULL,
--     password_hash VARCHAR(255) NOT NULL
-- );

--  CREATE TABLE vehicles (
--     vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
--     car_model VARCHAR(100) NOT NULL,
--     owner_id INT NOT NULL,

--     FOREIGN KEY (owner_id)
--         REFERENCES users(user_id)
-- );

--        CREATE TABLE rides (
--     ride_id INT PRIMARY KEY AUTO_INCREMENT,
--     driver_id INT NOT NULL,
--     vehicle_id INT NOT NULL,
--     no_of_seats INT NOT NULL,
--     source_address VARCHAR(200) NOT NULL,
--     destination_address VARCHAR(200) NOT NULL,
--     departure_time DATETIME NOT NULL,
--     cost_per_seat DECIMAL(10,2) NOT NULL,
--     ride_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

--     FOREIGN KEY (driver_id)
--         REFERENCES users(user_id),

--     FOREIGN KEY (vehicle_id)
--         REFERENCES vehicles(vehicle_id),

--     CHECK (no_of_seats > 0),
--     CHECK (cost_per_seat >= 0)
-- );

--      CREATE TABLE bookings (
--     booking_id INT PRIMARY KEY AUTO_INCREMENT,
--     ride_id INT NOT NULL,
--     passenger_id INT NOT NULL,
--     booking_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     seats_booked INT NOT NULL,
--     booking_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',

--     FOREIGN KEY (ride_id)
--         REFERENCES rides(ride_id),

--     FOREIGN KEY (passenger_id)
--         REFERENCES users(user_id),

--     UNIQUE (ride_id, passenger_id),
--     CHECK (seats_booked > 0)
-- );