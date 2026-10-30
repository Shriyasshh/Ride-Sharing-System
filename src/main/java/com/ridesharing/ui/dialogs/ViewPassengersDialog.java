package com.ridesharing.ui.dialogs;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog displaying passenger bookings for a ride to the ride's creator.
 * Shows passenger name, contact phone, email, seats booked, booking time, and status.
 * Supports filtering confirmed vs cancelled bookings with an informative empty state.
 */
public class ViewPassengersDialog extends JDialog {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final Ride ride;
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> filterCombo;
    private JLabel summaryLabel;
    private JLabel emptyLabel;
    private JScrollPane scrollPane;
    private List<Booking> cachedBookings = new ArrayList<>();

    public ViewPassengersDialog(JFrame parent, Ride ride) {
        super(parent, "Passenger Details — Ride #" + (ride != null ? ride.getRideId() : ""), true);
        this.ride = ride;
        setSize(780, 560);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(650, 450));
        buildUI();
        loadPassengers();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(AppTheme.BG_PRIMARY);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── Top Header ──
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = AppTheme.subtitleLabel("Passengers for Ride #" + (ride != null ? ride.getRideId() : ""));
        JLabel statusBadge = AppTheme.statusBadge(ride != null ? ride.getRideStatus().name() : "AVAILABLE");
        titleRow.add(titleLabel);
        titleRow.add(statusBadge);

        String route = ride != null ? ride.getSourceAddress() + " \u2192 " + ride.getDestinationAddress() : "";
        String departure = (ride != null && ride.getDepartureTime() != null) ? ride.getDepartureTime().format(DT_FMT) : "";
        JLabel subLabel = AppTheme.secondaryLabel(route + "  \u2022  Departure: " + departure);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleRow);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLabel);
        headerPanel.add(Box.createVerticalStrut(12));

        // ── Controls Row (Filter + Refresh + Summary) ──
        JPanel controlsCard = new JPanel(new BorderLayout(12, 0));
        controlsCard.setBackground(AppTheme.BG_CARD);
        controlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT),
                new EmptyBorder(10, 16, 10, 16)
        ));
        controlsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftControls.setOpaque(false);
        leftControls.add(AppTheme.bodyLabel("Filter:"));

        filterCombo = AppTheme.styledComboBox(new String[]{"Confirmed Only (Default)", "All Bookings", "Cancelled Only"});
        filterCombo.setPreferredSize(new Dimension(210, AppTheme.INPUT_HEIGHT));
        filterCombo.addActionListener(e -> applyFilter());
        leftControls.add(filterCombo);

        JButton refreshBtn = AppTheme.secondaryButton("\u21BB Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, AppTheme.INPUT_HEIGHT));
        refreshBtn.addActionListener(e -> loadPassengers());
        leftControls.add(refreshBtn);

        summaryLabel = AppTheme.secondaryLabel("Loading passengers...");

        controlsCard.add(leftControls, BorderLayout.WEST);
        controlsCard.add(summaryLabel, BorderLayout.EAST);

        headerPanel.add(controlsCard);
        main.add(headerPanel, BorderLayout.NORTH);

        // ── Table & Empty State ──
        String[] cols = {"Booking ID", "Passenger Name", "Phone", "Email", "Seats", "Booking Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        AppTheme.styleTable(table);

        // Status badge renderer for status column
        table.getColumnModel().getColumn(6).setCellRenderer(new AppTheme.StatusBadgeRenderer());

        // Center align ID, Seats, Status
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(85);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(95);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(AppTheme.BG_CARD);

        emptyLabel = new JLabel("No passengers have booked this ride yet.", SwingConstants.CENTER);
        emptyLabel.setFont(AppTheme.FONT_BODY);
        emptyLabel.setForeground(AppTheme.TEXT_MUTED);
        emptyLabel.setBorder(new EmptyBorder(60, 20, 60, 20));
        emptyLabel.setVisible(false);

        JPanel centerContainer = new JPanel(new CardLayout());
        centerContainer.setBackground(AppTheme.BG_CARD);
        centerContainer.setBorder(AppTheme.cardBorder());
        centerContainer.add(scrollPane, "table");
        centerContainer.add(emptyLabel, "empty");

        main.add(centerContainer, BorderLayout.CENTER);

        // ── Bottom Action ──
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        JButton closeBtn = AppTheme.secondaryButton("Close");
        closeBtn.setPreferredSize(new Dimension(100, AppTheme.BUTTON_HEIGHT));
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);

        main.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void loadPassengers() {
        if (ride == null) return;

        User currentUser = ServiceFactory.getAuthService().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "You must be logged in to view passenger details.",
                    "Authentication Required", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        try {
            cachedBookings = ServiceFactory.getBookingService().getPassengersByRide(ride.getRideId(), currentUser.getUserId());
            applyFilter();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load passenger details: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        int filterIndex = filterCombo.getSelectedIndex();
        tableModel.setRowCount(0);

        List<Booking> filtered = new ArrayList<>();
        int confirmedCount = 0;
        int confirmedSeats = 0;
        int cancelledCount = 0;

        for (Booking b : cachedBookings) {
            if (b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                confirmedCount++;
                confirmedSeats += b.getSeatsBooked();
            } else if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                cancelledCount++;
            }

            if (filterIndex == 0) { // Confirmed Only (Default)
                if (b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                    filtered.add(b);
                }
            } else if (filterIndex == 2) { // Cancelled Only
                if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                    filtered.add(b);
                }
            } else { // All Bookings
                filtered.add(b);
            }
        }

        for (Booking b : filtered) {
            String bookingTimeStr = b.getBookingTime() != null ? b.getBookingTime().format(DT_FMT) : "N/A";
            String passengerName = b.getPassengerName() != null ? b.getPassengerName() : "Passenger #" + b.getPassengerId();
            String phone = b.getPassengerPhone() != null ? b.getPassengerPhone() : "N/A";
            String email = b.getPassengerEmail() != null ? b.getPassengerEmail() : "N/A";

            tableModel.addRow(new Object[]{
                    "#" + b.getBookingId(),
                    passengerName,
                    phone,
                    email,
                    b.getSeatsBooked(),
                    bookingTimeStr,
                    b.getBookingStatus().name()
            });
        }

        // Summary text
        summaryLabel.setText("Confirmed: " + confirmedSeats + " seat(s) across " + confirmedCount +
                " booking(s)  \u2022  Cancelled: " + cancelledCount);

        // Empty state handling
        CardLayout cl = (CardLayout) ((JPanel) getContentPane().getComponent(1)).getLayout();
        JPanel centerContainer = (JPanel) getContentPane().getComponent(1);

        if (filtered.isEmpty()) {
            if (cachedBookings.isEmpty()) {
                emptyLabel.setText("No passengers have booked this ride yet.");
            } else if (filterIndex == 0) {
                emptyLabel.setText("No active confirmed bookings found for this ride (Cancelled bookings available in filter).");
            } else {
                emptyLabel.setText("No bookings match the selected filter.");
            }
            cl.show(centerContainer, "empty");
        } else {
            cl.show(centerContainer, "table");
        }
    }
}
