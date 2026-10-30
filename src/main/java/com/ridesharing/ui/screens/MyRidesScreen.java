package com.ridesharing.ui.screens;

import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.dialogs.RideDetailsDialog;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * My Published Rides screen with table, ride details, and cancellation.
 */
public class MyRidesScreen extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Ride> myRides;

    public MyRidesScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PRIMARY);
        buildUI();
    }

    public void refresh() {
        loadData();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BG_PRIMARY);
        content.setBorder(AppTheme.sectionPadding());

        // ── Header ──
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel heading = AppTheme.subtitleLabel("My Published Rides");
        headerPanel.add(heading, BorderLayout.WEST);
        content.add(headerPanel, BorderLayout.NORTH);

        // ── Table ──
        String[] cols = {"Ride ID", "Route", "Departure", "Vehicle", "Total", "Confirmed",
                "Remaining", "₹/Seat", "Status", "Passengers", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 9 || c == 10; }
        };

        table = new JTable(tableModel);
        AppTheme.styleTable(table);

        // Status badge renderer
        table.getColumnModel().getColumn(8).setCellRenderer(
                new AppTheme.StatusBadgeRenderer());

        // View Passengers button column
        table.getColumnModel().getColumn(9).setCellRenderer(
                new FindRidesScreen.ButtonRenderer("View Passengers"));
        table.getColumnModel().getColumn(9).setCellEditor(
                new FindRidesScreen.ButtonEditor("View Passengers", this::viewPassengersForSelectedRide));

        // Cancel button column
        table.getColumnModel().getColumn(10).setCellRenderer(
                new FindRidesScreen.ButtonRenderer("Cancel"));
        table.getColumnModel().getColumn(10).setCellEditor(
                new FindRidesScreen.ButtonEditor("Cancel", this::cancelSelectedRide));

        // Center aligned columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(65);
        table.getColumnModel().getColumn(6).setPreferredWidth(65);
        table.getColumnModel().getColumn(7).setPreferredWidth(65);
        table.getColumnModel().getColumn(8).setPreferredWidth(85);
        table.getColumnModel().getColumn(9).setPreferredWidth(115);
        table.getColumnModel().getColumn(10).setPreferredWidth(75);

        // Double click to view ride details
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow >= 0 && myRides != null) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        if (modelRow < myRides.size()) {
                            Window window = SwingUtilities.getWindowAncestor(MyRidesScreen.this);
                            RideDetailsDialog dialog = new RideDetailsDialog(
                                    window instanceof JFrame ? (JFrame) window : null,
                                    myRides.get(modelRow));
                            dialog.setVisible(true);
                            loadData();
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT));
        sp.getViewport().setBackground(AppTheme.BG_CARD);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.BG_CARD);
        tableCard.setBorder(AppTheme.cardBorder());
        tableCard.add(sp, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(AppTheme.BG_PRIMARY);
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        User user = ServiceFactory.getAuthService().getCurrentUser();
        if (user == null) return;

        myRides = ServiceFactory.getRideService().getRidesByDriver(user.getUserId());
        tableModel.setRowCount(0);

        for (Ride r : myRides) {
            Vehicle v = ServiceFactory.getVehicleService().findById(r.getVehicleId());
            int confirmed = ServiceFactory.getBookingService().getConfirmedSeatsForRide(r.getRideId());
            int remaining = ServiceFactory.getRideService().getRemainingSeats(r.getRideId());

            tableModel.addRow(new Object[]{
                    "#" + r.getRideId(),
                    r.getSourceAddress() + " → " + r.getDestinationAddress(),
                    r.getDepartureTime().format(DT_FMT),
                    v != null ? v.getCarModel() : "N/A",
                    r.getNoOfSeats(),
                    confirmed,
                    remaining,
                    "₹" + r.getCostPerSeat().toPlainString(),
                    r.getRideStatus().name(),
                    "View Passengers",
                    r.getRideStatus() == Ride.RideStatus.AVAILABLE ? "Cancel" : "—"
            });
        }
    }

    private void viewPassengersForSelectedRide() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || myRides == null) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= myRides.size()) return;

        Ride ride = myRides.get(modelRow);
        Window window = SwingUtilities.getWindowAncestor(this);
        new com.ridesharing.ui.dialogs.ViewPassengersDialog(
                window instanceof JFrame ? (JFrame) window : null, ride).setVisible(true);
        loadData();
    }

    private void cancelSelectedRide() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || myRides == null) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= myRides.size()) return;

        Ride ride = myRides.get(modelRow);
        if (ride.getRideStatus() != Ride.RideStatus.AVAILABLE) {
            JOptionPane.showMessageDialog(this, "This ride cannot be cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel ride #" + ride.getRideId() + "?\n" +
                "Route: " + ride.getSourceAddress() + " → " + ride.getDestinationAddress() + "\n\n" +
                "All confirmed bookings for this ride will also be cancelled.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = ServiceFactory.getRideService().cancelRide(ride.getRideId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Ride cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel ride.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                loadData();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Cancel Ride", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling ride: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
