/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;
import com.reuters.msgtest.XMLConverter ;


/***
 * Rvtest plugin - string serialiser from TibrvMsg to the rvtest XML format for rvsn00p
 * <p>
 * The XML String is rather verbose because it works with any kind of TIB/Rendezvous message, either a TibrvMsg or
 * a MTree or a MInstance.
 *
 * @author <a href="mailto:lundberg@home.se">Oran Lundberg</a>
 * @version $Id$
 * @since Rvsn00p v1.2.6
 */
public class MarshalRvToStringRvTestImpl implements IMarshalRvToStringImpl {
    /*** The TIB/Rendezvous message to XMLConverter */
    private XMLConverter xc = new XMLConverter();
    /***
     * Converts a TibrvMsg into an xml string
     * @param rvMsg the TIB/Rendezvous message
     * @param name
     * @return the XML String
     */
    public String rvmsgToString(TibrvMsg rvMsg, String name) {
        try {
            StringBuffer sb;
            sb =  xc.createXML(rvMsg);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * Converts an XML String into a TibrvMsg.
     *
     * @throws Exception "Not implemented"
     */
    public TibrvMsg stringToRvmsg(String pXmlString) throws Exception {
        throw new Exception("Not implemented");
    }

}
