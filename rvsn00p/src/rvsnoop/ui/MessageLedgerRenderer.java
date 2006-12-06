//:File:    MessageLedgerRenderer.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import rvsnoop.Logger;
import rvsnoop.MessageLedger;
import rvsnoop.RecordTypes;

/**
 * A renderer for the message ledger.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class MessageLedgerRenderer implements TableCellRenderer {

    /**
     * Cell renderer that selects a date representation based on the current column width.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     */
    private static class DateCellRenderer extends DefaultTableCellRenderer {
        private static final Logger logger = Logger.getLogger(DateCellRenderer.class);
        private static final long serialVersionUID = -6397207684112537883L;
        private static final DateFormat[] FORMATS = {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
            new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS"),
            new SimpleDateFormat("MM/dd HH:mm:ss.SSS"),
            new SimpleDateFormat("HH:mm:ss.SSS"),
            new SimpleDateFormat("HH:mm:ss.SS"),
            new SimpleDateFormat("HH:mm:ss.S"),
            new SimpleDateFormat("HH:mm:ss"),
            new SimpleDateFormat("HH:mm") };
        private int currentWidth;
        private Font currentFont;
        private DateFormat currentFormat;
        DateCellRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            final DateFormat format = getFormat(table.getColumnModel().getColumn(col).getWidth(), table);
            final String displayed = value != null ? format.format((Date) value) : "";
            return super.getTableCellRendererComponent(table, displayed, isSelected, hasFocus, row, col);
        }
        private DateFormat getFormat(int width, JTable table) {
            final Font font = table.getFont();
            if (currentWidth != width) currentFormat = null;
            if (currentFont == null || !currentFont.equals(font)) currentFormat = null;
            if (currentFormat == null) {
                currentWidth = width;
                currentFont = font;
                final Date date = new Date();
                final FontMetrics metrics = table.getFontMetrics(font);
                for (int i = 0, imax = FORMATS.length; i < imax; ++i) {
                    final int dateWidth = metrics.stringWidth(FORMATS[i].format(date));
                    if (dateWidth < width) {
                        currentFormat = FORMATS[i];
                        if (Logger.isDebugEnabled())
                            logger.debug("Setting date format to " + FORMATS[i].format(date));
                        break;
                    }
                }
                if (currentFormat == null) {
                    currentFormat = FORMATS[FORMATS.length - 1];
                    if (Logger.isDebugEnabled())
                        logger.debug("Setting date format to " + FORMATS[FORMATS.length - 1].format(date));
                }
            }
            return currentFormat;
        }
    }

    /**
     * A utility method to install renderers for all of the default column types in a typical {@link JTable}.
     *
     * @param table
     */
    public static void installStripedRenderers(JTable table) {
        final MessageLedgerRenderer dateRenderer = new MessageLedgerRenderer(new DateCellRenderer());
        final MessageLedgerRenderer renderer = new MessageLedgerRenderer(null);
        final TableColumnModel columns = table.getColumnModel();
        for (int i = 0, imax = columns.getColumnCount(); i < imax; ++i) {
            final Class columnClass = table.getColumnClass(i);
            if (Date.class.isAssignableFrom(columnClass))
                columns.getColumn(i).setCellRenderer(dateRenderer);
            else
                columns.getColumn(i).setCellRenderer(renderer);
        }
    }

    private final TableCellRenderer baseRenderer;

    private final Color evenRowsColor;

    private final Color oddRowsColor;

    private final RecordTypes types = RecordTypes.getInstance();

    private MessageLedgerRenderer(TableCellRenderer baseRenderer) {
        this.baseRenderer = baseRenderer;
        this.evenRowsColor = Color.WHITE;
        this.oddRowsColor = new Color(229, 229, 255);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        TableCellRenderer renderer = baseRenderer;
        if (renderer == null) {
            final Class rendererClass = value != null ? value.getClass() : Object.class;
            renderer = table.getDefaultRenderer(rendererClass);
        }
        final Component component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        if (!isSelected) {
            component.setForeground(types.getFirstMatchingType(MessageLedger.INSTANCE.getRecord(row)).getColour());
            component.setBackground(row % 2 == 0 ? evenRowsColor : oddRowsColor);
        }
        return component;
    }

}
