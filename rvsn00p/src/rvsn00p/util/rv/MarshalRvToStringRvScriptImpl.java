//:File:    MarshalRvToStringRvScriptImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.rvscript.tibrvXmlConvert;
import com.tibco.tibrv.TibrvMsg;

/**
 * Marshaller implementation that uses RvScript to do the work.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class MarshalRvToStringRvScriptImpl implements MarshalRvToString.Implementation {

    private final tibrvXmlConvert converter = new tibrvXmlConvert();

    MarshalRvToStringRvScriptImpl() {
        super();
    }
    
    public String getName() {
        return "RvScript";
    }

    public String marshal(String name, TibrvMsg message) {
        return converter.rvmsgToXml(message,name);
    }

    public TibrvMsg unmarshal(String string) {
        return converter.xmlToRvmsg(string);
    }

}
