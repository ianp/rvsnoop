/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.viewer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LogTableColumn
 *
 * @author Michael J. Sikorsky
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public class LogTableColumn implements java.io.Serializable {

    // log4j table columns.
    public final static LogTableColumn DATE = new LogTableColumn("Date");
    public final static LogTableColumn MESSAGE_NUM = new LogTableColumn("Msg#");
    public final static LogTableColumn LEVEL = new LogTableColumn("Type");
    public final static LogTableColumn SUBJECT = new LogTableColumn("Subject");
    public final static LogTableColumn MESSAGE = new LogTableColumn("Message");

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
    protected String _label;

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------
    private static LogTableColumn[] _log4JColumns;
    private static Map _logTableColumnMap;

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------
    static {
        _log4JColumns = new LogTableColumn[]{DATE, MESSAGE_NUM, LEVEL, SUBJECT,
                                             MESSAGE};

        _logTableColumnMap = new HashMap();

        for (int i = 0; i < _log4JColumns.length; ++i) {
            _logTableColumnMap.put(_log4JColumns[i].getLabel(), _log4JColumns[i]);
        }
    }


    public LogTableColumn(String label) {
        _label = label;
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    /**
     * Return the Label of the MsgType.
     */
    public String getLabel() {
        return _label;
    }

    /**
     * Convert a column label into a LogTableColumn object.
     *
     * @param level The label of a level to be converted into a LogTableColumn.
     * @return LogTableColumn The LogTableColumn with a label equal to column.
     * @throws LogTableColumnFormatException Is thrown when the column can not be
     *         converted into a LogTableColumn.
     */
    public static LogTableColumn valueOf(String column)
            throws LogTableColumnFormatException {
        LogTableColumn tableColumn = null;
        if (column != null) {
            column = column.trim();
            tableColumn = (LogTableColumn) _logTableColumnMap.get(column);
        }

        if (tableColumn == null) {
            StringBuffer buf = new StringBuffer();
            buf.append("Error while trying to parse (" + column + ") into");
            buf.append(" a LogTableColumn.");
            throw new LogTableColumnFormatException(buf.toString());
        }
        return tableColumn;
    }


    public boolean equals(Object o) {
        boolean equals = false;

        if (o instanceof LogTableColumn) {
            if (this.getLabel() ==
                    ((LogTableColumn) o).getLabel()) {
                equals = true;
            }
        }

        return equals;
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

    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

}






