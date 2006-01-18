//:File:    MessageLedgerFormat.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.table.TableColumn;

import ca.odell.glazedlists.gui.TableFormat;


/**
 * A format that describes the contents of the message ledger.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
// Class provides a static instance instead of a factory method.
// @PMD:REVIEWED:MissingStaticMethodInNonInstantiatableClass: by ianp on 1/17/06 6:30 PM
public final class MessageLedgerFormat implements TableFormat {

    public static abstract class ValueColumn extends TableColumn {
        private final String name;
        ValueColumn(String name) {
            this.name = name;
        }
        public final String getName() {
            return name;
        }
        public abstract Object getValue(Record record);
    }
    
    public static final ValueColumn CONNECTION = new ValueColumn("Connection") {
        private static final long serialVersionUID = -4938884664732119140L;
        public Object getValue(Record record) {
            final RvConnection connection = record.getConnection();
            return connection != null ? connection.getDescription() : "";
        }
    };
    
    public static final ValueColumn MESSAGE = new ValueColumn("Message") {
        private static final long serialVersionUID = 3526646591996520301L;
        public Object getValue(Record record) {
            return record.getMessage();
        }
    };
    
    public static final ValueColumn SEQUENCE_NO = new ValueColumn("Seq. No.") {
        private static final long serialVersionUID = -5762735421370128862L;
        public Object getValue(Record record) {
            return Long.toString(record.getSequenceNumber());
        }
    };
    
    public static final ValueColumn SUBJECT = new ValueColumn("Subject") {
        private static final long serialVersionUID = 7415054603888398693L;
        public Object getValue(Record record) {
            return record.getSendSubject();
        }
    };
    
    public static final ValueColumn TIMESTAMP = new ValueColumn("Timestamp") {
        private static final long serialVersionUID = -3858078006527711756L;
        private final Date date = new Date();
        public Object getValue(Record record) {
            date.setTime(record.getTimestamp());
            return StringUtils.format(date);
        }
    };

    public static final ValueColumn TRACKING_ID = new ValueColumn("Tracking ID") {
        private static final long serialVersionUID = -3033175036104293820L;
        public Object getValue(Record record) {
            return record.getTrackingId();
        }
    };
    
    public static final ValueColumn TYPE = new ValueColumn("Type") {
        private static final long serialVersionUID = 6353909616946303068L;
        public Object getValue(Record record) {
            return RecordType.getFirstMatchingType(record).getName();
        }
    };

    public static final MessageLedgerFormat INSTANCE = new MessageLedgerFormat();

    private final List allColumns = Arrays.asList(new ValueColumn[] {
        CONNECTION, TIMESTAMP, SEQUENCE_NO, TYPE, SUBJECT, TRACKING_ID, MESSAGE
    });
    
    private final List columns = new ArrayList(allColumns.size());
    
    private MessageLedgerFormat() {
        super();
        showAllColumns();
    }
    
    public List getAllColumns() {
        return Collections.unmodifiableList(allColumns);
    }

    public ValueColumn getColumn(String name) {
        for (int i = 0, imax = allColumns.size(); i < imax; ++i) {
            final ValueColumn column = (ValueColumn) allColumns.get(i);
            if (column.name.equals(name))
                return column;
        }
        return null;
    }

    public int getColumnCount() {
        return columns.size();
    }

    public String getColumnName(int index) {
        return ((ValueColumn) columns.get(index)).getName();
    }

    public List getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public Object getColumnValue(Object record, int index) {
        return ((ValueColumn) columns.get(index)).getValue((Record) record);
    }
    
    public void hideColumn(String name) {
        hideColumn(getColumn(name));
    }
    
    public void hideColumn(ValueColumn column) {
        columns.remove(column);
    }
        
    public void showAllColumns() {
        columns.clear();
        columns.addAll(allColumns);
    }
    
    public void showColumn(String name) {
        showColumn(getColumn(name));
    }
    
    public void showColumn(ValueColumn column) {
        if (!columns.contains(column)) columns.add(column);
    }
}
