/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;
import raccoon.tibco.tibrv.junit.data.RvMsg;
import raccoon.tibco.tibrv.mock.api.RvMsgBase;
import raccoon.tibco.common.badhnati.xml.RvToXml;
import raccoon.tibco.common.badhnati.xml.XmlToRv;

/***
 * The conversion utility between a TibrvMsg and an XML String for rvsn00p
 * <p>
 * The XML String is rather verbose because it works with any kind of TIB/Rendezvous message, either a TibrvMsg or
 * a MTree or a MInstance.
 *
 * @author <a href="mailto:crouvrais@users.sourceforge.net">Cedric ROUVRAIS</a>
 * @version $Id$
 * @since Raccoon v0.5. Created 28 janv. 2003 at 20:45:22
 */
public class MarshalRvToStringRacoonImpl implements IMarshalRvToStringImpl {
    /*** The TIB/Rendezvous message to XML String converter */
    private RvToXml mRvToXml = new RvToXml();
    /*** The XML String to TIB/Rendezvous message to converter */
    private XmlToRv mXmlToRv = new XmlToRv();

    /***
     * Converts a TibrvMsg (Rv or Ae) into an xml string
     * @param pTibrvMsg the TIB/Rendezvous message
     * @param name
     * @return the XML String
     */
    public String rvmsgToString(TibrvMsg pTibrvMsg, String name) {
        try {
            RvMsg rvMsg = new RvMsg(pTibrvMsg);
            return mRvToXml.unmarshal(rvMsg);
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * Converts an XML String into a TibrvMsg.
     *
     * @param pXmlString the XML string
     * @return the TibrvMsg instance
     * @throws Exception if something goes wrong.
     */
    public TibrvMsg stringToRvmsg(String pXmlString) throws Exception {
        RvMsgBase rvMsgBase = mXmlToRv.marshal(pXmlString);
        rvMsgBase.sync();
        return new TibrvMsg((TibrvMsg) rvMsgBase);
    }

}
