package com.ridesharing.ui.screens;

import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Welcome screen with branding, introduction, and auth actions.
 */
public class WelcomeScreen extends JPanel {

    private Runnable onLogin;
    private Runnable onRegister;

    public WelcomeScreen() {
        setLayout(new GridBagLayout());
        setBackground(AppTheme.BG_PRIMARY);
        buildUI();
    }

    public void setOnLogin(Runnable onLogin) { this.onLogin = onLogin; }
    public void setOnRegister(Runnable onRegister) { this.onRegister = onRegister; }

    private void buildUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(48, 56, 48, 56)
        ));
        card.setMaximumSize(new Dimension(480, 600));

        // Logo icon
        JLabel logo = new JLabel("\u26C8", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 56));
        logo.setForeground(AppTheme.PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // App name
        JLabel appName = new JLabel("RideShare", SwingConstants.CENTER);
        appName.setFont(new Font("Segoe UI", Font.BOLD, 32));
        appName.setForeground(AppTheme.PRIMARY);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline
        JLabel tagline = new JLabel("Share rides, share joy", SwingConstants.CENTER);
        tagline.setFont(AppTheme.FONT_BODY);
        tagline.setForeground(AppTheme.TEXT_SECONDARY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description
        JLabel desc = new JLabel("<html><div style='text-align:center;width:300px;'>"
                + "Connect with fellow travelers, share your journey, reduce costs, "
                + "and help the environment. Publish rides or book seats — it's that simple."
                + "</div></html>", SwingConstants.CENTER);
        desc.setFont(AppTheme.FONT_BODY);
        desc.setForeground(AppTheme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Separator
        JSeparator sep = AppTheme.horizontalSeparator();
        sep.setMaximumSize(new Dimension(300, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button
        JButton loginBtn = AppTheme.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(300, AppTheme.BUTTON_HEIGHT));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> { if (onLogin != null) onLogin.run(); });

        // Register button
        JButton registerBtn = AppTheme.secondaryButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(300, AppTheme.BUTTON_HEIGHT));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> { if (onRegister != null) onRegister.run(); });

        card.add(logo);
        card.add(Box.createVerticalStrut(12));
        card.add(appName);
        card.add(Box.createVerticalStrut(6));
        card.add(tagline);
        card.add(Box.createVerticalStrut(20));
        card.add(desc);
        card.add(Box.createVerticalStrut(28));
        card.add(sep);
        card.add(Box.createVerticalStrut(28));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(registerBtn);

        add(card);
    }
}
