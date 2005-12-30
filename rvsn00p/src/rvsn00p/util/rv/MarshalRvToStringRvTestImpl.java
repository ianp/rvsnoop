//:File:    MarshalRvToStringRvTestImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.reuters.msgtest.MsgTestException;
import com.reuters.msgtest.XMLConverter;
import com.tibco.tibrv.TibrvMsg;

/**
 * Marshaller implementation that uses RvTest to do the work.
 * <p>
 * The XML String is rather verbose because it works with either plain
 * Rendezvous or SDK-style Active enterprise messages.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.2.6
 */
public class MarshalRvToStringRvTestImpl implements MarshalRvToString.Implementation {

    private final XMLConverter converter = new XMLConverter();
    
    MarshalRvToStringRvTestImpl() {
        super();
    }
    
    public String getName() {
        return "RvTest";
    }
    
    public String marshal(String name, TibrvMsg message) {
        try {
            return converter.createXML(message).toString();
        } catch (MsgTestException e) {
            System.err.println("Error: unable to marshal message: " + e.getMessage());
            return "";
        }
    }

    public TibrvMsg unmarshal(String string) {
        throw new UnsupportedOperationException();
    }

}
