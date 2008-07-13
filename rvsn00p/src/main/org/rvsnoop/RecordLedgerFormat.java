/*
 * Class:     RecordLedgerFormat
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
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
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecordLedgerFormat implements AdvancedTableFormat<Record> {

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

    static String CONNECTION_NAME, MESSAGE_NAME, SEQUENCE_NO_NAME,
        SIZE_IN_BYTES_NAME, SUBJECT_NAME, TIMESTAMP_NAME, TRACKING_ID_NAME,
        TYPE_NAME;

    static { NLSUtils.internationalize(RecordLedgerFormat.class); }

    public static ColumnFormat CONNECTION = new ColumnFormat("connection", CONNECTION_NAME, String.class, new ComparableComparator()) {
        private static final long serialVersionUID = -4938884664732119140L;
        @Override
        public Object getValue(Record record) {
            final RvConnection connection = record.getConnection();
            return connection != null ? connection.getDescription() : "";
        }
    };

    public static final ColumnFormat MESSAGE = new ColumnFormat("message", MESSAGE_NAME, Object.class) {
        private static final long serialVersionUID = 3526646591996520301L;
        @Override
        public Object getValue(Record record) {
            return record.getMessage();
        }
    };

    public static final ColumnFormat SEQUENCE_NO = new ColumnFormat("sequence", SEQUENCE_NO_NAME, Long.class, new ComparableComparator()) {
        private static final long serialVersionUID = -5762735421370128862L;
        @Override
        public Object getValue(Record record) {
            return Long.toString(record.getSequenceNumber());
        }
    };

    public static final ColumnFormat SIZE_IN_BYTES = new ColumnFormat("size", SIZE_IN_BYTES_NAME, Integer.class, new ComparableComparator()) {
        private static final long serialVersionUID = 2274660797219318776L;
        @Override
        public Object getValue(Record record) {
            return new Integer(record.getSizeInBytes());
        }
    };

    public static final ColumnFormat SUBJECT = new ColumnFormat("subject", SUBJECT_NAME, String.class, new ComparableComparator()) {
        private static final long serialVersionUID = 7415054603888398693L;
        @Override
        public Object getValue(Record record) {
            return record.getSendSubject();
        }
    };

    public static final ColumnFormat TIMESTAMP = new ColumnFormat("timestamp", TIMESTAMP_NAME, Date.class, new ComparableComparator()) {
        private static final long serialVersionUID = -3858078006527711756L;
        @Override
        public Object getValue(Record record) {
            return new Date(record.getTimestamp());
        }
    };

    public static final ColumnFormat TRACKING_ID = new ColumnFormat("tracking", TRACKING_ID_NAME, String.class, new ComparableComparator()) {
        private static final long serialVersionUID = -3033175036104293820L;
        @Override
        public Object getValue(Record record) {
            return record.getTrackingId();
        }
    };

    public static final ColumnFormat TYPE = new ColumnFormat("type", TYPE_NAME, String.class, new ComparableComparator()) {
        private static final long serialVersionUID = 6353909616946303068L;
        final RecordTypes types = RecordTypes.getInstance();
        @Override
        public Object getValue(Record record) {
            return types.getFirstMatchingType(record).getName();
        }
    };

    public static final List<ColumnFormat> ALL_COLUMNS = Collections.unmodifiableList(
        Arrays.asList(new ColumnFormat[] {
            CONNECTION, TIMESTAMP, SEQUENCE_NO, TYPE,
            SUBJECT, SIZE_IN_BYTES, TRACKING_ID, MESSAGE
    }));

    public static String displayNameToIdentifier(String displayName) {
        for (int i = 0, imax = ALL_COLUMNS.size(); i < imax; ++i) {
            final ColumnFormat column = (ColumnFormat) ALL_COLUMNS.get(i);
            if (column.getName().equals(displayName)) {
                return column.identifier;
            }
        }
        return null;
    }

    /**
     * Gets a column format by it's ID.
     *
     * @param id The ID of the column.
     * @return The column, or <code>null</code> if none match the supplied ID.
     */
    public static ColumnFormat getColumn(String id) {
        for (int i = 0, imax = ALL_COLUMNS.size(); i < imax; ++i) {
            final ColumnFormat column = (ColumnFormat) ALL_COLUMNS.get(i);
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

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
     */
    public Class<?> getColumnClass(int column) {
        return ((ColumnFormat) columns.get(column)).getClazz();
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
     */
    public Comparator<?> getColumnComparator(int column) {
        return ((ColumnFormat) columns.get(column)).getComparator();
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
     */
    public int getColumnCount() {
        return columns.size();
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
     */
    public String getColumnName(int index) {
        return ((ColumnFormat) columns.get(index)).getName();
    }

    /**
     * Get a read only list of the columns currently displayed by this format.
     *
     * @return A <em>read-only</em> list containing the columns.
     */
    public List<ColumnFormat> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object, int)
     */
    public Object getColumnValue(Record record, int index) {
        return ((ColumnFormat) columns.get(index)).getValue(record);
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
     * @param model
     */
    void setModel(EventTableModel<Record> model) {
        this.model = model;
    }

}
