/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */

package rvsn00p.util.rv;
import  com.tibco.tibrv.TibrvMsg;
import  com.tibco.rvscript.tibrvXmlConvert;

/**
 *  MarshalRvToString impementation for TibrvMsg
 *
 * @author Örjan Lundberg
 *
 */
class MarshalRvToStringRvScriptImpl implements IMarshalRvToStringImpl {

    static tibrvXmlConvert _tx = new tibrvXmlConvert();
    public String rvmsgToString(TibrvMsg msg){
        return _tx.rvmsgToXml(msg);
    }

    public TibrvMsg stringToRvmsg(String s) throws Exception{
        return _tx.xmlToRvmsg(s);
    }

}
