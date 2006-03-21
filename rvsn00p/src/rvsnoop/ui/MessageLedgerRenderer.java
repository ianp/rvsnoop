//:File:    MessageLedgerRenderer.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import rvsnoop.MessageLedger;
import rvsnoop.RecordType;
import rvsnoop.StringUtils;

/**
 * A renderer for the message ledger.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class MessageLedgerRenderer implements TableCellRenderer {

    private static class DateCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -6397207684112537883L;
        DateCellRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            return super.getTableCellRendererComponent(table, StringUtils.format((Date) value), isSelected, hasFocus, row, col);
        }
    }

    private static void installRenderer(JTable table, Color oddRowsColor, Color evenRowsColor, Class clazz) {
        table.setDefaultRenderer(clazz, new MessageLedgerRenderer(oddRowsColor, evenRowsColor, table.getDefaultRenderer(clazz)));
    }

    /**
     * A utility method to install renderers for all of the default column types in a typical {@link JTable}.
     *
     * @param table
     * @param oddRowsColor
     * @param evenRowsColor
     */
    public static void installStripedRenderers(JTable table, Color oddRowsColor, Color evenRowsColor) {
        installRenderer(table, oddRowsColor, evenRowsColor, Object.class);
        installRenderer(table, oddRowsColor, evenRowsColor, Number.class);
        installRenderer(table, oddRowsColor, evenRowsColor, Double.class);
        installRenderer(table, oddRowsColor, evenRowsColor, Float.class);
        installRenderer(table, oddRowsColor, evenRowsColor, Icon.class);
        installRenderer(table, oddRowsColor, evenRowsColor, ImageIcon.class);
        installRenderer(table, oddRowsColor, evenRowsColor, Boolean.class);
        // Use a special renderer to pick up our preferred date format.
        table.setDefaultRenderer(Date.class, new MessageLedgerRenderer(oddRowsColor, evenRowsColor, new DateCellRenderer()));
    }

    private final TableCellRenderer baseRenderer;

    private final Color evenRowsColor;

    private final Color oddRowsColor;

    /**
     * @param evenRowsColor
     * @param oddRowsColor
     */
    public MessageLedgerRenderer(Color evenRowsColor, Color oddRowsColor, TableCellRenderer baseRenderer) {
        super();
        this.baseRenderer = baseRenderer != null ? baseRenderer : new DefaultTableCellRenderer();
        this.evenRowsColor = evenRowsColor;
        this.oddRowsColor = oddRowsColor;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        final Component component = baseRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        component.setForeground(RecordType.getFirstMatchingType(MessageLedger.INSTANCE.getRecord(row)).getColor());
        if (!isSelected) component.setBackground(row % 2 == 0 ? evenRowsColor : oddRowsColor);
        return component;
    }

}
