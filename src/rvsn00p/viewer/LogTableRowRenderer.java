//:File:    LogTableRowRenderer.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import rvsnoop.MsgType;
import rvsnoop.Record;

/**
 * LogTableRowRenderer
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class LogTableRowRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1687156505046434585L;

    private final Color _color = new Color(230, 230, 230);

    public LogTableRowRenderer() {
        super();
    }
    
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int col) {

        setBackground(row % 2 == 0 ? _color : Color.WHITE);

        final FilteredLogTableModel model = (FilteredLogTableModel) table.getModel();
        final Record record = model.getFilteredRecord(row);

        setForeground(getLogLevelColor(record.getType()));
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
    }

    private Color getLogLevelColor(MsgType level) {
        return (Color) MsgType.getLogLevelColorMap().get(level);
    }

}
