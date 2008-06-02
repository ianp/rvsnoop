/*
 * Class:     DataAccessor
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.TibrvUtils;

import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;
import com.tibco.tibrv.TibrvXml;

import rvsnoop.Record;

/**
 * An base for classes capable of extracting data from records.
 * <p>
 * Normally this will be a single value, such as the send subject, but in some
 * cases it may be multiple values, such as all field names.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class DataAccessor {

    static final class FieldContents extends DataAccessor {
        public static final String IDENTIFIER = "fieldContents";
        public FieldContents() {
            super(FIELD_CONTENTS, IDENTIFIER);
        }
        @Override
        public Iterator getDataElement(Record record) {
            return new FieldContentsIterator(TibrvUtils.depthFirstFieldIterator(record.getMessage()));
        }
    }

    private static final class FieldContentsIterator implements Iterator {
        private final Iterator iterator;
        FieldContentsIterator(Iterator iterator) {
            this.iterator = iterator;
        }
        public boolean hasNext() {
            return iterator.hasNext();
        }
        public Object next() {
            TibrvMsgField field = (TibrvMsgField) iterator.next();
            if (field.type == TibrvMsg.STRING) {
                return field.data;
            } else if (field.type == TibrvMsg.XML) {
                try {
                    return new String(((TibrvXml) field.data).getBytes(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Never happens, UTF-8 is always supported in a Java VM.
                }
            }
            return "";
        }
        public void remove() {
            iterator.remove();
        }
    }

    static final class FieldNames extends DataAccessor {
        public static final String IDENTIFIER = "fieldNames";
        public FieldNames() {
            super(FIELD_NAMES, IDENTIFIER);
        }
        @Override
        public Iterator getDataElement(Record record) {
            return new FieldNamesIterator(TibrvUtils.depthFirstFieldIterator(record.getMessage()));
        }
    }

    private static final class FieldNamesIterator implements Iterator {
        private final Iterator iterator;
        FieldNamesIterator(Iterator iterator) {
            this.iterator = iterator;
        }
        public boolean hasNext() {
            return iterator.hasNext();
        }
        public Object next() {
            return ((TibrvMsgField) iterator.next()).name;
        }
        public void remove() {
            iterator.remove();
        }
    }

    static final class ReplySubject extends DataAccessor {
        public static final String IDENTIFIER = "replySubject";
        private final SingleElementIterator iterator = new SingleElementIterator();
        public ReplySubject() {
            super(REPLY_SUBJECT, IDENTIFIER);
        }
        @Override
        public Iterator getDataElement(Record record) {
            iterator.element = record.getReplySubject();
            return iterator;
        }
    }

    static final class SendSubject extends DataAccessor {
        public static final String IDENTIFIER = "sendSubject";
        private final SingleElementIterator iterator = new SingleElementIterator();
        public SendSubject() {
            super(SEND_SUBJECT, IDENTIFIER);
        }
        @Override
        public Iterator getDataElement(Record record) {
            iterator.element = record.getSendSubject();
            return iterator;
        }
    }

    private static final class SingleElementIterator implements Iterator {
        Object element;
        public boolean hasNext() {
            return element != null;
        }
        public Object next() {
            if (element == null) throw new NoSuchElementException();
            Object next = element;
            element = null;
            return next;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static final class TrackingId extends DataAccessor {
        public static final String IDENTIFIER = "trackingId";
        private final SingleElementIterator iterator = new SingleElementIterator();
        public TrackingId() {
            super(TRACKING_ID, IDENTIFIER);
        }
        @Override
        public Iterator getDataElement(Record record) {
            iterator.element = record.getTrackingId();
            return iterator;
        }
    }

    static String FIELD_CONTENTS, FIELD_NAMES, REPLY_SUBJECT, SEND_SUBJECT, TRACKING_ID;

    static { NLSUtils.internationalize(DataAccessor.class); }

    private final String displayName;
    private final String identifier;

    protected DataAccessor(String displayName, String identifier) {
        Validate.notNull(displayName);
        Validate.notNull(identifier);
        this.displayName = displayName;
        this.identifier = identifier;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final DataAccessor other = (DataAccessor) obj;
        if (!identifier.equals(other.identifier)) { return false; }
        return true;
    }

    /**
     * Get the data elements from a record.
     *
     * @param record The record to extract data from.
     * @return The data elements.
     */
    public abstract Iterator getDataElement(Record record);

    /**
     * Get a string describing the data this class accesses which is suitable
     * for display in a UI.
     *
     * @return A human readable display string.
     */
    public final String getDisplayName() {
        return displayName;
    }

    /**
     * Get a string describing the data this class accesses which is fixed and
     * does not change with locale.
     *
     * @return A string intended for internal program use and storage.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 + identifier.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("identifier", identifier).toString();
    }

}
