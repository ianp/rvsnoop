// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop;

import com.google.common.base.Objects;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;
import org.rvsnoop.Logger;

/**
 * A record encapsulates the details of a single Rendezvous message.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 */
public final class Record {

    private static final Logger logger = Logger.getLogger();

    private static long nextSequenceNumber;

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
     * Constructor that allows setting a custom timestamp and subjects.
     *
     * @param connection The connection the message was received on.
     * @param message The message.
     * @param send The send subject of the message.
     * @param reply The reply subject of the message.
     * @param timestamp The time the message was received.
     */
    public Record(RvConnection connection, TibrvMsg message, String send, String reply, long timestamp) {
        super();
        this.connection = connection;
        this.message = message;
        try {
            if (send != null && send.length() > 0) message.setSendSubject(send);
            if (reply != null && reply.length() > 0) message.setReplySubject(reply);
        } catch (TibrvException e) {
            logger.error(e, "Could not set subject on message.");
        }
        int sizeInBytes = 0;
        try {
            sizeInBytes = message.getAsBytes().length;
        } catch (TibrvException e) {
            logger.warn(e, "Unable to extract bytes from message.");
        }
        this.sizeInBytes = sizeInBytes;
        this.subject = SubjectHierarchy.INSTANCE.getSubjectElement(message.getSendSubject());
        this.timestamp = timestamp;
        synchronized (Record.class) {
            sequenceNumber = nextSequenceNumber++;
        }
    }

    /**
     * Constructor that allows setting a custom timestamp.
     *
     * @param connection The connection the message was received on.
     * @param message The message.
     * @param timestamp The time the message was received.
     */
    public Record(RvConnection connection, TibrvMsg message, long timestamp) {
        this(connection, message, null, null, timestamp);
    }

    /**
     * Constructor.
     *
     * @param connection The connection the message was received on.
     * @param message The message.
     */
    public Record(RvConnection connection, TibrvMsg message) {
        this(connection, message, System.currentTimeMillis());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Record && sequenceNumber == ((Record) obj).sequenceNumber;
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
     * @return The size of the record, in bytes.
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
                logger.error(e, "Unable to determine tracking ID.");
            }
        }
        return trackingId;
    }

    @Override
    public int hashCode() {
        return (int) (17 + sequenceNumber * 37);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("sequenceNumber", sequenceNumber)
                .add("timestamp", timestamp)
                .add("connection", connection != null ? connection.getDescription() : "null")
                .add("subject", subject.getUserObjectPath()).toString();
    }

}
