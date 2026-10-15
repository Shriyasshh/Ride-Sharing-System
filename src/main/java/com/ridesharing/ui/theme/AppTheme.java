package com.ridesharing.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Centralized theme and design system for the Ride Sharing application.
 * All colors, fonts, spacing, and reusable styling are defined here.
 */
public final class AppTheme {

    // ── Primary Colors ──
    public static final Color PRIMARY = new Color(16, 85, 63);          // Deep emerald
    public static final Color PRIMARY_DARK = new Color(10, 60, 44);     // Darker emerald
    public static final Color PRIMARY_LIGHT = new Color(22, 115, 85);   // Lighter emerald
    public static final Color PRIMARY_HOVER = new Color(20, 100, 74);   // Hover state

    // ── Accent Colors ──
    public static final Color ACCENT = new Color(52, 199, 136);         // Mint green
    public static final Color ACCENT_LIGHT = new Color(209, 250, 229);  // Light mint

    // ── Background Colors ──
    public static final Color BG_PRIMARY = new Color(245, 245, 240);    // Warm off-white
    public static final Color BG_SECONDARY = new Color(250, 250, 247);  // Slightly lighter
    public static final Color BG_CARD = Color.WHITE;                     // Card surfaces
    public static final Color BG_SIDEBAR = PRIMARY;                      // Sidebar background
    public static final Color BG_HEADER = Color.WHITE;                   // Header background

    // ── Text Colors ──
    public static final Color TEXT_PRIMARY = new Color(30, 30, 30);      // Main text
    public static final Color TEXT_SECONDARY = new Color(110, 110, 110); // Secondary text
    public static final Color TEXT_MUTED = new Color(160, 160, 160);     // Muted/placeholder
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;             // Text on primary bg
    public static final Color TEXT_SIDEBAR = new Color(200, 230, 215);   // Sidebar text
    public static final Color TEXT_SIDEBAR_ACTIVE = Color.WHITE;         // Active sidebar text

    // ── Status Colors ──
    public static final Color STATUS_AVAILABLE = new Color(34, 139, 34);   // Green
    public static final Color STATUS_AVAILABLE_BG = new Color(220, 252, 231);
    public static final Color STATUS_CONFIRMED = new Color(30, 120, 180);  // Blue
    public static final Color STATUS_CONFIRMED_BG = new Color(219, 234, 254);
    public static final Color STATUS_CANCELLED = new Color(200, 50, 50);   // Red
    public static final Color STATUS_CANCELLED_BG = new Color(254, 226, 226);
    public static final Color STATUS_IN_PROGRESS = new Color(180, 130, 20);// Amber
    public static final Color STATUS_IN_PROGRESS_BG = new Color(254, 243, 199);

    // ── Border Colors ──
    public static final Color BORDER_LIGHT = new Color(228, 228, 228);
    public static final Color BORDER_FOCUS = PRIMARY_LIGHT;

    // ── Danger / Warning ──
    public static final Color DANGER = new Color(200, 50, 50);
    public static final Color DANGER_HOVER = new Color(180, 40, 40);
    public static final Color WARNING = new Color(230, 160, 30);

    // ── Typography ──
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_CAPTION = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SIDEBAR_ACTIVE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Spacing ──
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 12;
    public static final int SPACING_LG = 16;
    public static final int SPACING_XL = 24;
    public static final int SPACING_XXL = 32;

    // ── Dimensions ──
    public static final int SIDEBAR_WIDTH = 240;
    public static final int HEADER_HEIGHT = 60;
    public static final int CARD_RADIUS = 12;
    public static final int BUTTON_HEIGHT = 38;
    public static final int INPUT_HEIGHT = 38;
    public static final int TABLE_ROW_HEIGHT = 42;

    private AppTheme() {}

    /**
     * Installs FlatLaf and configures global UI defaults.
     */
    public static void install() {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("TabbedPane.selectedBackground", BG_CARD);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
    }

    // ── Border Factories ──

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        );
    }

    public static Border cardBorderSlim() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD)
        );
    }

    public static Border inputBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        );
    }

    public static Border sectionPadding() {
        return new EmptyBorder(SPACING_XL, SPACING_XL, SPACING_XL, SPACING_XL);
    }

    public static Border contentPadding() {
        return new EmptyBorder(SPACING_LG, SPACING_XL, SPACING_LG, SPACING_XL);
    }

    // ── Button Factories ──

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(TEXT_ON_PRIMARY);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 32, BUTTON_HEIGHT));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(PRIMARY);
        btn.setBackground(BG_CARD);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY, 1, true),
                new EmptyBorder(SPACING_SM, SPACING_LG, SPACING_SM, SPACING_LG)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 32, BUTTON_HEIGHT));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT_LIGHT);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(BG_CARD);
            }
        });
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(TEXT_ON_PRIMARY);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 32, BUTTON_HEIGHT));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(DANGER_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(DANGER);
            }
        });
        return btn;
    }

    public static JButton textButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(PRIMARY);
        btn.setBackground(null);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Input Factories ──

    public static JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        field.setBorder(inputBorder());
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    public static JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_BODY);
        field.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        field.setBorder(inputBorder());
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    public static JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        return combo;
    }

    // ── Label Factories ──

    public static JLabel titleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel headingLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADING);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel bodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel secondaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    public static JLabel captionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_CAPTION);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    // ── Card Panel ──

    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(cardBorder());
        return panel;
    }

    // ── Table Styling ──

    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_LIGHT);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setForeground(TEXT_SECONDARY);
        header.setBackground(BG_SECONDARY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(new EmptyBorder(0, SPACING_MD, 0, SPACING_MD));
    }

    // ── Status Badge ──

    public static JLabel statusBadge(String status) {
        JLabel badge = new JLabel(status) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_SMALL_BOLD);
        badge.setOpaque(false);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));

        switch (status.toUpperCase()) {
            case "AVAILABLE":
                badge.setForeground(STATUS_AVAILABLE);
                badge.setBackground(STATUS_AVAILABLE_BG);
                break;
            case "CONFIRMED":
                badge.setForeground(STATUS_CONFIRMED);
                badge.setBackground(STATUS_CONFIRMED_BG);
                break;
            case "CANCELLED":
                badge.setForeground(STATUS_CANCELLED);
                badge.setBackground(STATUS_CANCELLED_BG);
                break;
            case "IN_PROGRESS":
                badge.setForeground(STATUS_IN_PROGRESS);
                badge.setBackground(STATUS_IN_PROGRESS_BG);
                badge.setText("IN PROGRESS");
                break;
            default:
                badge.setForeground(TEXT_SECONDARY);
                badge.setBackground(BG_SECONDARY);
                break;
        }
        return badge;
    }

    /**
     * Table cell renderer that displays status values as pill badges.
     */
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));

        public StatusBadgeRenderer() {
            panel.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            panel.removeAll();
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            if (value != null && !value.toString().trim().isEmpty() && !value.toString().equals("—")) {
                JLabel badge = statusBadge(value.toString());
                panel.add(badge);
            } else {
                JLabel emptyLbl = new JLabel("—");
                emptyLbl.setFont(FONT_SMALL);
                emptyLbl.setForeground(TEXT_MUTED);
                panel.add(emptyLbl);
            }
            return panel;
        }
    }

    // ── Separator ──

    public static JSeparator horizontalSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BORDER_LIGHT);
        return sep;
    }
}
