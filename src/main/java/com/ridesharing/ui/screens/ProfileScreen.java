package com.ridesharing.ui.screens;

import com.ridesharing.model.Booking;
import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Profile screen: view and edit user details, view activity summary.
 */
public class ProfileScreen extends JPanel {

    private JTextField nameField, phoneField, emailField, govtIdField, behaviourField;
    private JLabel genderLabel;

    public ProfileScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PRIMARY);
    }

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

        JLabel heading = AppTheme.subtitleLabel("My Profile");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(20));

        // ── Profile Card ──
        JPanel profileCard = new JPanel(new GridBagLayout());
        profileCard.setBackground(AppTheme.BG_CARD);
        profileCard.setBorder(AppTheme.cardBorder());
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.setMaximumSize(new Dimension(600, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 12);

        nameField = AppTheme.styledTextField("");
        nameField.setText(user.getName());
        phoneField = AppTheme.styledTextField("");
        phoneField.setText(user.getPhoneNo() != null ? user.getPhoneNo() : "");
        emailField = AppTheme.styledTextField("");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        emailField.setEditable(false);
        emailField.setBackground(AppTheme.BG_SECONDARY);
        govtIdField = AppTheme.styledTextField("");
        govtIdField.setText(user.getGovtId() != null ? user.getGovtId() : "");
        govtIdField.setEditable(false);
        govtIdField.setBackground(AppTheme.BG_SECONDARY);
        behaviourField = AppTheme.styledTextField("");
        behaviourField.setText(user.getBehaviour() != null ? user.getBehaviour() : "");

        genderLabel = AppTheme.bodyLabel(user.getGender() != null ? user.getGender() : "Not set");

        int row = 0;
        addFormRow(profileCard, gbc, row++, "User ID", AppTheme.bodyLabel(String.valueOf(user.getUserId())));
        addFormRow(profileCard, gbc, row++, "Full Name", nameField);
        addFormRow(profileCard, gbc, row++, "Phone", phoneField);
        addFormRow(profileCard, gbc, row++, "Email", emailField);
        addFormRow(profileCard, gbc, row++, "Gender", genderLabel);
        addFormRow(profileCard, gbc, row++, "Government ID", govtIdField);
        addFormRow(profileCard, gbc, row++, "Behaviour", behaviourField);

        content.add(profileCard);
        content.add(Box.createVerticalStrut(16));

        // Save button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = AppTheme.primaryButton("Save Changes");
        saveBtn.addActionListener(e -> saveProfile(user));
        btnPanel.add(saveBtn);

        content.add(btnPanel);
        content.add(Box.createVerticalStrut(28));

        // ── Activity Summary ──
        JLabel activityHeading = AppTheme.headingLabel("Activity Summary");
        activityHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(activityHeading);
        content.add(Box.createVerticalStrut(12));

        List<Booking> bookings = ServiceFactory.getBookingService().getBookingsByPassenger(user.getUserId());
        List<Ride> rides = ServiceFactory.getRideService().getRidesByDriver(user.getUserId());
        List<Vehicle> vehicles = ServiceFactory.getVehicleService().getVehiclesByOwner(user.getUserId());

        long confirmedBookings = bookings.stream().filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED).count();
        long cancelledBookings = bookings.stream().filter(b -> b.getBookingStatus() == Booking.BookingStatus.CANCELLED).count();
        long activeRides = rides.stream().filter(r -> r.getRideStatus() == Ride.RideStatus.AVAILABLE).count();

        JPanel activityCard = new JPanel(new GridLayout(2, 3, 12, 12));
        activityCard.setBackground(AppTheme.BG_CARD);
        activityCard.setBorder(AppTheme.cardBorder());
        activityCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityCard.setMaximumSize(new Dimension(600, 160));

        activityCard.add(createStatLabel("Total Bookings", String.valueOf(bookings.size())));
        activityCard.add(createStatLabel("Confirmed", String.valueOf(confirmedBookings)));
        activityCard.add(createStatLabel("Cancelled", String.valueOf(cancelledBookings)));
        activityCard.add(createStatLabel("Published Rides", String.valueOf(rides.size())));
        activityCard.add(createStatLabel("Active Rides", String.valueOf(activeRides)));
        activityCard.add(createStatLabel("Vehicles", String.valueOf(vehicles.size())));

        content.add(activityCard);

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().setBackground(AppTheme.BG_PRIMARY);
        add(sp, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel label = AppTheme.bodyLabel(labelText);
        label.setForeground(AppTheme.TEXT_SECONDARY);
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private JPanel createStatLabel(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(AppTheme.PRIMARY);

        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(AppTheme.FONT_SMALL);
        lblLbl.setForeground(AppTheme.TEXT_SECONDARY);

        p.add(valLbl);
        p.add(lblLbl);
        return p;
    }

    private void saveProfile(User user) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String behaviour = behaviourField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setName(name);
        user.setPhoneNo(phone);
        user.setBehaviour(behaviour.isEmpty() ? null : behaviour);

        try {
            boolean success = ServiceFactory.getUserService().updateProfile(user);
            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully.",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
