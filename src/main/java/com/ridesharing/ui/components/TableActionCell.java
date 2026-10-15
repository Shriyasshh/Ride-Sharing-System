package com.ridesharing.ui.components;

import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Reusable table action button renderer and editor.
 * Gracefully handles active action buttons and inactive dash ("—") indicators.
 */
public final class TableActionCell {

    private TableActionCell() {}

    /**
     * Cell renderer for action column buttons.
     */
    public static class ActionButtonRenderer extends DefaultTableCellRenderer {
        private final JButton button;
        private final JLabel dashLabel;

        public ActionButtonRenderer(String defaultText) {
            button = AppTheme.textButton(defaultText);
            button.setForeground(AppTheme.PRIMARY);

            dashLabel = new JLabel("—", SwingConstants.CENTER);
            dashLabel.setFont(AppTheme.FONT_BODY);
            dashLabel.setForeground(AppTheme.TEXT_MUTED);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value != null ? value.toString() : "";
            if (text.isEmpty() || "—".equals(text) || "-".equals(text)) {
                return dashLabel;
            }
            button.setText(text);
            return button;
        }
    }

    /**
     * Cell editor for action column buttons that executes an action callback.
     */
    public static class ActionButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final JLabel dashLabel;
        private String currentAction = "";
        private int currentRow = -1;

        public ActionButtonEditor(Consumer<Integer> onAction) {
            super(new JCheckBox());
            button = AppTheme.textButton("");
            button.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0 && onAction != null && !"—".equals(currentAction)) {
                    onAction.accept(currentRow);
                }
            });

            dashLabel = new JLabel("—", SwingConstants.CENTER);
            dashLabel.setFont(AppTheme.FONT_BODY);
            dashLabel.setForeground(AppTheme.TEXT_MUTED);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            currentAction = value != null ? value.toString() : "";
            if (currentAction.isEmpty() || "—".equals(currentAction) || "-".equals(currentAction)) {
                return dashLabel;
            }
            button.setText(currentAction);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return currentAction;
        }
    }
}
