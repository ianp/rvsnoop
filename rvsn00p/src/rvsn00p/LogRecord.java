/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p;

import rvsn00p.util.DateFormatManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * LogRecord.  A LogRecord encapsulates the details of your desired log
 * request.
 * @author Örjan Lundberg
 *
 * Based on Logfactor5 By
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public abstract class LogRecord implements java.io.Serializable {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
    protected static long _seqCount = 0;

    protected MsgType _type;
    protected String _message;
    protected long _sequenceNumber;
    protected long _millis;
    protected String _sSubject;

    protected String _thrownStackTrace;
    protected Throwable _thrown;
    protected String _replySubject;



    protected String _trackingID;

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------

    public LogRecord() {
        super();

        _millis = System.currentTimeMillis();
        _sSubject = "Debug";
        _message = "";
        _type = MsgType.UNKNOWN;
        _sequenceNumber = getNextId();
        _replySubject = "";
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

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
        return _trackingID;
    }

    public void setTrackingID(String _trackingID) {
        this._trackingID = _trackingID;
    }

    /**
     * Abstract method. Must be overridden to indicate what log type
     * to show in red.
     */
    public abstract boolean isSevereType();



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
        return (_sSubject);
    }

    /**
     * Set the subject associated with this LogRecord. A subject represents
     * a hierarchical dot (".") separated namespace for messages.
     * The definition of a subject is application specific, but a common convention
     * is as follows:
     *
     * <p>
     * When logging messages
     * for a particluar class you can use its class name:
     * com.thoughtworks.framework.servlet.ServletServiceBroker.<br><br>
     * Futhermore, to log a message for a particular method in a class
     * add the method name:
     * com.thoughtworks.framework.servlet.ServletServiceBroker.init().
     * </p>
     *
     * @param subject The subject for this record.
     * @see #getSubject()
     */
    public void setSendSubject(String subject) {
        _sSubject = subject;
    }

    /**
     * Get the message asscociated with this LogRecord.
     *
     * @return The message of this record.
     * @see #setMessage(String)
     */
    public String getMessage() {
        return (_message);
    }

    /**
     * Set the message associated with this LogRecord.
     *
     * @param message The message for this record.
     * @see #getMessage()
     */
    public void setMessage(String message) {
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
     * Get the stack trace in a String-based format for the associated Throwable
     * of this LogRecord.  The stack trace in a String-based format is set
     * when the setThrown(Throwable) method is called.
     *
     * <p>
     * Why do we need this method considering that we
     * have the getThrown() and setThrown() methods?
     * A Throwable object may not be serializable, however, a String representation
     * of it is.  Users of LogRecords should generally call this method over
     * getThrown() for the reasons of serialization.
     * </p>
     *
     * @return The Stack Trace for the asscoiated Throwable of this LogRecord.
     * @see #setThrown(Throwable)
     * @see #getThrown()
     */
    public String getThrownStackTrace() {
        return (_thrownStackTrace);
    }

    /**
     * Set the ThrownStackTrace for the log record.
     *
     * @param trace A String to associate with this LogRecord
     * @see #getThrownStackTrace()
     */
    public void setThrownStackTrace(String trace) {
        _thrownStackTrace = trace;
    }

    /**
     * Get the Throwable associated with this LogRecord.
     *
     * @return The MsgType of this record.
     * @see #setThrown(Throwable)
     * @see #getThrownStackTrace()
     */
    public Throwable getThrown() {
        return (_thrown);
    }

    /**
     * Set the Throwable associated with this LogRecord.  When this method
     * is called, the stack trace in a String-based format is made
     * available via the getThrownStackTrace() method.
     *
     * @param thrown A Throwable to associate with this LogRecord.
     * @see #getThrown()
     * @see #getThrownStackTrace()
     */
    public void setThrown(Throwable thrown) {
        if (thrown == null) {
            return;
        }
        _thrown = thrown;
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        thrown.printStackTrace(out);
        out.flush();
        _thrownStackTrace = sw.toString();
        try {
            out.close();
            sw.close();
        }
        catch (IOException e) {
            // Do nothing, this should not happen as it is StringWriter.
        }
        out = null;
        sw = null;
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
     * Get the location in code where this LogRecord originated.
     *
     * @return The string containing the location information.
     */
    public String getLocation() {
        return _replySubject;
    }

    /**
     * Set the replysubject
     *
     * @param replysub A string containing location information.
     */
    public void setReplySubject(String replysub) {
        _replySubject = replysub;
    }

    /**
     * Resets that sequence number to 0.
     *
     */
    public static synchronized void resetSequenceNumber() {
        _seqCount = 0;
    }

    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    protected static synchronized long getNextId() {
        ++_seqCount;
        return _seqCount;
    }
    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Type Classes or Interfaces:
    //--------------------------------------------------------------------------

}



