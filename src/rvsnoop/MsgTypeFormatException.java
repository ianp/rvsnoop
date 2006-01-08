//:File:    MsgTypeFormatException.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

/**
 * Thrown to indicate that the client has attempted to convert a string
 * to one the MsgType types, but the string does not have the appropriate
 * format.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class MsgTypeFormatException extends Exception {

    private static final long serialVersionUID = 3460311286399467928L;

    public MsgTypeFormatException(String message) {
        super(message);
    }

}
