package com.ridesharing;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.ridesharing.model.Vehicle;
import com.ridesharing.util.DBConnection;

public class Main {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {

            System.out.println(
                    "Database connected successfully!");

            DatabaseMetaData metadata = connection.getMetaData();

            System.out.println(
                    "Database: "
                    + metadata.getDatabaseProductName());

            System.out.println(
                    "Version: "
                    + metadata.getDatabaseProductVersion());

        } catch (SQLException e) {

            System.out.println(
                    "Database connection failed!");

            e.printStackTrace();
        }

    }
}
