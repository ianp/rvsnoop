//:File:    Record.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.io.Serializable;

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
public final class Record implements Serializable {

    private static final Logger logger = Logger.getLogger(Record.class);
    
    private static long nextSequenceNumber = 0;

    private static final long serialVersionUID = 4866817496310259124L;

    /**
     * Resets that sequence number to zero.
     */
    public static void resetSequence() {
        synchronized (Record.class) {
            nextSequenceNumber = 0;
        }
    }
    
    private final String connection;

    private final TibrvMsg message;

    private final long sequenceNumber;

    private final SubjectElement subject;
    
    private final long timestamp = System.currentTimeMillis();
    
    private String trackingId;

    private MsgType type = MsgType.UNKNOWN;

    public Record(String connection, TibrvMsg message, SubjectElement subject) {
        super();
        this.connection = connection;
        this.message = message;
        this.subject = subject;
        synchronized (Record.class) {
            sequenceNumber = nextSequenceNumber++;
        }
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
     * Get the name of the connection upon which the message in this record waqs received.
     * 
     * @return The name of the connection.
     */
    public String getConnection() {
        return connection;
    }
    
    /**
     * Get the string encoding used by this message.
     * 
     * @return The string encoding.
     */
    public String getEncoding() {
        return message.getMsgStringEncoding();
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
     * Gets the subject element representing the subject that this record was sent to.
     * @return
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
     * @param message The message to extract the tracking ID from.
     * @return A string containing tracking ID, or the empty string if no ID was found.
     * @throws TibrvException
     */
    public String getTrackingId() {
        if (trackingId == null)
            try {
                final TibrvMsgField field = message.getField("^tracking^");
                trackingId = (String) ((TibrvMsg) field.data).get("^id^");
            } catch (NullPointerException e) {
                trackingId = "";
            } catch (TibrvException e) {
                trackingId = "";
                logger.error("Unable to determine tracking ID.", e);
            }
        return trackingId;
    }

    /**
     * Get the type of this record.
     *
     * @return The type of this record.
     */
    public MsgType getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) (sequenceNumber * 17);
    }

    /**
     * Set the type of this record.
     *
     * @param type The type to set.
     */
    public void setType(MsgType type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("[Record:sequenceNumber=").append(sequenceNumber);
        buffer.append(",type=").append(type);
        buffer.append(",timestamp=").append(timestamp).append("]");
        return buffer.toString();
    }

}
