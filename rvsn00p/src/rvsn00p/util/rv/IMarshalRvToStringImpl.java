//:File:    IMarshalRvToStringImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;

/**
 * Interface for  MarshalRvToString impementations
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 */
public interface IMarshalRvToStringImpl {

    /**
     *
     * @param rvMsg the Rendezvous message to convert to a string
     * @param name a name for the message
     * @return string containing the converted Rendezvous message
     */
    public String rvmsgToString(TibrvMsg rvMsg, String name);

    /**
     *
     * @param rvMsgString the string containing the message
     * @return the converted message
     * @throws Exception
     */
    public TibrvMsg stringToRvmsg(String rvMsgString) throws Exception;

}
