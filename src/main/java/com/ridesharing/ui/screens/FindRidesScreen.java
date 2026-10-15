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
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

/**
 * Find Rides screen with search form, results table, and sorting/filtering.
 */
public class FindRidesScreen extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JTextField sourceField, destField, dateField, seatsField;
    private DefaultTableModel tableModel;
    private JTable resultsTable;
    private JLabel resultCountLabel;
    private List<Ride> currentResults;

    public FindRidesScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PRIMARY);
        buildUI();
    }

    public void refresh() {
        sourceField.setText("");
        destField.setText("");
        dateField.setText("");
        seatsField.setText("1");
        doSearch();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BG_PRIMARY);
        content.setBorder(AppTheme.sectionPadding());

        // ── Search Panel ──
        JPanel searchCard = new JPanel();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        searchCard.setBackground(AppTheme.BG_CARD);
        searchCard.setBorder(AppTheme.cardBorder());

        JLabel heading = AppTheme.headingLabel("Search for Rides");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchCard.add(heading);
        searchCard.add(Box.createVerticalStrut(16));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        sourceField = AppTheme.styledTextField("From (e.g. Bangalore)");
        destField = AppTheme.styledTextField("To (e.g. Mysore)");
        dateField = AppTheme.styledTextField("Date (yyyy-mm-dd, optional)");
        seatsField = AppTheme.styledTextField("Min seats");
        seatsField.setText("1");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        fieldsPanel.add(AppTheme.bodyLabel("From:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        fieldsPanel.add(sourceField, gbc);

        // Swap button
        JButton swapBtn = AppTheme.textButton("\u21C4");
        swapBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        swapBtn.setToolTipText("Swap source and destination");
        swapBtn.addActionListener(e -> {
            String tmp = sourceField.getText();
            sourceField.setText(destField.getText());
            destField.setText(tmp);
        });
        gbc.gridx = 2; gbc.weightx = 0.05;
        fieldsPanel.add(swapBtn, gbc);

        gbc.gridx = 3; gbc.weightx = 0.1;
        fieldsPanel.add(AppTheme.bodyLabel("To:"), gbc);
        gbc.gridx = 4; gbc.weightx = 0.35;
        fieldsPanel.add(destField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(AppTheme.bodyLabel("Date:"), gbc);
        gbc.gridx = 1;
        fieldsPanel.add(dateField, gbc);

        gbc.gridx = 3;
        fieldsPanel.add(AppTheme.bodyLabel("Seats:"), gbc);
        gbc.gridx = 4;
        fieldsPanel.add(seatsField, gbc);

        searchCard.add(fieldsPanel);
        searchCard.add(Box.createVerticalStrut(12));

        JPanel searchBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBtnPanel.setOpaque(false);
        searchBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton searchBtn = AppTheme.primaryButton("\u2315  Search Rides");
        searchBtn.addActionListener(e -> doSearch());
        searchBtnPanel.add(searchBtn);

        JButton clearBtn = AppTheme.secondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            sourceField.setText("");
            destField.setText("");
            dateField.setText("");
            seatsField.setText("1");
            doSearch();
        });
        searchBtnPanel.add(clearBtn);

        resultCountLabel = AppTheme.secondaryLabel("");
        searchBtnPanel.add(Box.createHorizontalStrut(16));
        searchBtnPanel.add(resultCountLabel);

        searchCard.add(searchBtnPanel);
        content.add(searchCard, BorderLayout.NORTH);

        // ── Results Table ──
        String[] cols = {"Ride ID", "Route", "Departure", "Driver", "Vehicle",
                "₹/Seat", "Remaining", "Status", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 8; }
        };

        resultsTable = new JTable(tableModel);
        AppTheme.styleTable(resultsTable);

        // Row sorter for sorting by columns
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);

        // Custom comparator for price column
        sorter.setComparator(5, Comparator.comparingDouble(o -> {
            try { return Double.parseDouble(o.toString().replace(",", "")); } catch (Exception e) { return 0.0; }
        }));

        // Status column pill badge renderer
        resultsTable.getColumnModel().getColumn(7).setCellRenderer(
                new AppTheme.StatusBadgeRenderer());

        // Action button column
        resultsTable.getColumnModel().getColumn(8).setCellRenderer(
                new ButtonRenderer("View Details"));
        resultsTable.getColumnModel().getColumn(8).setCellEditor(
                new ButtonEditor("View Details", () -> {
                    int viewRow = resultsTable.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = resultsTable.convertRowIndexToModel(viewRow);
                        if (currentResults != null && modelRow < currentResults.size()) {
                            showRideDetails(currentResults.get(modelRow));
                        }
                    }
                }));

        // Center aligned cell renderer for numeric/code columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Double-click row listener to view details directly
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = resultsTable.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = resultsTable.convertRowIndexToModel(viewRow);
                        if (currentResults != null && modelRow < currentResults.size()) {
                            showRideDetails(currentResults.get(modelRow));
                        }
                    }
                }
            }
        });

        // Set column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        resultsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        resultsTable.getColumnModel().getColumn(8).setPreferredWidth(100);

        JScrollPane sp = new JScrollPane(resultsTable);
        sp.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT)
        ));
        sp.getViewport().setBackground(AppTheme.BG_CARD);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(AppTheme.BG_CARD);
        tablePanel.setBorder(AppTheme.cardBorder());
        JLabel tableHeading = AppTheme.headingLabel("Search Results");
        tablePanel.add(tableHeading, BorderLayout.NORTH);
        tablePanel.add(Box.createVerticalStrut(8));
        tablePanel.add(sp, BorderLayout.CENTER);

        content.add(tablePanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(AppTheme.BG_PRIMARY);
        add(scrollPane, BorderLayout.CENTER);

        // Initial load
        doSearch();
    }

    private void doSearch() {
        String source = sourceField.getText().trim();
        String dest = destField.getText().trim();
        String dateStr = dateField.getText().trim();
        String seatsStr = seatsField.getText().trim();

        LocalDate date = null;
        if (!dateStr.isEmpty()) {
            try { date = LocalDate.parse(dateStr, DATE_FMT); }
            catch (DateTimeParseException e) { /* ignore invalid date */ }
        }

        int minSeats = 1;
        try { minSeats = Integer.parseInt(seatsStr); } catch (NumberFormatException ignored) {}

        currentResults = ServiceFactory.getRideService().searchRides(source, dest, date, minSeats);
        tableModel.setRowCount(0);

        for (Ride r : currentResults) {
            User driver = ServiceFactory.getUserService().findById(r.getDriverId());
            Vehicle vehicle = ServiceFactory.getVehicleService().findById(r.getVehicleId());
            int remaining = ServiceFactory.getRideService().getRemainingSeats(r.getRideId());

            tableModel.addRow(new Object[]{
                    "DEMO-R" + r.getRideId(),
                    r.getSourceAddress() + " → " + r.getDestinationAddress(),
                    r.getDepartureTime().format(DT_FMT),
                    driver != null ? driver.getName() : "Unknown",
                    vehicle != null ? vehicle.getCarModel() : "Unknown",
                    r.getCostPerSeat().toPlainString(),
                    remaining + " / " + r.getNoOfSeats(),
                    r.getRideStatus().name(),
                    "View Details"
            });
        }

        resultCountLabel.setText(currentResults.size() + " ride(s) found");
    }

    private void showRideDetails(Ride ride) {
        Window window = SwingUtilities.getWindowAncestor(this);
        RideDetailsDialog dialog = new RideDetailsDialog(
                window instanceof JFrame ? (JFrame) window : null, ride);
        dialog.setVisible(true);
        doSearch(); // refresh after potential booking
    }

    // ── Button Renderer & Editor for Action Column ──

    public static class ButtonRenderer extends DefaultTableCellRenderer {
        private final JButton button;
        private final JLabel dashLabel;

        public ButtonRenderer(String text) {
            button = AppTheme.textButton(text);
            button.setForeground(AppTheme.PRIMARY);
            dashLabel = new JLabel("—", SwingConstants.CENTER);
            dashLabel.setFont(AppTheme.FONT_BODY);
            dashLabel.setForeground(AppTheme.TEXT_MUTED);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String valStr = value != null ? value.toString() : "";
            if (valStr.isEmpty() || "—".equals(valStr) || "-".equals(valStr)) {
                return dashLabel;
            }
            button.setText(valStr);
            return button;
        }
    }

    public static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final JLabel dashLabel;
        private String currentVal = "";

        public ButtonEditor(String text, Runnable onClick) {
            super(new JCheckBox());
            button = AppTheme.textButton(text);
            button.addActionListener(e -> {
                fireEditingStopped();
                if (onClick != null && !"—".equals(currentVal) && !currentVal.isEmpty()) {
                    onClick.run();
                }
            });
            dashLabel = new JLabel("—", SwingConstants.CENTER);
            dashLabel.setFont(AppTheme.FONT_BODY);
            dashLabel.setForeground(AppTheme.TEXT_MUTED);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentVal = value != null ? value.toString() : "";
            if (currentVal.isEmpty() || "—".equals(currentVal) || "-".equals(currentVal)) {
                return dashLabel;
            }
            button.setText(currentVal);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return currentVal;
        }
    }
}
