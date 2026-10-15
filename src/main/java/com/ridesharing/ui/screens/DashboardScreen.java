package com.ridesharing.ui.screens;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard with summary cards, recent bookings, upcoming rides, and quick actions.
 */
public class DashboardScreen extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private Runnable onFindRides;
    private Runnable onPublishRide;

    public DashboardScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PRIMARY);
    }

    public void setOnFindRides(Runnable r) { this.onFindRides = r; }
    public void setOnPublishRide(Runnable r) { this.onPublishRide = r; }

    public void refresh() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        User user = ServiceFactory.getAuthService().getCurrentUser();
        if (user == null) return;

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_PRIMARY);
        content.setBorder(AppTheme.sectionPadding());

        // ── Welcome ──
        JLabel welcome = new JLabel("Welcome back, " + user.getName() + "!");
        welcome.setFont(AppTheme.FONT_TITLE);
        welcome.setForeground(AppTheme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeSub = AppTheme.secondaryLabel("Here's your ride-sharing overview.");
        welcomeSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(welcome);
        content.add(Box.createVerticalStrut(4));
        content.add(welcomeSub);
        content.add(Box.createVerticalStrut(24));

        // ── Summary Cards ──
        List<Booking> myBookings = ServiceFactory.getBookingService()
                .getBookingsByPassenger(user.getUserId());
        long activeBookings = myBookings.stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED).count();

        List<Ride> myRides = ServiceFactory.getRideService()
                .getRidesByDriver(user.getUserId());
        long activeRides = myRides.stream()
                .filter(r -> r.getRideStatus() == Ride.RideStatus.AVAILABLE).count();

        List<Vehicle> myVehicles = ServiceFactory.getVehicleService()
                .getVehiclesByOwner(user.getUserId());

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        cardsPanel.add(createMetricCard("\u2611", "Active Bookings",
                String.valueOf(activeBookings), AppTheme.STATUS_CONFIRMED, AppTheme.STATUS_CONFIRMED_BG));
        cardsPanel.add(createMetricCard("\u2630", "Published Rides",
                String.valueOf(activeRides), AppTheme.PRIMARY, AppTheme.ACCENT_LIGHT));
        cardsPanel.add(createMetricCard("\u26DF", "My Vehicles",
                String.valueOf(myVehicles.size()), AppTheme.STATUS_IN_PROGRESS, AppTheme.STATUS_IN_PROGRESS_BG));
        cardsPanel.add(createMetricCard("\u2B50", "Total Bookings",
                String.valueOf(myBookings.size()), new Color(130, 80, 170), new Color(243, 232, 255)));

        content.add(cardsPanel);
        content.add(Box.createVerticalStrut(24));

        // ── Quick Actions ──
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton findBtn = AppTheme.primaryButton("\u2315  Find a Ride");
        findBtn.addActionListener(e -> { if (onFindRides != null) onFindRides.run(); });

        JButton publishBtn = AppTheme.secondaryButton("\u2795  Publish a Ride");
        publishBtn.addActionListener(e -> { if (onPublishRide != null) onPublishRide.run(); });

        actionsPanel.add(findBtn);
        actionsPanel.add(publishBtn);

        content.add(actionsPanel);
        content.add(Box.createVerticalStrut(28));

        // ── Two-column: Recent Bookings + Upcoming Rides ──
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        columnsPanel.setOpaque(false);
        columnsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        columnsPanel.add(buildRecentBookingsPanel(myBookings));
        columnsPanel.add(buildUpcomingRidesPanel(myRides));

        content.add(columnsPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(AppTheme.BG_PRIMARY);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String icon, String label, String value,
                                    Color accentColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(AppTheme.cardBorder());

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 28));
        iconLbl.setForeground(accentColor);
        iconLbl.setOpaque(true);
        iconLbl.setBackground(bgColor);
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconLbl.setPreferredSize(new Dimension(52, 52));
        iconLbl.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLbl.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(AppTheme.FONT_SMALL);
        labelLbl.setForeground(AppTheme.TEXT_SECONDARY);

        textPanel.add(valueLbl);
        textPanel.add(labelLbl);

        card.add(iconLbl, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildRecentBookingsPanel(List<Booking> bookings) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BG_CARD);
        panel.setBorder(AppTheme.cardBorder());

        JLabel heading = AppTheme.headingLabel("Recent Bookings");
        panel.add(heading, BorderLayout.NORTH);

        if (bookings.isEmpty()) {
            JLabel empty = AppTheme.secondaryLabel("No bookings yet. Find a ride to get started!");
            empty.setBorder(new EmptyBorder(20, 0, 20, 0));
            panel.add(empty, BorderLayout.CENTER);
        } else {
            String[] cols = {"Booking", "Route", "Seats", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

            int count = 0;
            for (Booking b : bookings) {
                if (count >= 5) break;
                Ride ride = ServiceFactory.getRideService().findById(b.getRideId());
                String route = ride != null ? ride.getSourceAddress() + " → " + ride.getDestinationAddress() : "N/A";
                model.addRow(new Object[]{
                        "DEMO-B" + b.getBookingId(),
                        route,
                        b.getSeatsBooked(),
                        b.getBookingStatus().name()
                });
                count++;
            }

            JTable table = new JTable(model);
            AppTheme.styleTable(table);
            table.setPreferredScrollableViewportSize(new Dimension(0, 200));

            // Status badge renderer
            table.getColumnModel().getColumn(3).setCellRenderer(new AppTheme.StatusBadgeRenderer());

            // Center align booking ID and seats
            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(0).setCellRenderer(center);
            table.getColumnModel().getColumn(2).setCellRenderer(center);

            // Double click to view ride details
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0 && row < bookings.size()) {
                            Booking b = bookings.get(row);
                            Ride ride = ServiceFactory.getRideService().findById(b.getRideId());
                            if (ride != null) {
                                Window win = SwingUtilities.getWindowAncestor(DashboardScreen.this);
                                new com.ridesharing.ui.dialogs.RideDetailsDialog(
                                        win instanceof JFrame ? (JFrame) win : null, ride).setVisible(true);
                                refresh();
                            }
                        }
                    }
                }
            });

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_LIGHT));
            panel.add(sp, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel buildUpcomingRidesPanel(List<Ride> rides) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BG_CARD);
        panel.setBorder(AppTheme.cardBorder());

        JLabel heading = AppTheme.headingLabel("My Upcoming Rides");
        panel.add(heading, BorderLayout.NORTH);

        List<Ride> upcoming = rides.stream()
                .filter(r -> r.getRideStatus() == Ride.RideStatus.AVAILABLE)
                .toList();

        if (upcoming.isEmpty()) {
            JLabel empty = AppTheme.secondaryLabel("No upcoming rides. Publish one!");
            empty.setBorder(new EmptyBorder(20, 0, 20, 0));
            panel.add(empty, BorderLayout.CENTER);
        } else {
            String[] cols = {"Ride", "Route", "Departure", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

            int count = 0;
            for (Ride r : upcoming) {
                if (count >= 5) break;
                model.addRow(new Object[]{
                        "DEMO-R" + r.getRideId(),
                        r.getSourceAddress() + " → " + r.getDestinationAddress(),
                        r.getDepartureTime().format(DT_FMT),
                        r.getRideStatus().name()
                });
                count++;
            }

            JTable table = new JTable(model);
            AppTheme.styleTable(table);
            table.setPreferredScrollableViewportSize(new Dimension(0, 200));

            // Status badge renderer
            table.getColumnModel().getColumn(3).setCellRenderer(new AppTheme.StatusBadgeRenderer());

            // Center align ride ID
            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(0).setCellRenderer(center);

            // Double click to view ride details
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0 && row < upcoming.size()) {
                            Ride r = upcoming.get(row);
                            Window win = SwingUtilities.getWindowAncestor(DashboardScreen.this);
                            new com.ridesharing.ui.dialogs.RideDetailsDialog(
                                    win instanceof JFrame ? (JFrame) win : null, r).setVisible(true);
                            refresh();
                        }
                    }
                }
            });

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_LIGHT));
            panel.add(sp, BorderLayout.CENTER);
        }

        return panel;
    }
}
