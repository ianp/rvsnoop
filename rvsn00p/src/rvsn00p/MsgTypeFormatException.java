/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p;

/**
 * Thrown to indicate that the client has attempted to convert a string
 * to one the MsgType types, but the string does not have the appropriate
 * format.
 *
 * @author Michael J. Sikorsky<
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public class MsgTypeFormatException extends Exception {
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

    public MsgTypeFormatException(String message) {
        super(message);
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

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






