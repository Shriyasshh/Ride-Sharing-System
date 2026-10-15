package com.ridesharing;

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

        // 2. Perform non-blocking background check for database connection
        checkDatabaseInBackground();

        // 3. Launch the desktop application on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

    /**
     * Non-blocking database probe.
     * If the database is available, logs connectivity details.
     * If offline, notes that the application continues running in mock mode.
     */
    private static void checkDatabaseInBackground() {
        Thread dbThread = new Thread(() -> {
            try (Connection connection = DBConnection.getConnection()) {
                DatabaseMetaData metadata = connection.getMetaData();
                System.out.println("[INFO] Database connected: "
                        + metadata.getDatabaseProductName() + " "
                        + metadata.getDatabaseProductVersion());
            } catch (Exception e) {
                System.out.println("[INFO] Real database not reachable (" + e.getMessage()
                        + "). Application running in self-contained Mock Mode.");
            }
        }, "DB-Check-Worker");
        dbThread.setDaemon(true);
        dbThread.start();
    }
}
