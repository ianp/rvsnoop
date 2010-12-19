// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import rvsnoop.Record;
import rvsnoop.RecordTypes;
import rvsnoop.RvConnection;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

/**
 * A format that describes the contents of a record ledger.
 */
public final class RecordLedgerFormat implements AdvancedTableFormat<Record> {

    static String CONNECTION_NAME, MESSAGE_NAME, SEQUENCE_NO_NAME,
        SIZE_IN_BYTES_NAME, SUBJECT_NAME, TIMESTAMP_NAME, TRACKING_ID_NAME,
        TYPE_NAME;

    static { NLSUtils.internationalize(RecordLedgerFormat.class); }

    private static ColumnFormat CONNECTION = new ColumnFormat("connection", CONNECTION_NAME, String.class, new ComparableComparator()) {
        @Override
        public Object getValue(Record record) {
            final RvConnection connection = record.getConnection();
            return connection != null ? connection.getDescription() : "";
        }
    };

    public static final ColumnFormat MESSAGE = new ColumnFormat("message", MESSAGE_NAME, Object.class) {
        @Override
        public Object getValue(Record record) {
            return record.getMessage();
        }
    };

    private static final ColumnFormat SEQUENCE_NO = new ColumnFormat("sequence", SEQUENCE_NO_NAME, Long.class, new ComparableComparator()) {
        @Override
        public Object getValue(Record record) {
            return Long.toString(record.getSequenceNumber());
        }
    };

    private static final ColumnFormat SIZE_IN_BYTES = new ColumnFormat("size", SIZE_IN_BYTES_NAME, Integer.class, new ComparableComparator()) {
        @Override
        public Object getValue(Record record) {
            return record.getSizeInBytes();
        }
    };

    private static final ColumnFormat SUBJECT = new ColumnFormat("subject", SUBJECT_NAME, String.class, new ComparableComparator()) {
        @Override
        public Object getValue(Record record) {
            return record.getSendSubject();
        }
    };

    private static final ColumnFormat TIMESTAMP = new ColumnFormat("timestamp", TIMESTAMP_NAME, Date.class, new ComparableComparator()) {
        @Override
        public Object getValue(Record record) {
            return new Date(record.getTimestamp());
        }
    };

    private static final ColumnFormat TRACKING_ID = new ColumnFormat("tracking", TRACKING_ID_NAME, String.class, new ComparableComparator()) {
        @Override
        public Object getValue(Record record) {
            return record.getTrackingId();
        }
    };

    private static final ColumnFormat TYPE = new ColumnFormat("type", TYPE_NAME, String.class, new ComparableComparator()) {
        //final RecordTypes types = RecordTypes.getInstance();
        @Override
        public Object getValue(Record record) {
            //return types.getFirstMatchingType(record).getName();
            return null;
        }
    };

    public static final List<ColumnFormat> ALL_COLUMNS = Collections.unmodifiableList(
        Arrays.asList(CONNECTION, TIMESTAMP, SEQUENCE_NO, TYPE, SUBJECT, SIZE_IN_BYTES, TRACKING_ID, MESSAGE));

    /**
     * Gets a column format by it's ID.
     *
     * @param id The ID of the column.
     * @return The column, or <code>null</code> if none match the supplied ID.
     */
    public static ColumnFormat getColumn(String id) {
        for (int i = 0, imax = ALL_COLUMNS.size(); i < imax; ++i) {
            final ColumnFormat column = ALL_COLUMNS.get(i);
            if (column.getIdentifier().equals(id)) { return column; }
        }
        return null;
    }

    private final List<ColumnFormat> columns = new ArrayList<ColumnFormat>(ALL_COLUMNS);

    private EventTableModel<Record> model;

    /**
     * Create a new <code>RecordLedgerFormat</code>.
     */
    public RecordLedgerFormat() {
        super();
        // Do not display the message by default.
        columns.remove(MESSAGE);
    }

    /**
     * Add a column to this format.
     *
     * @param column The column to add.
     */
    public void add(ColumnFormat column) {
        if (columns.contains(column)) { return; }
        columns.add(column);
        if (model != null) { model.setTableFormat(this); }
    }

    /**
     * Is a given column visible in this format.
     *
     * @param column The column to check.
     * @return <code>true</code> if the column is currently visible,
     *     <code>false</code> otherwise.
     */
    public boolean contains(ColumnFormat column) {
        return columns.contains(column);
    }

    public Class<?> getColumnClass(int column) {
        return columns.get(column).getClazz();
    }

    public Comparator<?> getColumnComparator(int column) {
        return columns.get(column).getComparator();
    }

    public int getColumnCount() {
        return columns.size();
    }

    public String getColumnName(int index) {
        return columns.get(index).getName();
    }

    /**
     * Get a read only list of the columns currently displayed by this format.
     *
     * @return A <em>read-only</em> list containing the columns.
     */
    public List<ColumnFormat> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public Object getColumnValue(Record record, int index) {
        return columns.get(index).getValue(record);
    }

    /**
     * Remove a column from this format.
     *
     * @param column The column to remove.
     */
    public void remove(ColumnFormat column) {
        columns.remove(column);
        if (model != null) { model.setTableFormat(this); }
    }

    /**
     * Set the columns included in this format.
     *
     * @param columns The columns to set.
     */
    public void setColumns(List<ColumnFormat> columns) {
        columns.clear();
        columns.addAll(columns);
        if (model != null) { model.setTableFormat(this); }
    }

    /**
     * Set the model that will display this format.
     * <p>
     * If a model has been set it will be automatically notified whenever a
     * column is added to or removed from this format.
     *
     * @param model the model to set.
     */
    void setModel(EventTableModel<Record> model) {
        this.model = model;
    }

    @SuppressWarnings("unchecked")
	private static final class ComparableComparator implements Comparator<Comparable> {
        ComparableComparator() {
            super();
        }
        public int compare(Comparable o1, Comparable o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Format information for a single column.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     */
    public abstract static class ColumnFormat {
        private final Class<?> clazz;
        private final Comparator<?> comparator;
        private final String identifier;
        private final String name;
        ColumnFormat(String id, String name, Class<?> clazz) {
            this(id, name, clazz, Collator.getInstance());
        }
        ColumnFormat(String id, String name, Class<?> clazz, Comparator<?> comparator) {
            this.clazz = clazz;
            this.comparator = comparator;
            this.identifier = id;
            this.name = name;
        }
        public Class<?> getClazz() {
            return clazz;
        }
        public Comparator<?> getComparator() {
            return comparator;
        }
        public final String getIdentifier() {
            return identifier;
        }
        public final String getName() {
            return name;
        }
        public abstract Object getValue(Record record);
    }

}
