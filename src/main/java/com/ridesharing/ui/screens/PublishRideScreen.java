package com.ridesharing.ui.screens;

import com.ridesharing.model.Ride;
import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Publish a Ride form with validation.
 */
public class PublishRideScreen extends JPanel {

    private JTextField sourceField, destField, dateField, timeField, costField, seatsField;
    private JComboBox<String> vehicleCombo;
    private JLabel errorLabel;
    private List<Vehicle> userVehicles;

    private Runnable onPublished;

    public PublishRideScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PRIMARY);
        buildUI();
    }

    public void setOnPublished(Runnable onPublished) { this.onPublished = onPublished; }

    public void refresh() {
        loadVehicles();
        clearForm();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_PRIMARY);
        content.setBorder(AppTheme.sectionPadding());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(AppTheme.cardBorder());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(600, 700));

        JLabel heading = AppTheme.subtitleLabel("Publish a Ride");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);

        JLabel desc = AppTheme.secondaryLabel("Share your ride with other travelers.");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(20));

        // Form grid
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 12);

        sourceField = AppTheme.styledTextField("e.g. Bangalore");
        destField = AppTheme.styledTextField("e.g. Mysore");
        dateField = AppTheme.styledTextField("yyyy-MM-dd");
        timeField = AppTheme.styledTextField("HH:mm (24h)");
        costField = AppTheme.styledTextField("e.g. 250");
        seatsField = AppTheme.styledTextField("e.g. 3");
        vehicleCombo = new JComboBox<>();
        vehicleCombo.setFont(AppTheme.FONT_BODY);
        vehicleCombo.setPreferredSize(new Dimension(200, AppTheme.INPUT_HEIGHT));

        JPanel vehicleRow = new JPanel(new BorderLayout(8, 0));
        vehicleRow.setOpaque(false);
        vehicleRow.add(vehicleCombo, BorderLayout.CENTER);

        JButton addVehicleBtn = AppTheme.secondaryButton("+ Add");
        addVehicleBtn.setPreferredSize(new Dimension(85, AppTheme.INPUT_HEIGHT));
        addVehicleBtn.setToolTipText("Add a new vehicle to your profile");
        addVehicleBtn.addActionListener(e -> quickAddVehicle());
        vehicleRow.add(addVehicleBtn, BorderLayout.EAST);

        int row = 0;
        addFormRow(form, gbc, row++, "Source Address *", sourceField);
        addFormRow(form, gbc, row++, "Destination *", destField);
        addFormRow(form, gbc, row++, "Departure Date *", dateField);
        addFormRow(form, gbc, row++, "Departure Time *", timeField);
        addFormRow(form, gbc, row++, "Vehicle *", vehicleRow);
        addFormRow(form, gbc, row++, "Available Seats *", seatsField);
        addFormRow(form, gbc, row++, "Cost per Seat (₹) *", costField);

        card.add(form);
        card.add(Box.createVerticalStrut(8));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(12));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton publishBtn = AppTheme.primaryButton("Publish Ride");
        publishBtn.addActionListener(e -> attemptPublish());
        btnPanel.add(publishBtn);

        JButton clearBtn = AppTheme.secondaryButton("Clear");
        clearBtn.addActionListener(e -> clearForm());
        btnPanel.add(clearBtn);

        card.add(btnPanel);

        content.add(card);

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().setBackground(AppTheme.BG_PRIMARY);
        add(sp, BorderLayout.CENTER);

        loadVehicles();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(AppTheme.bodyLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void loadVehicles() {
        User user = ServiceFactory.getAuthService().getCurrentUser();
        if (user == null) return;
        userVehicles = ServiceFactory.getVehicleService().getVehiclesByOwner(user.getUserId());
        vehicleCombo.removeAllItems();
        if (userVehicles.isEmpty()) {
            vehicleCombo.addItem("No vehicles — add one first");
        } else {
            for (Vehicle v : userVehicles) {
                vehicleCombo.addItem(v.getCarModel() + " (ID: " + v.getVehicleId() + ")");
            }
        }
    }

    private void quickAddVehicle() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 8));
        panel.add(AppTheme.bodyLabel("Car Model:"));
        JTextField modelField = AppTheme.styledTextField("e.g. Maruti Suzuki Swift");
        panel.add(modelField);

        int result = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                panel,
                "Add New Vehicle",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String model = modelField.getText().trim();
            if (model.isEmpty()) {
                showError("Car model is required.");
                return;
            }
            User user = ServiceFactory.getAuthService().getCurrentUser();
            if (user != null) {
                Vehicle v = new Vehicle(0, model, user.getUserId());
                ServiceFactory.getVehicleService().addVehicle(v);
                loadVehicles();
                if (vehicleCombo.getItemCount() > 0) {
                    vehicleCombo.setSelectedIndex(vehicleCombo.getItemCount() - 1);
                }
                errorLabel.setText(" ");
            }
        }
    }

    private void clearForm() {
        sourceField.setText("");
        destField.setText("");
        dateField.setText("");
        timeField.setText("");
        costField.setText("");
        seatsField.setText("");
        errorLabel.setText(" ");
        if (vehicleCombo.getItemCount() > 0) vehicleCombo.setSelectedIndex(0);
    }

    private void attemptPublish() {
        User user = ServiceFactory.getAuthService().getCurrentUser();
        if (user == null) return;

        String source = sourceField.getText().trim();
        String dest = destField.getText().trim();
        String dateStr = dateField.getText().trim();
        String timeStr = timeField.getText().trim();
        String costStr = costField.getText().trim();
        String seatsStr = seatsField.getText().trim();

        if (source.isEmpty()) { showError("Source address is required."); return; }
        if (dest.isEmpty()) { showError("Destination is required."); return; }
        if (dateStr.isEmpty()) { showError("Departure date is required."); return; }
        if (timeStr.isEmpty()) { showError("Departure time is required."); return; }
        if (seatsStr.isEmpty()) { showError("Available seats is required."); return; }
        if (costStr.isEmpty()) { showError("Cost per seat is required."); return; }

        // Parse date/time
        LocalDate date;
        try { date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
        catch (DateTimeParseException e) { showError("Invalid date format. Use yyyy-MM-dd."); return; }

        LocalTime time;
        try { time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")); }
        catch (DateTimeParseException e) { showError("Invalid time format. Use HH:mm (24h)."); return; }

        LocalDateTime departure = LocalDateTime.of(date, time);
        if (departure.isBefore(LocalDateTime.now())) {
            showError("Departure time cannot be in the past.");
            return;
        }

        int seats;
        try { seats = Integer.parseInt(seatsStr); } catch (NumberFormatException e) {
            showError("Seats must be a number."); return;
        }
        if (seats <= 0) { showError("Seats must be at least 1."); return; }

        BigDecimal cost;
        try { cost = new BigDecimal(costStr); } catch (NumberFormatException e) {
            showError("Cost must be a valid number."); return;
        }
        if (cost.compareTo(BigDecimal.ZERO) < 0) { showError("Cost cannot be negative."); return; }

        if (userVehicles == null || userVehicles.isEmpty()) {
            showError("You need to add a vehicle before publishing a ride.");
            return;
        }

        Vehicle selectedVehicle = userVehicles.get(vehicleCombo.getSelectedIndex());

        Ride ride = new Ride(user.getUserId(), selectedVehicle.getVehicleId(),
                seats, source, dest, departure, cost);

        try {
            Ride published = ServiceFactory.getRideService().publishRide(ride);

            JOptionPane.showMessageDialog(this,
                    "Ride published successfully!\n\n" +
                    "Ride ID: #" + published.getRideId() + "\n" +
                    "Route: " + source + " → " + dest + "\n" +
                    "Departure: " + departure.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                    "Ride Published",
                    JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            if (onPublished != null) onPublished.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Failed to publish ride: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
