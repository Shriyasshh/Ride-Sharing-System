package com.ridesharing.ui.screens;

import com.ridesharing.model.User;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.service.interfaces.AuthService;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login screen with email/phone and password fields.
 */
public class LoginScreen extends JPanel {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JButton loginBtn;

    private Runnable onSuccess;
    private Runnable onBack;

    public LoginScreen() {
        setLayout(new GridBagLayout());
        setBackground(AppTheme.BG_PRIMARY);
        buildUI();
    }

    public void setOnSuccess(Runnable onSuccess) { this.onSuccess = onSuccess; }
    public void setOnBack(Runnable onBack) { this.onBack = onBack; }

    private void buildUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(40, 48, 40, 48)
        ));

        // Back link
        JButton backBtn = AppTheme.textButton("\u2190 Back");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        // Title
        JLabel title = new JLabel("Welcome back");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(AppTheme.FONT_BODY);
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email field
        JLabel emailLabel = AppTheme.bodyLabel("Email or Phone");
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField = AppTheme.styledTextField("Enter email or phone number");
        emailField.setMaximumSize(new Dimension(360, AppTheme.INPUT_HEIGHT));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field
        JLabel passLabel = AppTheme.bodyLabel("Password");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel passPanel = new JPanel(new BorderLayout(4, 0));
        passPanel.setMaximumSize(new Dimension(360, AppTheme.INPUT_HEIGHT));
        passPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passPanel.setOpaque(false);

        passwordField = AppTheme.styledPasswordField("Enter password");
        JButton toggleBtn = new JButton("\u25C9");
        toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggleBtn.setPreferredSize(new Dimension(40, AppTheme.INPUT_HEIGHT));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setToolTipText("Toggle password visibility");
        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('\u2022');
                toggleBtn.setText("\u25C9");
            } else {
                passwordField.setEchoChar((char) 0);
                toggleBtn.setText("\u25CE");
            }
        });

        passPanel.add(passwordField, BorderLayout.CENTER);
        passPanel.add(toggleBtn, BorderLayout.EAST);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        loginBtn = AppTheme.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(360, AppTheme.BUTTON_HEIGHT));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());

        // Enter key trigger
        passwordField.addActionListener(e -> attemptLogin());
        emailField.addActionListener(e -> passwordField.requestFocus());

        card.add(backBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(loginBtn);

        add(card);
    }

    private void attemptLogin() {
        String emailOrPhone = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (emailOrPhone.isEmpty()) {
            showError("Please enter your email or phone number.");
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        errorLabel.setText(" ");

        // Simulate brief delay for realism
        Timer timer = new Timer(400, e -> {
            AuthService auth = ServiceFactory.getAuthService();
            User user = auth.login(emailOrPhone, password);
            loginBtn.setEnabled(true);
            loginBtn.setText("Sign In");

            if (user != null) {
                if (onSuccess != null) onSuccess.run();
            } else {
                showError("Invalid email/phone or password. Try demo123.");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    /** Reset fields when navigating back to this screen */
    public void reset() {
        emailField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        loginBtn.setEnabled(true);
        loginBtn.setText("Sign In");
    }
}
