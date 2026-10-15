package com.ridesharing.ui.components;

import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern metric card component displaying an icon, primary value, and subtitle label.
 */
public class MetricCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel subtitleLabel;

    public MetricCard(String icon, String title, String value, Color accentColor, Color bgColor) {
        setLayout(new BorderLayout(12, 0));
        setBackground(AppTheme.BG_CARD);
        setBorder(AppTheme.cardBorder());

        // Left Icon badge
        JLabel iconLbl = new JLabel(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 26));
        iconLbl.setForeground(accentColor);
        iconLbl.setOpaque(false);
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconLbl.setPreferredSize(new Dimension(52, 52));

        // Right Text stack
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);

        subtitleLabel = new JLabel(title);
        subtitleLabel.setFont(AppTheme.FONT_SMALL);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(subtitleLabel);

        add(iconLbl, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setTitle(String title) {
        subtitleLabel.setText(title);
    }
}
