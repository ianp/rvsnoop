//:File:    LogRecord.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

import java.io.Serializable;

import com.tibco.tibrv.TibrvMsg;

/**
 * A LogRecord encapsulates the details of your desired log request.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class LogRecord implements Serializable {

    private static final long serialVersionUID = 4866817496310259124L;

    private static long nextSequenceId = 0;

    protected MsgType _type;

    protected long _sequenceNumber;
    protected long _millis;

    protected StringBuffer _sSubject = new StringBuffer();
    protected TibrvMsg _message;
    protected StringBuffer _replySubject = new StringBuffer();
    protected StringBuffer _trackingID = new StringBuffer();

    public LogRecord(TibrvMsg message) {
        this._millis = System.currentTimeMillis();
        this._sequenceNumber = getNextSequenceId();
        this._type = MsgType.UNKNOWN;
        this._message = message;
    }

    /**
     * Get the type of this LogRecord.
     *
     * @return The MsgType of this record.
     * @see #setType(MsgType)
     * @see MsgType
     */
    public MsgType getType() {
        return (_type);
    }

    /**
     * Set the type of this LogRecord.
     *
     * @param type The MsgType for this record.
     * @see MsgType
     */
    public void setType(MsgType type) {
        _type = type;
    }

    public String getTrackingID() {
        return _trackingID.toString();
    }
    public StringBuffer getTrackingIDStringBuffer(){
        return _trackingID;
    }

    public void setTrackingID(String trackingID) {
        _trackingID.setLength(0);
        _trackingID.append(trackingID);
    }

    /**
     * Determines which <code>Type</code> levels will
     * be displayed
     *
     * @return true if the log level is Severe.
     */
    public boolean isSevereType() {
        boolean isSevere = false;

        if (MsgType.ERROR.equals(getType()) ||
                MsgType.ERROR.equals(getType())) {
            isSevere = true;
        }

        return isSevere;
    }


    /**
     * @return true if isSevereType() or hasThrown() returns true.
     */
    public boolean isFatal() {
        return isSevereType();
    }

    /**
     * Get the subject asscociated with this LogRecord.  For a more detailed
     * description of what a subject is see setSubject().
     *
     * @return The subject of this record.
     */
    public String getSubject() {
        return _sSubject.toString();
    }

    public StringBuffer getSubjectAsStringBuffer() {
        return _sSubject;
    }

    /**
     * Set the subject associated with this LogRecord. A subject represents
     * a hierarchical dot (".") separated namespace for messages.
     * The definition of a subject is application specific, but a common convention
     * is as follows:
     *
     * @param subject The subject for this record.
     */
    public void setSendSubject(String subject) {
        _sSubject.setLength(0);
        _sSubject.append(subject);
    }

    /**
     * Get the message asscociated with this LogRecord.
     *
     * @return The message of this record.
     * @see #setMessage(TibrvMsg)
     */
    public TibrvMsg getMessage() {
        return _message;
    }

    /**
     * Set the message associated with this LogRecord.
     *
     * @param message The message for this record.
     * @see #getMessage()
     */
    public void setMessage(TibrvMsg message) {
        if (message == null) throw new NullPointerException();
        _message = message;
    }

    /**
     * Get the sequence number associated with this LogRecord.  Sequence numbers
     * are generally assigned when a LogRecord is constructed.  Sequence numbers
     * start at 0 and increase with each newly constructed LogRocord.
     *
     * @return The sequence number of this record.
     * @see #setSequenceNumber(long)
     */
    public long getSequenceNumber() {
        return (_sequenceNumber);
    }

    /**
     * Set the sequence number assocsiated with this LogRecord.  A sequence number
     * will automatically be assigned to evey newly constructed LogRecord, however,
     * this method can override the value.
     *
     * @param number The sequence number.
     * @see #getSequenceNumber()
     */
    public void setSequenceNumber(long number) {
        _sequenceNumber = number;
    }

    /**
     * Get the event time of this record in milliseconds from 1970.
     * When a LogRecord is constructed the event time is set but may be
     * overridden by calling setMillis();
     *
     * @return The event time of this record in milliseconds from 1970.
     * @see #setMillis(long)
     */
    public long getMillis() {
        return _millis;
    }

    /**
     * Set the event time of this record.  When a LogRecord is constructed
     * the event time is set but may be overridden by calling this method.
     *
     * @param millis The time in milliseconds from 1970.
     * @see #getMillis()
     */
    public void setMillis(long millis) {
        _millis = millis;
    }

    /**
     * Return a String representation of this LogRecord.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("LogRecord: [");
        buf.append(_type);
        buf.append(",");
        buf.append(_message);
        buf.append("]");
        return (buf.toString());
    }

    /**
     * Set the replysubject
     *
     * @param replysub A string containing subject information.
     */
    public void setReplySubject(String replysub) {
        _replySubject.setLength(0);
        _replySubject.append(replysub);
    }

    /**
     * Resets that sequence number to zero.
     */
    public static synchronized void resetSequenceId() {
        nextSequenceId = 0;
    }

    protected static synchronized long getNextSequenceId() {
        return nextSequenceId++;
    }

}
