//:File:    MarshalRvToStringRvScriptImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.rvscript.tibrvXmlConvert;
import com.tibco.tibrv.TibrvMsg;

/**
 * MarshalRvToString impementation for TibrvMsg
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
class MarshalRvToStringRvScriptImpl implements IMarshalRvToStringImpl {

    static tibrvXmlConvert _tx = new tibrvXmlConvert();
    public String rvmsgToString(TibrvMsg msg, String name){
        return _tx.rvmsgToXml(msg,name);
    }

    public TibrvMsg stringToRvmsg(String s) throws Exception{
        return _tx.xmlToRvmsg(s);
    }

}
