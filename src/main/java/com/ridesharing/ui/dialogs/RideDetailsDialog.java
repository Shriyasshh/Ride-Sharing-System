package com.ridesharing.ui.dialogs;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Ride details dialog with booking workflow:
 * seat selector, total fare calculation, booking confirmation.
 */
public class RideDetailsDialog extends JDialog {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final Ride ride;
    private JSpinner seatSpinner;
    private JLabel totalFareLabel;
    private JButton bookBtn;

    public RideDetailsDialog(JFrame parent, Ride ride) {
        super(parent, "Ride Details", true);
        this.ride = ride;
        setSize(520, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(AppTheme.BG_CARD);
        main.setBorder(new EmptyBorder(24, 28, 24, 28));

        User driver = ServiceFactory.getUserService().findById(ride.getDriverId());
        Vehicle vehicle = ServiceFactory.getVehicleService().findById(ride.getVehicleId());
        int remaining = ServiceFactory.getRideService().getRemainingSeats(ride.getRideId());
        User currentUser = ServiceFactory.getAuthService().getCurrentUser();
        boolean isOwnRide = currentUser != null && currentUser.getUserId() == ride.getDriverId();

        // ── Header ──
        JLabel titleLabel = AppTheme.subtitleLabel("Ride #" + ride.getRideId());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusBadge = AppTheme.statusBadge(ride.getRideStatus().name());
        statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(statusBadge);

        main.add(headerPanel);
        main.add(Box.createVerticalStrut(20));

        // ── Route ──
        main.add(detailRow("\u25B6 From", ride.getSourceAddress()));
        main.add(detailRow("\u25BC To", ride.getDestinationAddress()));
        main.add(detailRow("\u23F0 Departure", ride.getDepartureTime().format(DT_FMT)));
        main.add(Box.createVerticalStrut(12));
        main.add(AppTheme.horizontalSeparator());
        main.add(Box.createVerticalStrut(12));

        // ── Driver & Vehicle ──
        main.add(detailRow("\uD83D\uDE97 Driver", driver != null ? driver.getName() : "Unknown"));
        main.add(detailRow("\uD83D\uDE98 Vehicle", vehicle != null ? vehicle.getCarModel() : "Unknown"));
        main.add(detailRow("\uD83D\uDCBA Total Seats", String.valueOf(ride.getNoOfSeats())));
        main.add(detailRow("\u2705 Remaining", String.valueOf(remaining)));
        main.add(detailRow("\u20B9 Cost/Seat", "\u20B9 " + ride.getCostPerSeat().toPlainString()));

        main.add(Box.createVerticalStrut(12));
        main.add(AppTheme.horizontalSeparator());
        main.add(Box.createVerticalStrut(16));

        // ── Booking Section ──
        boolean canBook = ride.getRideStatus() == Ride.RideStatus.AVAILABLE
                && remaining > 0 && !isOwnRide;

        if (canBook) {
            JLabel bookingHeading = AppTheme.headingLabel("Book Seats");
            bookingHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
            main.add(bookingHeading);
            main.add(Box.createVerticalStrut(12));

            JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
            bookPanel.setOpaque(false);
            bookPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            bookPanel.add(AppTheme.bodyLabel("Seats:"));
            seatSpinner = new JSpinner(new SpinnerNumberModel(1, 1, remaining, 1));
            seatSpinner.setFont(AppTheme.FONT_BODY);
            seatSpinner.setPreferredSize(new Dimension(70, AppTheme.INPUT_HEIGHT));
            seatSpinner.addChangeListener(e -> updateTotalFare());
            bookPanel.add(seatSpinner);

            bookPanel.add(Box.createHorizontalStrut(16));
            bookPanel.add(AppTheme.bodyLabel("Total Fare:"));
            totalFareLabel = new JLabel("\u20B9 " + ride.getCostPerSeat().toPlainString());
            totalFareLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            totalFareLabel.setForeground(AppTheme.PRIMARY);
            bookPanel.add(totalFareLabel);

            main.add(bookPanel);
            main.add(Box.createVerticalStrut(16));

            bookBtn = AppTheme.primaryButton("Confirm Booking");
            bookBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            bookBtn.addActionListener(e -> attemptBooking());
            main.add(bookBtn);
        } else {
            String reason;
            if (isOwnRide) reason = "You cannot book your own ride.";
            else if (remaining <= 0) reason = "No seats remaining.";
            else reason = "This ride is not available for booking.";

            JLabel notAvailable = AppTheme.secondaryLabel(reason);
            notAvailable.setAlignmentX(Component.LEFT_ALIGNMENT);
            main.add(notAvailable);

            if (isOwnRide) {
                main.add(Box.createVerticalStrut(12));
                JButton viewPassengersBtn = AppTheme.primaryButton("\uD83D\uDC65  View Passengers");
                viewPassengersBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                viewPassengersBtn.addActionListener(e -> {
                    Window win = SwingUtilities.getWindowAncestor(this);
                    new ViewPassengersDialog(win instanceof JFrame ? (JFrame) win : null, ride).setVisible(true);
                });
                main.add(viewPassengersBtn);
            }
        }

        main.add(Box.createVerticalStrut(16));

        JButton closeBtn = AppTheme.secondaryButton("Close");
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());
        main.add(closeBtn);

        JScrollPane sp = new JScrollPane(main);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppTheme.BG_CARD);
        setContentPane(sp);
    }

    private JPanel detailRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(AppTheme.FONT_BODY_BOLD);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(130, 22));
        row.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(AppTheme.FONT_BODY);
        val.setForeground(AppTheme.TEXT_PRIMARY);
        row.add(val);

        return row;
    }

    private void updateTotalFare() {
        int seats = (int) seatSpinner.getValue();
        BigDecimal total = ride.getCostPerSeat().multiply(BigDecimal.valueOf(seats));
        totalFareLabel.setText("\u20B9 " + total.toPlainString());
    }

    private void attemptBooking() {
        int seats = (int) seatSpinner.getValue();
        BigDecimal total = ride.getCostPerSeat().multiply(BigDecimal.valueOf(seats));
        User currentUser = ServiceFactory.getAuthService().getCurrentUser();

        // Booking review confirmation
        int confirm = JOptionPane.showConfirmDialog(this,
                "Booking Review:\n\n" +
                "Route: " + ride.getSourceAddress() + " → " + ride.getDestinationAddress() + "\n" +
                "Departure: " + ride.getDepartureTime().format(DT_FMT) + "\n" +
                "Seats: " + seats + "\n" +
                "Total Fare: ₹" + total.toPlainString() + "\n\n" +
                "Confirm this booking?",
                "Confirm Booking",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Booking booking = ServiceFactory.getBookingService()
                    .createBooking(ride.getRideId(), currentUser.getUserId(), seats);

            JOptionPane.showMessageDialog(this,
                    "Booking Confirmed!\n\n" +
                    "Booking ID: #" + booking.getBookingId() + "\n" +
                    "Seats: " + seats + "\n" +
                    "Total: ₹" + total.toPlainString(),
                    "Booking Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Booking Failed",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred: " + ex.getMessage(),
                    "Booking Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
