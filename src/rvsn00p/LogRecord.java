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
 * Contributed by ThoughtWorks Inc.
 */
public class LogRecord implements java.io.Serializable {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
    protected static long _seqCount = 0;

    protected MsgType _type;

    protected long _sequenceNumber;
    protected long _millis;

    protected StringBuffer _sSubject = new StringBuffer();
    protected StringBuffer _message  = new StringBuffer();
    protected StringBuffer _replySubject = new StringBuffer();
    protected StringBuffer _trackingID = new StringBuffer();

    private static final int MAX_FREE_POOL_SIZE = 2000;  // Free pool capacity.

    // Pool owned by class.
    private static LogRecord[] _freeStack = new LogRecord[MAX_FREE_POOL_SIZE];
    private static int _countFree = 0;
    private static long lastAllocationTime = System.currentTimeMillis()-3000 ;

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------

    protected LogRecord() {
        super();
    }

    public static synchronized LogRecord getInstance() {

        LogRecord result;
        // Check if the pool is empty.

        if (_countFree == 0) {

            long timeSinceLastAlloc =  System.currentTimeMillis() - lastAllocationTime ;

            int noToAlloc = 0;
            if( timeSinceLastAlloc < 200 ) {
                noToAlloc =  MAX_FREE_POOL_SIZE;
            } else if( timeSinceLastAlloc < 1000 ) {
                noToAlloc =  MAX_FREE_POOL_SIZE/2;
            } else if( timeSinceLastAlloc < 3000 ) {
                noToAlloc =  MAX_FREE_POOL_SIZE/4;
            } else {
                noToAlloc = 100;
            }

            // Fill the pool if so.
            for(int i = 0; i < noToAlloc; ++i ){
              freeInstance(new LogRecord());
            }

            lastAllocationTime = System.currentTimeMillis();
        }

        // Remove object from end of free pool.
        result = _freeStack[--_countFree];

        // Initialize the object to the specified state.
        result._millis = System.currentTimeMillis();
        result._type = MsgType.UNKNOWN;
        result._sequenceNumber = getNextId();
        return result;
    }


    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    public static synchronized void freeInstance(LogRecord lr) {
        if (_countFree < MAX_FREE_POOL_SIZE) {
            _freeStack[_countFree++] = lr;
        }
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
     * @see #setMessage(String)
     */
    public String getMessage() {
        return _message.toString();
    }

    public StringBuffer getMessageAsStringBuffer() {
        return _message;
    }

    /**
     * Set the message associated with this LogRecord.
     *
     * @param message The message for this record.
     * @see #getMessage()
     */
    public void setMessage(String message) {
        _message.setLength(0);
        _message.append(message);
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



