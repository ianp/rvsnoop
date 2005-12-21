//:File:    FilteredLogTableModel.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import rvsn00p.LogRecord;
import rvsn00p.LogRecordFilter;
import rvsn00p.PassingLogRecordFilter;
import rvsn00p.util.DateFormatManager;
import rvsn00p.util.HTMLEncoder;

/**
 * A TableModel for LogRecords which includes filtering support.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class FilteredLogTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 3614054890778884099L;
    protected LogRecordFilter _filter = new PassingLogRecordFilter();
    protected List _allRecords = new ArrayList();
    protected List _filteredRecords;
    protected int _maxNumberOfLogRecords = 5000;
    protected String[] _colNames = {"Date",
                                    "Msg#",
                                    "Type",
                                    "Subject",
                                    "Tracking ID",
                                    "Message"};

    protected DateFormatManager _dfm = null;
    static int _lastHTMLBufLength = 1000;
    final protected Date _conversionDate = new Date();
    final protected StringBuffer _conversionStrBuf = new StringBuffer(15);
    final protected StringBuffer _outStrBuf = new StringBuffer(15);

    public FilteredLogTableModel() {
        super();
    }

    public void setDateFormatManager(DateFormatManager dfm) {
        if (dfm != null)
            this._dfm = dfm;
    }

    public DateFormatManager getDateFormatManager() {
        return this._dfm;
    }

    public void setLogRecordFilter(LogRecordFilter filter) {
        _filter = filter;
    }

    public LogRecordFilter getLogRecordFilter() {
        return _filter;
    }

    public String getColumnName(int i) {
        return _colNames[i];
    }

    public int getColumnCount() {
        return _colNames.length;
    }

    public int getRowCount() {
        return getFilteredRecords().size();
    }

    public int getTotalRowCount() {
        return _allRecords.size();
    }

    public Object getValueAt(int row, int col) {
        LogRecord record = getFilteredRecord(row);
        return getColumn(col, record);
    }

    public void setMaxNumberOfLogRecords(int maxNumRecords) {
        if (maxNumRecords > 0) {
            _maxNumberOfLogRecords = maxNumRecords;
        }

    }

    public synchronized boolean addLogRecord(LogRecord record) {

        _allRecords.add(record);

        if (_filter.passes(record) == false) {
            return false;
        }
        getFilteredRecords().add(record);
        fireTableRowsInserted(getRowCount(), getRowCount());
        trimRecords();
        return true;
    }

    /**
     * Forces the LogTableModel to requery its filters to determine
     * which records to display.
     */
    public synchronized void refresh() {
        _filteredRecords = createFilteredRecordsList();
        fireTableDataChanged();
    }

    public synchronized void fastRefresh() {
        _filteredRecords.remove(0);
        fireTableRowsDeleted(0, 0);
    }


    /**
     * Clears all records from the LogTableModel
     */
    public synchronized void clear() {
        _allRecords.clear();
        _filteredRecords.clear();
        fireTableDataChanged();
    }

    protected List getFilteredRecords() {
        if (_filteredRecords == null) {
            refresh();
        }
        return _filteredRecords;
    }

    protected List createFilteredRecordsList() {
        List result = new ArrayList();
        Iterator records = _allRecords.iterator();
        LogRecord current;
        while (records.hasNext()) {
            current = (LogRecord) records.next();
            if (_filter.passes(current)) {
                result.add(current);
            }
        }
        return result;
    }

    /**
     * Returns a HTML table representation of the filtered records
     * @param dfMgr the date formt manager used
     */
    public StringBuffer createFilteredHTMLTable(DateFormatManager dfMgr) {
        //use a buffer with the same size as the last used one
        final StringBuffer strbuf = new StringBuffer(_lastHTMLBufLength);
        final Iterator records = _filteredRecords.iterator();
        final StringBuffer buffer = new StringBuffer();
        LogRecord current;
        addHtmlTableHeaderString(strbuf, dfMgr);
        while (records.hasNext()) {
            current = (LogRecord) records.next();

            strbuf.append("<tr>\n\t");
            addHTMLTDString(current, strbuf, dfMgr, buffer);
            strbuf.append("\n</tr>\n");
        }
        strbuf.append("</table>");

        //remember last buffer size
        _lastHTMLBufLength = strbuf.length() + 2;

        return strbuf;
    }

    /**
     * createFilteredTextFromMsg.
     * Returns a text string containing all message fields delimite
     */
    public StringBuffer createFilteredTextFromMsg() {
        final StringBuffer strbuf = new StringBuffer();
        final Iterator records = _filteredRecords.iterator();
        LogRecord current;
        while (records.hasNext()) {
            current = (LogRecord) records.next();
            strbuf.append("\n");
            strbuf.append(current.getMessage());
        }
        strbuf.append("\n");

        return strbuf;
    }

    /**
     * Adda a HTML <td> String representation of this LogRecord to the buf parameter.
     * @param lr the logrecord
     * @param buf the stringbuffer to add the <td> string representation
     * @param dfMgr the date formt manager used
     */
    protected void addHTMLTDString(LogRecord lr, StringBuffer buf, DateFormatManager dfMgr, StringBuffer Tbuffer) {
        if (lr != null) {
            for (int i = 0; i < getColumnCount(); ++i) {

                buf.append("<td>");
                if (i == 5) {
                    // message
                    buf.append("<code>");
                    Tbuffer.setLength(0);
                    addColumnToStringBuffer(Tbuffer, i, lr, dfMgr);
                    HTMLEncoder.encodeStringBuffer(Tbuffer);
                    buf.append(Tbuffer);
                    buf.append("</code>");
                } else {
                    addColumnToStringBuffer(buf, i, lr, dfMgr);
                }
                buf.append("</td>");
            }
        } else {
            buf.append("<td></td>");
        }
    }


    protected void addHtmlTableHeaderString(StringBuffer buf, DateFormatManager dfMgr) {
        // table parameters
        buf.append("<table border=\"1\" width=\"100%\">\n");
        buf.append("<tr>\n");

        // print the column headers
        for (int i = 0; i < getColumnCount(); ++i) {
            buf.append("\t<th align=\"left\" bgcolor=\"#C0C0C0\" bordercolor=\"#FFFFFF\">");

            buf.append(getColumnName(i));

            //date format
            if (i == 0) {
                buf.append("<br>(");
                buf.append(dfMgr.getPattern());
                buf.append(")");
            }
            buf.append("</th>\n");
        }
        buf.append("</tr>\n");
    }

    protected LogRecord getFilteredRecord(int row) {
        final List records = getFilteredRecords();
        final int size = records.size();
        if (row < size) {
            return (LogRecord) records.get(row);
        }
        // a minor problem has happened. JTable has asked for
        // a row outside the bounds, because the size of
        // _filteredRecords has changed while it was looping.
        // return the last row.
        return (LogRecord) records.get(size - 1);

    }

    protected Object getColumn(int col, LogRecord lr, DateFormatManager dfm) {
        if (lr == null) {
            return "NULL Column";
        }

        switch (col) {
            case 0:
                synchronized (_conversionDate) {
                    _conversionDate.setTime(lr.getMillis());
                    _conversionStrBuf.setLength(0);
                    _outStrBuf.setLength(0);
                    _outStrBuf.append(dfm.format(_conversionDate, _conversionStrBuf));
                    return _outStrBuf.toString();
                }
            case 1:
                return String.valueOf(lr.getSequenceNumber());
            case 2:
                return lr.getType();
            case 3:
                return lr.getSubject();
            case 4:
                return lr.getTrackingID();
            case 5:
                return lr.getMessage();
            default:
                String message = "The column number " + col + " must be between 0 and 5";
                throw new IllegalArgumentException(message);
        }
    }

    protected void addColumnToStringBuffer(StringBuffer sb, int col, LogRecord lr, DateFormatManager dfm) {
        if (lr == null) {
            sb.append("NULL Column");
        }

        switch (col) {
            case 0:
                synchronized (_conversionDate) {
                    _conversionDate.setTime(lr.getMillis());
                    _conversionStrBuf.setLength(0);
                    sb.append(dfm.format(_conversionDate, _conversionStrBuf));
                }
                break;
            case 1:
                sb.append(lr.getSequenceNumber());
                break;
            case 2:
                sb.append(lr.getType().toString());
                break;
            case 3:
                sb.append(lr.getSubjectAsStringBuffer());
                break;
            case 4:
                sb.append(lr.getTrackingIDStringBuffer());
                break;
            case 5:
                sb.append("message with ").append(lr.getMessage().getNumFields()).append(" fields");
                break;
            default:
                String message = "The column number " + col + " must be between 0 and 5";
                throw new IllegalArgumentException(message);
        }
    }

    protected void addColumnToStringBuffer(StringBuffer sb, int col, LogRecord lr) {
        addColumnToStringBuffer(sb, col, lr, _dfm);
    }

    protected Object getColumn(int col, LogRecord lr) {
        return getColumn(col, lr, _dfm);
    }


    /**
     * We don't want the amount of rows to grow without bound,
     * leading to a out-of-memory-exception.  Especially not good
     * in a production environment :)
     * This method & clearLogRecords() are synchronized so we don't
     * delete rows that don't exist.
     */
    protected void trimRecords() {
        if (needsTrimming()) {
            trimOldestRecords();
        }
    }

    protected boolean needsTrimming() {
        return (_allRecords.size() > _maxNumberOfLogRecords);
    }

    protected void trimOldestRecords() {
        synchronized (_allRecords) {
            final int trim = numberOfRecordsToTrim();
            if (trim > 1) {
                List oldRecords = _allRecords.subList(0, trim);

//                final Iterator records = oldRecords.iterator();
//                while (records.hasNext()) {
//                    LogRecord.freeInstance((LogRecord) records.next());
//                }
                oldRecords.clear();
                refresh();
            } else {
                _allRecords.remove(0);
                fastRefresh();
            }
        }

    }

    private int numberOfRecordsToTrim() {
        return _allRecords.size() - _maxNumberOfLogRecords;
    }

}
