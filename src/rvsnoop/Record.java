//:File:    Record.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
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

    private static final class RecordBinding extends TupleBinding {
        RecordBinding() {
            super();
        }
        public Object entryToObject(TupleInput input) {
            final byte[] magic = new byte[BIND_RECORD_MAGIC.length];
            input.readFast(magic);
            if (!Arrays.equals(BIND_RECORD_MAGIC, magic))
                throw new IllegalArgumentException("TupleInput does not contain a valid record.");
            final boolean connFlag = input.readBoolean();
            final String description = connFlag ? input.readString() : null;
            final String service = connFlag ? input.readString() : null;
            final String network = connFlag ? input.readString() : null;
            final String daemon = connFlag ? input.readString() : null;
            RvConnection connection = RvConnection.getConnection(service, network, daemon);
            if (connection == null) {
                connection = RvConnection.createConnection(service, network, daemon);
                connection.setDescription(description);
            }
            final long timestamp = input.readLong();
            final int length = input.readInt();
            final byte[] message = new byte[length];
            try {
                return new Record(connection, new TibrvMsg(message), timestamp);
            } catch (TibrvException e) {
                if (Logger.isErrorEnabled())
                    logger.error("Could not unmarshal message data.", e);
                return null;
            }
        }

        public void objectToEntry(Object object, TupleOutput output) {
            final Record record = (Record) object;
            final boolean connection = record.connection != null;
            output.writeFast(BIND_RECORD_MAGIC);
            output.writeBoolean(connection);
            if (connection) {
                output.writeString(record.connection.getDescription());
                output.writeString(record.connection.getService());
                output.writeString(record.connection.getNetwork());
                output.writeString(record.connection.getDaemon());
            }
            output.writeLong(record.timestamp);
            byte[] bytes = null;
            try {
                bytes = record.message.getAsBytes();
                output.writeInt(bytes.length);
                output.writeFast(bytes);
            } catch (TibrvException e) {
                output.writeInt(0);
                if (Logger.isErrorEnabled())
                    logger.error("Could not marshal message data.", e);
            }
        }
    }
    
    private static final Logger logger = Logger.getLogger(Record.class);
    
    private static long nextSequenceNumber = 0;

    /**
     * Magic number that represents a single record entry in a byte stream.
     * <p>
     * The format of a single record entry is
     * <pre>MAGIC_NUMBER CONNECTION? CONNECTION_INFO TIMESTAMP LENGTH MESSAGE</pre>
     * where connection is a boolean, if it is true then connection info is
     * stored as four null terminated strings representing description, service,
     * network, and daemon, respectively. If connection is false then connection
     * info is not stored.
     * <p>
     * Note that sequence number is <em>not</em> stored.
     */
    public static final byte[] BIND_RECORD_MAGIC;

    /**
     * Magic number that represents a set of record entries in a byte stream.
     * <p>
     * The format of a record set is <pre>MAGIC_NUMBER LENGTH RECORD*</pre>.
     */
    public static final byte[] BIND_RECORD_SET_MAGIC;
    
    public static final TupleBinding BINDING = new RecordBinding();

    private static final long serialVersionUID = 4866817496310259124L;

    static {
        try {
            BIND_RECORD_MAGIC = "RVMBSF02".getBytes("UTF-8");
            BIND_RECORD_SET_MAGIC = "RVMBSF01".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
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

    private final SubjectElement subject;

    private final long timestamp;
    
    private String trackingId;

    /**
     * Private constructor to allow setting a custom timestamp.
     * @param connection
     * @param message
     * @param timestamp
     */
    private Record(RvConnection connection, TibrvMsg message, long timestamp) {
        super();
        this.connection = connection;
        this.message = message;
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) (sequenceNumber * 17);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("[Record:sequenceNumber=").append(sequenceNumber);
        buffer.append(",timestamp=").append(timestamp).append("]");
        return buffer.toString();
    }

}
