package com.ridesharing.ui.screens;

import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Settings screen with notification preferences (mock), help/about, and logout.
 */
public class SettingsScreen extends JPanel {

    private Runnable onLogout;

    public SettingsScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PRIMARY);
        buildUI();
    }

    public void setOnLogout(Runnable onLogout) { this.onLogout = onLogout; }

    public void refresh() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_PRIMARY);
        content.setBorder(AppTheme.sectionPadding());

        JLabel heading = AppTheme.subtitleLabel("Settings");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(24));

        // ── Notification Preferences (Mock) ──
        JPanel notifCard = new JPanel();
        notifCard.setLayout(new BoxLayout(notifCard, BoxLayout.Y_AXIS));
        notifCard.setBackground(AppTheme.BG_CARD);
        notifCard.setBorder(AppTheme.cardBorder());
        notifCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        notifCard.setMaximumSize(new Dimension(600, 250));

        JLabel notifHeading = AppTheme.headingLabel("Notification Preferences");
        notifHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        notifCard.add(notifHeading);
        notifCard.add(Box.createVerticalStrut(4));

        JLabel notifNote = AppTheme.captionLabel("(These are simulated preferences)");
        notifNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        notifCard.add(notifNote);
        notifCard.add(Box.createVerticalStrut(12));

        JCheckBox emailNotif = new JCheckBox("Email notifications for new bookings");
        emailNotif.setFont(AppTheme.FONT_BODY);
        emailNotif.setSelected(true);
        emailNotif.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox rideUpdates = new JCheckBox("Ride status updates");
        rideUpdates.setFont(AppTheme.FONT_BODY);
        rideUpdates.setSelected(true);
        rideUpdates.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox promoNotif = new JCheckBox("Promotional offers");
        promoNotif.setFont(AppTheme.FONT_BODY);
        promoNotif.setAlignmentX(Component.LEFT_ALIGNMENT);

        notifCard.add(emailNotif);
        notifCard.add(Box.createVerticalStrut(6));
        notifCard.add(rideUpdates);
        notifCard.add(Box.createVerticalStrut(6));
        notifCard.add(promoNotif);

        content.add(notifCard);
        content.add(Box.createVerticalStrut(20));

        // ── About ──
        JPanel aboutCard = new JPanel();
        aboutCard.setLayout(new BoxLayout(aboutCard, BoxLayout.Y_AXIS));
        aboutCard.setBackground(AppTheme.BG_CARD);
        aboutCard.setBorder(AppTheme.cardBorder());
        aboutCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        aboutCard.setMaximumSize(new Dimension(600, 200));

        JLabel aboutHeading = AppTheme.headingLabel("About RideShare");
        aboutHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        aboutCard.add(aboutHeading);
        aboutCard.add(Box.createVerticalStrut(8));

        JLabel version = AppTheme.bodyLabel("Version: 1.0-SNAPSHOT");
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        aboutCard.add(version);
        aboutCard.add(Box.createVerticalStrut(4));

        JLabel techStack = AppTheme.secondaryLabel("Built with Java 26 • Swing • FlatLaf • PostgreSQL • JDBC • Maven");
        techStack.setAlignmentX(Component.LEFT_ALIGNMENT);
        aboutCard.add(techStack);
        aboutCard.add(Box.createVerticalStrut(4));

        JLabel description = AppTheme.secondaryLabel("A ride-sharing platform connecting drivers and passengers.");
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        aboutCard.add(description);
        aboutCard.add(Box.createVerticalStrut(12));

        boolean realDb = com.ridesharing.service.ServiceFactory.isUseRealServices();
        JLabel statusNote = new JLabel(realDb
                ? "<html><span style='color:#10b981;'>&#10004;</span> Connected to PostgreSQL Database (Port 5433). Real services active.</html>"
                : "<html><i>This application is running in <b>mock mode</b>. Data is stored in memory.</i></html>");
        statusNote.setFont(AppTheme.FONT_CAPTION);
        statusNote.setForeground(realDb ? AppTheme.STATUS_CONFIRMED : AppTheme.STATUS_IN_PROGRESS);
        statusNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        aboutCard.add(statusNote);

        content.add(aboutCard);
        content.add(Box.createVerticalStrut(20));

        // ── Logout ──
        JPanel logoutCard = new JPanel();
        logoutCard.setLayout(new BoxLayout(logoutCard, BoxLayout.Y_AXIS));
        logoutCard.setBackground(AppTheme.BG_CARD);
        logoutCard.setBorder(AppTheme.cardBorder());
        logoutCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutCard.setMaximumSize(new Dimension(600, 120));

        JLabel logoutHeading = AppTheme.headingLabel("Account");
        logoutHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutCard.add(logoutHeading);
        logoutCard.add(Box.createVerticalStrut(12));

        JButton logoutBtn = AppTheme.dangerButton("Logout");
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION && onLogout != null) {
                onLogout.run();
            }
        });
        logoutCard.add(logoutBtn);

        content.add(logoutCard);

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().setBackground(AppTheme.BG_PRIMARY);
        add(sp, BorderLayout.CENTER);
    }
}
