package com.ridesharing.ui.screens;

import com.ridesharing.model.User;
import com.ridesharing.service.ServiceFactory;
import com.ridesharing.service.interfaces.AuthService;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration screen with form validation.
 */
public class RegisterScreen extends JPanel {

    private JTextField nameField, phoneField, emailField, govtIdField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> genderCombo;
    private JLabel errorLabel;
    private JButton registerBtn;

    private Runnable onSuccess;
    private Runnable onBack;

    public RegisterScreen() {
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
                new EmptyBorder(36, 48, 36, 48)
        ));

        // Back link
        JButton backBtn = AppTheme.textButton("\u2190 Back");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        JLabel title = new JLabel("Create Account");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Join the ride-sharing community");
        subtitle.setFont(AppTheme.FONT_BODY);
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form fields in a grid
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(420, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 8);

        nameField = AppTheme.styledTextField("Full name");
        phoneField = AppTheme.styledTextField("Phone number");
        emailField = AppTheme.styledTextField("Email address");
        govtIdField = AppTheme.styledTextField("Government ID (optional)");
        genderCombo = AppTheme.styledComboBox(new String[]{"Select Gender", "Male", "Female", "Other"});
        passwordField = AppTheme.styledPasswordField("Password");
        confirmPasswordField = AppTheme.styledPasswordField("Confirm password");

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Full Name *", nameField);
        addFormRow(formPanel, gbc, row++, "Phone Number *", phoneField);
        addFormRow(formPanel, gbc, row++, "Email *", emailField);
        addFormRow(formPanel, gbc, row++, "Gender *", genderCombo);
        addFormRow(formPanel, gbc, row++, "Government ID", govtIdField);
        addFormRow(formPanel, gbc, row++, "Password *", passwordField);
        addFormRow(formPanel, gbc, row++, "Confirm Password *", confirmPasswordField);

        // Error
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Register button
        registerBtn = AppTheme.primaryButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(420, AppTheme.BUTTON_HEIGHT));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> attemptRegister());

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(AppTheme.BG_PRIMARY);

        card.add(backBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(formPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(registerBtn);

        add(scrollPane);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel label = AppTheme.bodyLabel(labelText);
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void attemptRegister() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String govtId = govtIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (name.isEmpty()) { showError("Name is required."); return; }
        if (phone.isEmpty()) { showError("Phone number is required."); return; }
        if (!phone.matches("\\d{10,15}")) { showError("Enter a valid phone number (10-15 digits)."); return; }
        if (email.isEmpty()) { showError("Email is required."); return; }
        if (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) { showError("Enter a valid email."); return; }
        if ("Select Gender".equals(gender)) { showError("Please select a gender."); return; }
        if (password.isEmpty()) { showError("Password is required."); return; }
        if (password.length() < 4) { showError("Password must be at least 4 characters."); return; }
        if (!password.equals(confirmPassword)) { showError("Passwords do not match."); return; }

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating account...");
        errorLabel.setText(" ");

        Timer timer = new Timer(500, e -> {
            try {
                User user = new User(phone, name, gender, email,
                        govtId.isEmpty() ? null : govtId, null, password);
                AuthService auth = ServiceFactory.getAuthService();
                auth.register(user);
                registerBtn.setEnabled(true);
                registerBtn.setText("Create Account");
                if (onSuccess != null) onSuccess.run();
            } catch (IllegalArgumentException ex) {
                registerBtn.setEnabled(true);
                registerBtn.setText("Create Account");
                showError(ex.getMessage());
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    public void reset() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        govtIdField.setText("");
        genderCombo.setSelectedIndex(0);
        passwordField.setText("");
        confirmPasswordField.setText("");
        errorLabel.setText(" ");
    }
}
