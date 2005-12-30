//:File:    FilteredLogTableModel.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import rvsn00p.Record;
import rvsn00p.LogRecordFilter;
import rvsn00p.PassingLogRecordFilter;
import rvsn00p.StringUtils;
import rvsn00p.util.HTMLEncoder;
import rvsn00p.util.rv.MarshalRvToString;

/**
 * A TableModel for LogRecords which includes filtering support.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class FilteredLogTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 3614054890778884099L;
    private LogRecordFilter _filter = new PassingLogRecordFilter();
    private final List _allRecords = new ArrayList();
    private List _filteredRecords;

    private int maxRecords = 5000;
    
    private final String[] _colNames = { "Date", "Msg#", "Type", "Subject", "Tracking ID", "Message" };

    private static int _lastHTMLBufLength = 1000;

    public FilteredLogTableModel() {
        super();
    }

    public void setLogRecordFilter(LogRecordFilter filter) {
        _filter = filter;
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
        final Record record = getFilteredRecord(row);
        return getColumn(col, record);
    }

    public synchronized boolean addLogRecord(Record record) {
        _allRecords.add(record);
        if (!_filter.passes(record))
            return false;
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
    
    public synchronized void removeAll(Collection records) {
        _allRecords.removeAll(records);
        _filteredRecords.removeAll(records);
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

    public List getFilteredRecords() {
        if (_filteredRecords == null)
            refresh();
        return _filteredRecords;
    }

    private List createFilteredRecordsList() {
        final List result = new ArrayList();
        for (final Iterator iter = _allRecords.iterator(); iter.hasNext();) {
            final Record record = (Record) iter.next();
            if (_filter.passes(record))
                result.add(record);
        }
        return result;
    }

    /**
     * Returns a HTML table representation of the filtered records
     */
    public StringBuffer createFilteredHTMLTable() {
        //use a buffer with the same size as the last used one
        final StringBuffer strbuf = new StringBuffer(_lastHTMLBufLength);
        final Iterator records = _filteredRecords.iterator();
        final StringBuffer buffer = new StringBuffer();
        Record current;
        addHtmlTableHeaderString(strbuf);
        while (records.hasNext()) {
            current = (Record) records.next();

            strbuf.append("<tr>\n\t");
            addHTMLTDString(current, strbuf, buffer);
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
        Record current;
        while (records.hasNext()) {
            current = (Record) records.next();
            strbuf.append("\n");
            strbuf.append(current.getMessage());
        }
        strbuf.append("\n");

        return strbuf;
    }

    /**
     * Adda a HTML <td> String representation of this Record to the buf parameter.
     * @param lr the logrecord
     * @param buf the stringbuffer to add the <td> string representation
     * @param dfMgr the date formt manager used
     */
    private void addHTMLTDString(Record lr, StringBuffer buf, StringBuffer Tbuffer) {
        if (lr != null) {
            for (int i = 0; i < getColumnCount(); ++i) {

                buf.append("<td>");
                if (i == 5) {
                    // message
                    buf.append("<code>");
                    Tbuffer.setLength(0);
                    addColumnToStringBuffer(Tbuffer, i, lr);
                    HTMLEncoder.encodeStringBuffer(Tbuffer);
                    buf.append(Tbuffer);
                    buf.append("</code>");
                } else {
                    addColumnToStringBuffer(buf, i, lr);
                }
                buf.append("</td>");
            }
        } else {
            buf.append("<td></td>");
        }
    }


    private void addHtmlTableHeaderString(StringBuffer buf) {
        // table parameters
        buf.append("<table border=\"1\" width=\"100%\">\n");
        buf.append("<tr>\n");

        // print the column headers
        for (int i = 0; i < getColumnCount(); ++i) {
            buf.append("\t<th align=\"left\" bgcolor=\"#C0C0C0\" bordercolor=\"#FFFFFF\">");

            buf.append(getColumnName(i));

            //date format
            if (i == 0)
                buf.append("<br>(").append(StringUtils.getDateFormat()).append(")");
            buf.append("</th>\n");
        }
        buf.append("</tr>\n");
    }

    public Record getFilteredRecord(int row) {
        final List records = getFilteredRecords();
        final int size = records.size();
        if (row < size) {
            return (Record) records.get(row);
        }
        // a minor problem has happened. JTable has asked for
        // a row outside the bounds, because the size of
        // _filteredRecords has changed while it was looping.
        // return the last row.
        return (Record) records.get(size - 1);

    }

    private Object getColumn(int col, Record lr) {
        if (lr == null) {
            return "NULL Column";
        }

        switch (col) {
            case 0:
                return StringUtils.format(new Date(lr.getTimestamp()));
            case 1:
                return String.valueOf(lr.getSequenceNumber());
            case 2:
                return lr.getType();
            case 3:
                return lr.getSendSubject();
            case 4:
                return lr.getTrackingId();
            case 5:
                return lr.getMessage();
            default:
                throw new IllegalArgumentException("The column number " + col + " must be between 0 and 5");
        }
    }

    private void addColumnToStringBuffer(StringBuffer sb, int col, Record lr) {
        if (lr == null)
            sb.append("NULL Column");

        switch (col) {
            case 0:
                sb.append(StringUtils.format(new Date(lr.getTimestamp())));
                break;
            case 1:
                sb.append(lr.getSequenceNumber());
                break;
            case 2:
                sb.append(lr.getType().toString());
                break;
            case 3:
                sb.append(lr.getSendSubject());
                break;
            case 4:
                sb.append(lr.getTrackingId());
                break;
            case 5:
                sb.append(MarshalRvToString.marshal("", lr.getMessage()));
                break;
            default:
                throw new IllegalArgumentException("The column number " + col + " must be between 0 and 5");
        }
    }

    /**
     * We don't want the amount of rows to grow without bound,
     * leading to a out-of-memory-exception.  Especially not good
     * in a production environment :)
     * This method & clearLogRecords() are synchronized so we don't
     * delete rows that don't exist.
     */
    private void trimRecords() {
        if (needsTrimming()) {
            trimOldestRecords();
        }
    }

    private boolean needsTrimming() {
        return (_allRecords.size() > maxRecords);
    }

    private void trimOldestRecords() {
        synchronized (_allRecords) {
            final int trim = numberOfRecordsToTrim();
            if (trim > 1) {
                final List oldRecords = _allRecords.subList(0, trim);

//                final Iterator records = oldRecords.iterator();
//                while (records.hasNext()) {
//                    Record.freeInstance((Record) records.next());
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
        return _allRecords.size() - maxRecords;
    }

    /**
     * @param maxRecords The maxRecords to set.
     */
    public void setMaxRecords(int maxRecords) {
        if (maxRecords > 0)
            this.maxRecords = maxRecords;
    }

    /**
     * @return Returns the maxRecords.
     */
    public int getMaxRecords() {
        return maxRecords;
    }

}
