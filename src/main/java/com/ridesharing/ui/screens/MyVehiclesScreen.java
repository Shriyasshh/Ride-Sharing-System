package com.ridesharing.ui.screens;

import com.ridesharing.model.User;
import com.ridesharing.model.Vehicle;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * My Vehicles screen with vehicle list and add vehicle dialog.
 */
public class MyVehiclesScreen extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel emptyLabel;

    public MyVehiclesScreen() {
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

        // ── Header with Add button ──
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel heading = AppTheme.subtitleLabel("My Vehicles");
        headerPanel.add(heading, BorderLayout.WEST);

        JButton addBtn = AppTheme.primaryButton("\u2795  Add Vehicle");
        addBtn.addActionListener(e -> showAddVehicleDialog());
        headerPanel.add(addBtn, BorderLayout.EAST);

        content.add(headerPanel, BorderLayout.NORTH);

        // ── Table ──
        String[] cols = {"Vehicle ID", "Car Model", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 2; }
        };

        table = new JTable(tableModel);
        AppTheme.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        table.getColumnModel().getColumn(2).setCellRenderer(
                new FindRidesScreen.ButtonRenderer("Delete"));
        table.getColumnModel().getColumn(2).setCellEditor(
                new FindRidesScreen.ButtonEditor("Delete", this::deleteSelectedVehicle));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT));
        sp.getViewport().setBackground(AppTheme.BG_CARD);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.BG_CARD);
        tableCard.setBorder(AppTheme.cardBorder());

        emptyLabel = AppTheme.secondaryLabel("No vehicles yet. Add your first vehicle to start publishing rides!");
        emptyLabel.setBorder(new EmptyBorder(40, 0, 40, 0));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setVisible(false);

        tableCard.add(sp, BorderLayout.CENTER);
        tableCard.add(emptyLabel, BorderLayout.SOUTH);

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

        List<Vehicle> vehicles = ServiceFactory.getVehicleService()
                .getVehiclesByOwner(user.getUserId());
        tableModel.setRowCount(0);

        for (Vehicle v : vehicles) {
            tableModel.addRow(new Object[]{
                    v.getVehicleId(),
                    v.getCarModel(),
                    "Delete"
            });
        }

        emptyLabel.setVisible(vehicles.isEmpty());
        table.setVisible(!vehicles.isEmpty());
    }

    private void showAddVehicleDialog() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 8));
        JLabel label = AppTheme.bodyLabel("Car Model:");
        JTextField modelField = AppTheme.styledTextField("e.g. Maruti Suzuki Swift");
        panel.add(label);
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
                JOptionPane.showMessageDialog(this, "Car model is required.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = ServiceFactory.getAuthService().getCurrentUser();
            Vehicle vehicle = new Vehicle(0, model, user.getUserId());
            Vehicle added = ServiceFactory.getVehicleService().addVehicle(vehicle);

            JOptionPane.showMessageDialog(this,
                    "Vehicle added successfully!\n\nVehicle ID: " + added.getVehicleId() +
                    "\nModel: " + added.getCarModel(),
                    "Vehicle Added",
                    JOptionPane.INFORMATION_MESSAGE);

            loadData();
        }
    }

    private void deleteSelectedVehicle() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);

        int vehicleId = (int) tableModel.getValueAt(modelRow, 0);
        String model = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete vehicle \"" + model + "\" (ID: " + vehicleId + ")?\n\n" +
                "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ServiceFactory.getVehicleService().deleteVehicle(vehicleId);
            JOptionPane.showMessageDialog(this, "Vehicle deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }
}
