//:File:    LogTable.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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
public final class LogTable extends JTable {

    private static final long serialVersionUID = -1119183751891536610L;
    private int _rowHeight = 30;

    private static final int NUM_COLUMNS = 6;
    private static final int COL_DATE = 0;
    private static final int COL_SUBJECT = 3;
    private static final int COL_TRACKING_ID = 4;
    private static final int COL_MESSAGE = 5;

    private final TableColumn[] _tableColumns = new TableColumn[NUM_COLUMNS];
    private final int[] _colWidths = {8, 1, 30, 150, 40, 100};
    private final LogTableColumn[] _colNames = LogTableColumn.getLogTableColumnArray();

    private final JTextArea detailsText;
    private final JTree detailsTree;

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

    public int getMsgColumnID() {
        return COL_MESSAGE;
    }

    public int getSubjectColumnID() {
        return COL_SUBJECT;
    }

    public int getDateColumnID() {
        return COL_DATE;
    }

    public int getTIDColumnID() {
        return COL_TRACKING_ID;
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
        final TableColumnModel model = getColumnModel();
        // Remove all the columns:
        for (int f = 0; f < NUM_COLUMNS; ++f) {
            model.removeColumn(_tableColumns[f]);
        }
        // Add them back in the correct order:
        for (int i = 0; i < NUM_COLUMNS; ++i) {
            model.addColumn(_tableColumns[i]);
        }
        //SWING BUG:
        sizeColumnsToFit(-1);
    }

    public void setView(List columns) {
        final TableColumnModel model = getColumnModel();

        // Remove all the columns:
        for (int f = 0; f < NUM_COLUMNS; ++f)
            model.removeColumn(_tableColumns[f]);
        final Iterator selectedColumns = columns.iterator();
        final List columnNameAndNumber = getColumnNameAndNumber();
        while (selectedColumns.hasNext()) {
            // add the column to the view
            model.addColumn(_tableColumns[columnNameAndNumber.indexOf(selectedColumns.next())]);
        }

        //SWING BUG:
        sizeColumnsToFit(-1);
    }

    public void setFont(Font font) {
        super.setFont(font);
        final Graphics g = this.getGraphics();
        if (g != null) {
            final FontMetrics fm = g.getFontMetrics(font);
            final int height = fm.getHeight();
            _rowHeight = height + height / 3;
            setRowHeight(_rowHeight);
        }
    }

    private void init() {
        setRowHeight(_rowHeight);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // assign a column number to a column name
    private List getColumnNameAndNumber() {
        final List columnNameAndNumber = new ArrayList();
        for (int i = 0; i < _colNames.length; ++i) {
            columnNameAndNumber.add(i, _colNames[i]);
        }
        return columnNameAndNumber;
    }

    int getColumnWidth(String name) {
        for (int i = 0; i < NUM_COLUMNS; ++i)
            if (_colNames[i].getLabel().equalsIgnoreCase(name))
                return getColumnModel().getColumn(i).getPreferredWidth();
        return 0;
    }

    void setColumnWidth(String name, int width) {
        for (int i = 0; i < NUM_COLUMNS; ++i)
            if (_colNames[i].getLabel().equalsIgnoreCase(name))
                getColumnModel().getColumn(i).setPreferredWidth(width);
    }

    private class DetailsTextListener implements ListSelectionListener {
        private final StringBuffer buffer = new StringBuffer();
        DetailsTextListener() {
            super();
        }
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) return;
            final ListSelectionModel sm = (ListSelectionModel) e.getSource();
            final int index = sm.getMinSelectionIndex();
            if (index == -1) return; // Selection is empty.
            final TableModel tm = LogTable.this.getModel();
            final Object selection = tm.getValueAt(index, NUM_COLUMNS - 1);
            if (selection == null) {
                if (detailsText != null) detailsText.setText("");
                return;
            }
            for (int i = 0; i < NUM_COLUMNS - 1; ++i) {
                final Object value = tm.getValueAt(index, i);
                buffer.append(_colNames[i]).append(":\t");
                buffer.append(value != null ? value : "").append("\n");
            }
            buffer.append(_colNames[NUM_COLUMNS - 1]);
            buffer.append(":\n");
            buffer.append(MarshalRvToString.marshal("", (TibrvMsg) selection));
            detailsText.setText(buffer.toString());
            buffer.setLength(0);
        }
    }
    
    private class DetailsTreeListener implements ListSelectionListener {
        DetailsTreeListener() {
            super();
        }
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) return;
            final ListSelectionModel sm = (ListSelectionModel) e.getSource();
            final int index = sm.getMinSelectionIndex();
            if (index == -1) return; // Selection is empty.
            final TableModel tm = LogTable.this.getModel();
            final Object selection = tm.getValueAt(index, NUM_COLUMNS - 1);
            if (selection == null) {
                if (detailsTree != null) detailsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
                return;
            }
            detailsTree.setModel(new DefaultTreeModel(new RvRootNode((TibrvMsg) selection)));
        }
    }
}
