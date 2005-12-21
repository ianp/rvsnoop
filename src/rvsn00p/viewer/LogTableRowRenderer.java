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

import rvsn00p.LogRecord;
import rvsn00p.MsgType;

/**
 * LogTableRowRenderer
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class LogTableRowRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1687156505046434584L;
    protected boolean _highlightFatal = true;
    protected Color _color = new Color(230, 230, 230);

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int col) {

        if ((row % 2) == 0) {
            setBackground(_color);
        } else {
            setBackground(Color.white);
        }

        FilteredLogTableModel model = (FilteredLogTableModel) table.getModel();
        LogRecord record = model.getFilteredRecord(row);

        setForeground(getLogLevelColor(record.getType()));

        return (super.getTableCellRendererComponent(table,
                                                    value,
                                                    isSelected,
                                                    hasFocus,
                                                    row, col));
    }

    protected Color getLogLevelColor(MsgType level) {
        return (Color) MsgType.getLogLevelColorMap().get(level);
    }

}
