//:File:    Record.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

/**
 * A record encapsulates the details of a single Rendezvous message.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class Record {

    private static final Logger logger = Logger.getLogger(Record.class);

    private static long nextSequenceNumber = 0;

    /**
     * Resets that sequence number to zero.
     */
    public static void resetSequence() {
        synchronized (Record.class) {
            nextSequenceNumber = 0;
        }
    }

    private final RvConnection connection;

    private final TibrvMsg message;

    private final long sequenceNumber;

    private final int sizeInBytes;

    private final SubjectElement subject;

    private final long timestamp;

    private String trackingId;

    /**
     * Private constructor to allow setting a custom timestamp.
     * @param connection
     * @param message
     * @param timestamp
     */
    Record(RvConnection connection, TibrvMsg message, long timestamp) {
        super();
        this.connection = connection;
        this.message = message;
        int sizeInBytes = 0;
        try {
            sizeInBytes = message.getAsBytes().length;
        } catch (TibrvException e) {
            if (Logger.isWarnEnabled())
                logger.warn("Unable to extract bytes from message.", e);
        }
        this.sizeInBytes = sizeInBytes;
        this.subject = SubjectHierarchy.INSTANCE.getSubjectElement(message.getSendSubject());
        this.timestamp = timestamp;
        synchronized (Record.class) {
            sequenceNumber = nextSequenceNumber++;
        }
    }

    public Record(RvConnection connection, TibrvMsg message) {
        this(connection, message, System.currentTimeMillis());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Record
            && sequenceNumber == ((Record) obj).sequenceNumber;
    }

    /**
     * Get the connection upon which the message in this record was received.
     *
     * @return The connection.
     */
    public RvConnection getConnection() {
        return connection;
    }

    /**
     * Get the message that this record represents.
     *
     * @return The rendezvous message.
     */
    public TibrvMsg getMessage() {
        return message;
    }

    /**
     * Get the reply subject of the message in this record.
     * <p>
     * If there is no reply subject for the message this method return the empty
     * string.
     *
     * @return The reply subject.
     */
    public String getReplySubject() {
        final String rs = message.getReplySubject();
        return rs != null ? rs : "";
    }

    /**
     * Get the send subject of the message in this record.
     * <p>
     * If there is no send subject for the message this method returns the
     * string ‘<code>[No Subject!]</code>’. A message may have no send
     * subject it was reconstructed from a byte array, for example as the result
     * of a copy and paste operation or loading the message from a file.
     *
     * @return The send subject.
     */
    public String getSendSubject() {
        final String ss =  message.getSendSubject();
        return ss != null ? ss : subject.getElementName();
    }

    /**
     * Get the record sequence number.
     * <p>
     * Sequence numbers are assigned on construction and are unique.
     *
     * @return The sequence number of this record.
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Get the size of the message represented by this record, in bytes.
     *
     * @return
     */
    public int getSizeInBytes() {
        return sizeInBytes;
    }

    /**
     * Gets the subject element representing the subject that this record was sent to.
     * @return The subject element holding this record.
     */
    public SubjectElement getSubject() {
        return subject;
    }

    /**
     * Get the event time of this record in milliseconds since 1970.
     * <p>
     * This timestamp is automatically assigned at object creation time.
     *
     * @return The event time of this record in milliseconds since 1970.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Extracts an Active Enterprise style tracking ID from a message.
     *
     * @return A string containing tracking ID, or the empty string if no ID was found.
     */
    public String getTrackingId() {
        if (trackingId == null) {
            try {
                final TibrvMsgField field = message.getField("^tracking^");
                trackingId = (String) ((TibrvMsg) field.data).get("^id^");
            } catch (NullPointerException e) {
                trackingId = "";
            } catch (TibrvException e) {
                trackingId = "";
                logger.error("Unable to determine tracking ID.", e);
            }
        }
        return trackingId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) (17 + sequenceNumber * 37);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
            .append("sequenceNumber", sequenceNumber)
            .append("timestamp", timestamp)
            .append("connection", connection != null ? connection.getDescription() : "null")
            .append("subject", subject.getUserObjectPath()).toString();
    }

}
