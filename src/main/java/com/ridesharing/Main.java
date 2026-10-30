package com.ridesharing;

import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.app.MainFrame;
import com.ridesharing.ui.theme.AppTheme;
import com.ridesharing.util.DBConnection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Ride Sharing Desktop Application — Main Entry Point.
 *
 * Sets up FlatLaf modern Look & Feel, checks database connectivity
 * asynchronously in the background, and initializes the desktop GUI on the EDT.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Install FlatLaf modern desktop Look & Feel and global UI defaults
        AppTheme.install();

        // 2. Connect to real database and configure ServiceFactory
        initBackendServices();

        // 3. Launch the desktop application on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

    /**
     * Initializes backend services by verifying database connectivity.
     * Activates real PostgreSQL services if reachable; falls back to mock mode if offline.
     */
    private static void initBackendServices() {
        try (Connection connection = DBConnection.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            System.out.println("[INFO] Database connected: "
                    + metadata.getDatabaseProductName() + " "
                    + metadata.getDatabaseProductVersion());
            ServiceFactory.setUseRealServices(true);
        } catch (Exception e) {
            System.out.println("[INFO] Real database not reachable (" + e.getMessage() + "). Application running in self-contained Mock Mode.");
            ServiceFactory.setUseRealServices(false);
        }
    }
}

