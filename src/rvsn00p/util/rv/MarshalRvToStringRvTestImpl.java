//:File:    MarshalRvToStringRvTestImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.reuters.msgtest.XMLConverter;
import com.tibco.tibrv.TibrvMsg;

/***
 * Rvtest plugin - string serialiser from TibrvMsg to the rvtest XML format for rvsn00p
 * <p>
 * The XML String is rather verbose because it works with any kind of TIB/Rendezvous message, either a TibrvMsg or
 * a MTree or a MInstance.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.2.6
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
