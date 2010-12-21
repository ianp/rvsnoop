// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import rvsnoop.Record;
import rvsnoop.RecordTypes;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

/**
 * A format that describes the contents of a record ledger.
 */
public final class RecordLedgerFormat implements AdvancedTableFormat<Record> {

    private final ImmutableList<ColumnFormat> allColumns;

    private final List<ColumnFormat> columns;

    private EventTableModel<Record> model;

    private final Preferences preferences;

    @Inject
    public RecordLedgerFormat(ApplicationContext context, final RecordTypes recordTypes, Preferences preferences) {
        this.preferences = preferences;
        ResourceMap resourceMap = context.getResourceMap(getClass());
        allColumns = ImmutableList.of(
                new ColumnFormat("connection", resourceMap, String.class) {
                    @Override public Object getValue(Record record) {
                        return record.getConnection() != null ? record.getConnection().getDescription() : "";
                    }
                },
                new ColumnFormat("timestamp", resourceMap, Date.class) {
                    @Override public Object getValue(Record record) {
                        return new Date(record.getTimestamp());
                    }
                },
                new ColumnFormat("sequence", resourceMap, Long.class) {
                    @Override public Object getValue(Record record) {
                        return Long.toString(record.getSequenceNumber());
                    }
                },
                new ColumnFormat("type", resourceMap, String.class) {
                    @Override public Object getValue(Record record) {
                        return recordTypes.getFirstMatchingType(record).getName();
                    }
                },
                new ColumnFormat("subject", resourceMap, String.class) {
                    @Override public Object getValue(Record record) {
                        return record.getSendSubject();
                    }
                },
                new ColumnFormat("size", resourceMap, Integer.class) {
                    @Override public Object getValue(Record record) {
                        return record.getSizeInBytes();
                    }
                },
                new ColumnFormat("tracking", resourceMap, String.class) {
                    @Override public Object getValue(Record record) {
                        return record.getTrackingId();
                    }
                }
        );
        columns = new ArrayList<ColumnFormat>(allColumns);
        for (ColumnFormat column : allColumns) {
            if (!preferences.getBoolean(column.getPreferenceKey(), true)) {
                columns.remove(column);
            }
        }
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
        preferences.putBoolean(column.getPreferenceKey(), true);
    }

    /**
     * Is a given column visible in this format.
     *
     * @param column The column to check.
     * @return {@code true} if the column is currently visible, {@code false} otherwise.
     */
    public boolean contains(ColumnFormat column) {
        return columns.contains(column);
    }

    public ImmutableList<ColumnFormat> getAllColumns() {
        return allColumns;
    }

    /**
     * Gets a column format by it's ID.
     *
     * @param id The ID of the column.
     * @return The column, or {@code null} if none match the supplied ID.
     */
    public ColumnFormat getColumn(String id) {
        for (ColumnFormat column : allColumns) {
            if (column.getIdentifier().equals(id)) { return column; }
        }
        return null;
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
        preferences.putBoolean(column.getPreferenceKey(), false);
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
        for (ColumnFormat column : allColumns) {
            preferences.putBoolean(column.getPreferenceKey(), columns.contains(column));
        }
    }

    /**
     * Set the model that will display this format.
     * <p>
     * If a model has been set it will be automatically notified whenever a
     * column is added to or removed from this format.
     *
     * @param model the model to set.
     */
    public void setModel(EventTableModel<Record> model) {
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
     */
    public abstract static class ColumnFormat {
        private final Class<?> clazz;
        private final Comparator<?> comparator;
        private final String identifier;
        private final String name;
        private final String preferenceKey;
        ColumnFormat(String id, ResourceMap resourceMap, Class<?> clazz) {
            this(id, resourceMap, clazz, new ComparableComparator());
        }
        ColumnFormat(String id, ResourceMap resourceMap, Class<?> clazz, Comparator<?> comparator) {
            this.clazz = clazz;
            this.comparator = comparator;
            this.identifier = id;
            this.name = resourceMap.getString("columnTitle." + id);
            this.preferenceKey = "TableColumnVisible[" +id + "]";
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
        public final String getPreferenceKey() {
            return preferenceKey;
        }
        public final String getName() {
            return name;
        }
        public abstract Object getValue(Record record);
    }

}
