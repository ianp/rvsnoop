/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */

package rvsn00p;


/**
 * A <code>RvSnooperLogRecord</code> encapsulates
 * the details of your rv messages in a format usable
 * by the <code>RvSnooperGUI</code>.
 *
 * @author Örjan Lundberg
 *
 * Based on Logfactor5 By
 *
 * @author Brent Sprecher
 */

// Contributed by ThoughtWorks Inc.

public class RvSnooperLogRecord extends LogRecord {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------

    /**
     * Constructs an instance of a <code>RvSnooperLogRecord</code>.
     */
    public RvSnooperLogRecord() {
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------
    /**
     * Determines which <code>Priority</code> levels will
     * be displayed in colored font when the <code>LogMonitorAppender</code>
     * renders this log message. By default, messages will be colored
     * red if they are of <code>Priority</code> ERROR or FATAL.
     *
     * @return true if the log level is ERROR.
     */
    public boolean isSevereType() {
        boolean isSevere = false;

        if (MsgType.ERROR.equals(getType()) ||
                MsgType.ERROR.equals(getType())) {
            isSevere = true;
        }

        return isSevere;
    }



    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

}



