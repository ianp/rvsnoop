//:File:    MarshalRvToStringRvMsgImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;

/**
 * Marshaller implementation that just calls <code>toString()</code> to do the work.
 * <p>
 * This implementation does not support unmarshalling strings to messages.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$ $Date$
 */
final class MarshalRvToStringRvMsgImpl extends MarshalRvToString.Implementation {

    MarshalRvToStringRvMsgImpl() {
        super("TibrvMsg");
    }

    public String marshal(String name, TibrvMsg message) {
        return message.toString();
    }

}
