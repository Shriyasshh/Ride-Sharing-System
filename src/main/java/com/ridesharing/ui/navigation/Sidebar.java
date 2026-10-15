package com.ridesharing.ui.navigation;

import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Left sidebar navigation panel with emerald-themed design.
 */
public class Sidebar extends JPanel {

    private final List<NavItem> navItems = new ArrayList<>();
    private NavItem activeItem;
    private Runnable onLogout;
    private NavigationListener navigationListener;

    public interface NavigationListener {
        void onNavigate(String screenName);
    }

    public Sidebar() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_SIDEBAR);
        setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setBorder(null);

        buildUI();
    }

    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    private void buildUI() {
        // ── Brand Header ──
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(AppTheme.PRIMARY_DARK);
        brandPanel.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel logo = new JLabel("\u26C8");  // Unicode icon
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        logo.setForeground(AppTheme.ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appName = new JLabel("RideShare");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Share rides, share joy");
        tagline.setFont(AppTheme.FONT_CAPTION);
        tagline.setForeground(new Color(150, 200, 180));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(8));
        brandPanel.add(appName);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(tagline);

        // ── Navigation Items ──
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(AppTheme.BG_SIDEBAR);
        navPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        addNavItem(navPanel, "\u25A3", "Dashboard", "dashboard");
        addNavItem(navPanel, "\u2315", "Find Rides", "find_rides");
        addNavItem(navPanel, "\u2795", "Publish a Ride", "publish_ride");
        addNavItem(navPanel, "\u2630", "My Published Rides", "my_rides");
        addNavItem(navPanel, "\u2611", "My Bookings", "my_bookings");
        addNavItem(navPanel, "\u26DF", "My Vehicles", "my_vehicles");

        navPanel.add(Box.createVerticalStrut(16));
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(30, 75, 55));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        navPanel.add(sep);
        navPanel.add(Box.createVerticalStrut(16));

        addNavItem(navPanel, "\u2699", "Profile", "profile");
        addNavItem(navPanel, "\u2692", "Settings", "settings");

        JScrollPane scrollPane = new JScrollPane(navPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(AppTheme.BG_SIDEBAR);

        // ── Logout Panel ──
        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setBackground(AppTheme.PRIMARY_DARK);
        logoutPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JButton logoutBtn = new JButton("\u23FB  Logout");
        logoutBtn.setFont(AppTheme.FONT_SIDEBAR);
        logoutBtn.setForeground(new Color(255, 130, 130));
        logoutBtn.setBackground(null);
        logoutBtn.setOpaque(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        logoutPanel.add(logoutBtn, BorderLayout.CENTER);

        add(brandPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(logoutPanel, BorderLayout.SOUTH);

        // Set first item active
        if (!navItems.isEmpty()) {
            setActive(navItems.get(0));
        }
    }

    private void addNavItem(JPanel parent, String icon, String label, String screenName) {
        NavItem item = new NavItem(icon, label, screenName);
        navItems.add(item);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        parent.add(item);
    }

    public void setActive(NavItem item) {
        if (activeItem != null) activeItem.setActive(false);
        activeItem = item;
        activeItem.setActive(true);
    }

    public void navigateTo(String screenName) {
        for (NavItem item : navItems) {
            if (item.screenName.equals(screenName)) {
                setActive(item);
                break;
            }
        }
    }

    // ── Inner NavItem Class ──

    class NavItem extends JPanel {
        final String screenName;
        private final JLabel iconLabel;
        private final JLabel textLabel;
        private boolean active = false;

        NavItem(String icon, String text, String screenName) {
            this.screenName = screenName;
            setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
            setBackground(AppTheme.BG_SIDEBAR);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 12, 0, 12));

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
            iconLabel.setForeground(AppTheme.TEXT_SIDEBAR);

            textLabel = new JLabel(text);
            textLabel.setFont(AppTheme.FONT_SIDEBAR);
            textLabel.setForeground(AppTheme.TEXT_SIDEBAR);

            add(iconLabel);
            add(textLabel);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Sidebar.this.setActive(NavItem.this);
                    if (navigationListener != null) navigationListener.onNavigate(screenName);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) setBackground(AppTheme.PRIMARY_LIGHT);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) setBackground(AppTheme.BG_SIDEBAR);
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            if (active) {
                setBackground(AppTheme.PRIMARY_LIGHT);
                textLabel.setFont(AppTheme.FONT_SIDEBAR_ACTIVE);
                textLabel.setForeground(AppTheme.TEXT_SIDEBAR_ACTIVE);
                iconLabel.setForeground(AppTheme.ACCENT);
            } else {
                setBackground(AppTheme.BG_SIDEBAR);
                textLabel.setFont(AppTheme.FONT_SIDEBAR);
                textLabel.setForeground(AppTheme.TEXT_SIDEBAR);
                iconLabel.setForeground(AppTheme.TEXT_SIDEBAR);
            }
        }
    }
}
