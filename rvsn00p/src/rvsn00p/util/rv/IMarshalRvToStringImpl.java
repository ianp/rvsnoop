/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import  com.tibco.tibrv.TibrvMsg;
import  com.tibco.tibrv.TibrvCmMsg;

/**
 * Interface for  MarshalRvToString impementations
 * @author <a href="mailto:lundberg@home.se">Orjan Lundberg</a>
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
