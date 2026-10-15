package com.ridesharing.ui.screens;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.dialogs.RideDetailsDialog;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * My Bookings screen with table, details, and cancellation.
 */
public class MyBookingsScreen extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Booking> myBookings;

    public MyBookingsScreen() {
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

        JLabel heading = AppTheme.subtitleLabel("My Bookings");
        content.add(heading, BorderLayout.NORTH);

        // ── Table ──
        String[] cols = {"Booking ID", "Route", "Departure", "Seats", "Fare", "Booked At", "Status", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        table = new JTable(tableModel);
        AppTheme.styleTable(table);

        // Status badge renderer
        table.getColumnModel().getColumn(6).setCellRenderer(
                new AppTheme.StatusBadgeRenderer());

        // Cancel button
        table.getColumnModel().getColumn(7).setCellRenderer(
                new FindRidesScreen.ButtonRenderer("Cancel"));
        table.getColumnModel().getColumn(7).setCellEditor(
                new FindRidesScreen.ButtonEditor("Cancel", this::cancelSelectedBooking));

        // Center aligned columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Double click to view ride details
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow >= 0 && myBookings != null) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        if (modelRow < myBookings.size()) {
                            Booking b = myBookings.get(modelRow);
                            Ride ride = ServiceFactory.getRideService().findById(b.getRideId());
                            if (ride != null) {
                                Window window = SwingUtilities.getWindowAncestor(MyBookingsScreen.this);
                                RideDetailsDialog dialog = new RideDetailsDialog(
                                        window instanceof JFrame ? (JFrame) window : null,
                                        ride);
                                dialog.setVisible(true);
                                loadData();
                            }
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

        myBookings = ServiceFactory.getBookingService().getBookingsByPassenger(user.getUserId());
        tableModel.setRowCount(0);

        for (Booking b : myBookings) {
            Ride ride = ServiceFactory.getRideService().findById(b.getRideId());
            String route = ride != null ? ride.getSourceAddress() + " → " + ride.getDestinationAddress() : "N/A";
            String departure = ride != null ? ride.getDepartureTime().format(DT_FMT) : "N/A";
            BigDecimal fare = ride != null ? ride.getCostPerSeat().multiply(BigDecimal.valueOf(b.getSeatsBooked())) : BigDecimal.ZERO;

            tableModel.addRow(new Object[]{
                    "DEMO-B" + b.getBookingId(),
                    route,
                    departure,
                    b.getSeatsBooked(),
                    "₹" + fare.toPlainString(),
                    b.getBookingTime() != null ? b.getBookingTime().format(DT_FMT) : "N/A",
                    b.getBookingStatus().name(),
                    b.getBookingStatus() == Booking.BookingStatus.CONFIRMED ? "Cancel" : "—"
            });
        }
    }

    private void cancelSelectedBooking() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || myBookings == null) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= myBookings.size()) return;

        Booking booking = myBookings.get(modelRow);
        if (booking.getBookingStatus() != Booking.BookingStatus.CONFIRMED) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Ride ride = ServiceFactory.getRideService().findById(booking.getRideId());
        String route = ride != null ? ride.getSourceAddress() + " → " + ride.getDestinationAddress() : "N/A";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel booking DEMO-B" + booking.getBookingId() + "?\n\n" +
                "Route: " + route + "\n" +
                "Seats: " + booking.getSeatsBooked() + "\n\n" +
                "This action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ServiceFactory.getBookingService().cancelBooking(booking.getBookingId());
            JOptionPane.showMessageDialog(this, "Booking cancelled successfully.",
                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }
}
