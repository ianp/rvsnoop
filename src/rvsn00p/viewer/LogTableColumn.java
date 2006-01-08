//:File:    LogTableColumn.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LogTableColumn
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class LogTableColumn implements Serializable {

    private static final long serialVersionUID = 7160812243483286466L;
    
    public final static LogTableColumn CONNECTION = new LogTableColumn("Connection");
    public final static LogTableColumn DATE = new LogTableColumn("Timestamp");
    public final static LogTableColumn MESSAGE_NUM = new LogTableColumn("Seq. No.");
    public final static LogTableColumn LEVEL = new LogTableColumn("Type");
    public final static LogTableColumn SUBJECT = new LogTableColumn("Subject");
    public final static LogTableColumn TID = new LogTableColumn("Tracking ID");
    public final static LogTableColumn MESSAGE = new LogTableColumn("Message");

    protected String _label;

    static LogTableColumn[] _log4JColumns;
    private static Map _logTableColumnMap;

    static {
        _log4JColumns = new LogTableColumn[] {
            CONNECTION, DATE, MESSAGE_NUM, LEVEL, SUBJECT, TID, MESSAGE
        };

        _logTableColumnMap = new HashMap();

        for (int i = 0; i < _log4JColumns.length; ++i) {
            _logTableColumnMap.put(_log4JColumns[i].getLabel(), _log4JColumns[i]);
        }
    }


    public LogTableColumn(String label) {
        super();
        _label = label;
    }

    /**
     * Return the Label of the MsgType.
     */
    public String getLabel() {
        return _label;
    }

    /**
     * Convert a column label into a LogTableColumn object.
     *
     * @param label The label to be converted into a column object.
     * @return The column corresponding to the label string or <code>null</code> if no matching column exists.
     */
    public static LogTableColumn valueOf(String label) {
        if (label == null) return null;
        return (LogTableColumn) _logTableColumnMap.get(label.trim());
    }


    public boolean equals(Object o) {
        if (o == this) return true;
        return o instanceof LogTableColumn && getLabel().equals(((LogTableColumn) o).getLabel());
    }

    public int hashCode() {
        return _label.hashCode();
    }

    public String toString() {
        return _label;
    }

    /**
     * @return A <code>List</code> of <code>LogTableColumn/code> objects that map
     * to log4j <code>Column</code> objects.
     */
    public static List getLogTableColumns() {
        return Arrays.asList(_log4JColumns);
    }

    public static LogTableColumn[] getLogTableColumnArray() {
        return _log4JColumns;
    }
}
