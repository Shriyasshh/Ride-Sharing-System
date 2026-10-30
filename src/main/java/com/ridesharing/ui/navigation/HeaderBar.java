package com.ridesharing.ui.navigation;

import com.ridesharing.model.User;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Top header bar with page title, notification placeholder, and user profile.
 */
public class HeaderBar extends JPanel {

    private final JLabel pageTitleLabel;
    private final JLabel userNameLabel;
    private final JLabel userEmailLabel;

    public interface HeaderListener {
        void onNavigate(String screenName);
        void onLogout();
    }

    private HeaderListener headerListener;

    public void setHeaderListener(HeaderListener listener) {
        this.headerListener = listener;
    }

    public HeaderBar() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_HEADER);
        setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_LIGHT),
                new EmptyBorder(0, AppTheme.SPACING_XL, 0, AppTheme.SPACING_XL)
        ));

        // ── Left: Page Title ──
        pageTitleLabel = new JLabel("Dashboard");
        pageTitleLabel.setFont(AppTheme.FONT_SUBTITLE);
        pageTitleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        // ── Right: Notifications + User Profile ──
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        // Notification bell (placeholder)
        JLabel bellIcon = new JLabel("\uD83D\uDD14");
        bellIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        bellIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellIcon.setToolTipText("Notifications");
        bellIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(HeaderBar.this,
                        "You have no new notifications.",
                        "Notifications", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // User info
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setOpaque(false);
        userPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        userNameLabel = new JLabel("User");
        userNameLabel.setFont(AppTheme.FONT_BODY_BOLD);
        userNameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        userNameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        userEmailLabel = new JLabel("user@example.com");
        userEmailLabel.setFont(AppTheme.FONT_CAPTION);
        userEmailLabel.setForeground(AppTheme.TEXT_SECONDARY);
        userEmailLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        userPanel.add(userNameLabel);
        userPanel.add(userEmailLabel);

        // Avatar circle
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String initial = userNameLabel.getText().isEmpty() ? "U" :
                        userNameLabel.getText().substring(0, 1).toUpperCase();
                int x = (36 - fm.stringWidth(initial)) / 2;
                int y = (36 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }
        };
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Click trigger for user profile menu
        java.awt.event.MouseAdapter menuTrigger = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showUserMenu(e.getComponent(), 0, e.getComponent().getHeight());
            }
        };
        userPanel.addMouseListener(menuTrigger);
        avatar.addMouseListener(menuTrigger);

        rightPanel.add(bellIcon);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(userPanel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(avatar);

        add(pageTitleLabel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        refreshUser();
    }

    private void showUserMenu(Component invoker, int x, int y) {
        User user = ServiceFactory.getAuthService().getCurrentUser();
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT));

        JMenuItem userInfoItem = new JMenuItem(user != null ? "Signed in as " + user.getName() : "Signed in");
        userInfoItem.setEnabled(false);
        userInfoItem.setFont(AppTheme.FONT_SMALL_BOLD);
        menu.add(userInfoItem);

        menu.addSeparator();

        JMenuItem profileItem = new JMenuItem("My Profile");
        profileItem.setFont(AppTheme.FONT_BODY);
        profileItem.addActionListener(e -> {
            if (headerListener != null) headerListener.onNavigate("profile");
        });
        menu.add(profileItem);

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.setFont(AppTheme.FONT_BODY);
        settingsItem.addActionListener(e -> {
            if (headerListener != null) headerListener.onNavigate("settings");
        });
        menu.add(settingsItem);

        menu.addSeparator();

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(AppTheme.FONT_BODY);
        logoutItem.setForeground(AppTheme.DANGER);
        logoutItem.addActionListener(e -> {
            if (headerListener != null) headerListener.onLogout();
        });
        menu.add(logoutItem);

        menu.show(invoker, x, y);
    }

    public void setPageTitle(String title) {
        pageTitleLabel.setText(title);
    }

    public void refreshUser() {
        User user = ServiceFactory.getAuthService().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getName());
            userEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "");
        }
        repaint();
    }
}
