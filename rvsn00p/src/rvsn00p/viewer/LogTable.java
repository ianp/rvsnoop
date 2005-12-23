//:File:    LogTable.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import rvsn00p.util.DateFormatManager;
import rvsn00p.util.rv.MarshalRvToString;
import rvsn00p.util.rv.RvRootNode;

import com.tibco.tibrv.TibrvMsg;

/**
 * LogTable.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class LogTable extends JTable {

    private static final long serialVersionUID = -1119183751891536610L;
    protected int _rowHeight = 30;

    // For the columns:
    protected int _numCols = 6;
    protected TableColumn[] _tableColumns = new TableColumn[_numCols];
    protected int[] _colWidths = {8, 1, 30, 150, 40, 100};
    protected LogTableColumn[] _colNames = LogTableColumn.getLogTableColumnArray();
    protected int _colDate = 0;
    protected int _colMessageNum = 1;
    protected int _colLevel = 2;
    protected int _colSubject = 3;
    protected int _colTrackingID = 4;
    protected int _colMessage = 5;
    protected StringBuffer _buf = new StringBuffer();

    final JTextArea detailsText;
    final JTree detailsTree;

    public LogTable(JTree detailsTree, JTextArea detailsText) {
        super();

        init();

        this.detailsText = detailsText;
        this.detailsTree = detailsTree;

        setModel(new FilteredLogTableModel());

        Enumeration columns = getColumnModel().getColumns();
        int i = 0;
        while (columns.hasMoreElements()) {
            TableColumn col = (TableColumn) columns.nextElement();
            col.setCellRenderer(new LogTableRowRenderer());
            col.setPreferredWidth(_colWidths[i]);

            _tableColumns[i] = col;
            ++i;
        }


        ListSelectionModel rowSM = getSelectionModel();
        if (detailsText != null) rowSM.addListSelectionListener(new DetailsTextListener());
        if (detailsTree != null) rowSM.addListSelectionListener(new DetailsTreeListener());

        //setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    /**
     * Get the DateFormatManager for formatting dates.
     */
    public DateFormatManager getDateFormatManager() {
        return getFilteredLogTableModel().getDateFormatManager();
    }

    /**
     * Set the date format manager for formatting dates.
     */
    public void setDateFormatManager(DateFormatManager dfm) {
        //_dateFormatManager = dfm;
        getFilteredLogTableModel().setDateFormatManager(dfm);
    }

    public int getMsgColumnID() {
        return _colMessage;
    }

    public int getSubjectColumnID() {
        return _colSubject;
    }

    public int getDateColumnID() {
        return _colDate;
    }

    public int getTIDColumnID() {
        return _colTrackingID;
    }

    public synchronized void clearLogRecords() {
        //For JDK1.3
        //((DefaultTableModel)getModel()).setRowCount(0);

        // For JDK1.2.x
        getFilteredLogTableModel().clear();
    }

    public FilteredLogTableModel getFilteredLogTableModel() {
        return (FilteredLogTableModel) getModel();
    }

    // default view if a view is not set and saved
    public void setDetailedView() {
        //TODO: Defineable Views.
        TableColumnModel model = getColumnModel();
        // Remove all the columns:
        for (int f = 0; f < _numCols; ++f) {
            model.removeColumn(_tableColumns[f]);
        }
        // Add them back in the correct order:
        for (int i = 0; i < _numCols; ++i) {
            model.addColumn(_tableColumns[i]);
        }
        //SWING BUG:
        sizeColumnsToFit(-1);
    }

    public void setView(List columns) {
        TableColumnModel model = getColumnModel();

        // Remove all the columns:
        for (int f = 0; f < _numCols; ++f) {
            model.removeColumn(_tableColumns[f]);
        }
        Iterator selectedColumns = columns.iterator();
        Vector columnNameAndNumber = getColumnNameAndNumber();
        while (selectedColumns.hasNext()) {
            // add the column to the view
            model.addColumn(_tableColumns[columnNameAndNumber.indexOf(selectedColumns.next())]);
        }

        //SWING BUG:
        sizeColumnsToFit(-1);
    }

    public void setFont(Font font) {
        super.setFont(font);
        Graphics g = this.getGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(font);
            int height = fm.getHeight();
            _rowHeight = height + height / 3;
            setRowHeight(_rowHeight);
        }
    }

    protected void init() {
        setRowHeight(_rowHeight);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // assign a column number to a column name
    protected Vector getColumnNameAndNumber() {
        Vector columnNameAndNumber = new Vector();
        for (int i = 0; i < _colNames.length; ++i) {
            columnNameAndNumber.add(i, _colNames[i]);
        }
        return columnNameAndNumber;
    }

    protected int getColumnWidth(String name) {
        try {
            for (int i = 0; i < _numCols; ++i) {
                if (_colNames[i].getLabel().equalsIgnoreCase(name)) {
                    TableColumnModel model = getColumnModel();
                    return model.getColumn(i).getPreferredWidth();
                }
            }
        } catch (Exception ex) {
            return 0;
        }

        return 0;
    }

    void setColumnWidth(String name, int width) {
        try {
            for (int i = 0; i < _numCols; ++i) {
                if (_colNames[i].getLabel().equalsIgnoreCase(name)) {
                    TableColumnModel model = getColumnModel();
                    model.getColumn(i).setPreferredWidth(width);
                }
            }
        } catch (Exception ignored) {
            // Intentionally ignored.
        }
    }

    private class DetailsTextListener implements ListSelectionListener {
        private final StringBuffer buffer = new StringBuffer();
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) return;
            ListSelectionModel sm = (ListSelectionModel) e.getSource();
            int index = sm.getMinSelectionIndex();
            if (index == -1) return; // Selection is empty.
            TableModel tm = LogTable.this.getModel();
            Object selection = tm.getValueAt(index, _numCols - 1);
            if (selection == null) {
                if (detailsText != null) detailsText.setText("");
                return;
            }
            for (int i = 0; i < _numCols - 1; ++i) {
                Object value = tm.getValueAt(index, i);
                buffer.append(_colNames[i]).append(":\t");
                buffer.append(value != null ? value : "").append("\n");
            }
            buffer.append(_colNames[_numCols - 1]);
            buffer.append(":\n");
            buffer.append(MarshalRvToString.marshal("", (TibrvMsg) selection));
            detailsText.setText(buffer.toString());
            buffer.setLength(0);
        }
    }
    
    private class DetailsTreeListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) return;
            ListSelectionModel sm = (ListSelectionModel) e.getSource();
            int index = sm.getMinSelectionIndex();
            if (index == -1) return; // Selection is empty.
            TableModel tm = LogTable.this.getModel();
            Object selection = tm.getValueAt(index, _numCols - 1);
            if (selection == null) {
                if (detailsTree != null) detailsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
                return;
            }
            detailsTree.setModel(new DefaultTreeModel(new RvRootNode((TibrvMsg) selection)));
        }
    }
}
