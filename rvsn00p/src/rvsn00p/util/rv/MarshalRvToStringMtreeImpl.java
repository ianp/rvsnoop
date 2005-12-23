//:File:    MarshalRvToStringMtreeImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.sdk.MTree;
import com.tibco.tibrv.TibrvMsg;

/**
 * Marshaller implementation that uses SDK MTree's to do the work.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
class MarshalRvToStringMtreeImpl implements MarshalRvToString.Implementation {

    MarshalRvToStringMtreeImpl() {
        // Just a sanity check for the classpath.
        new MTree("garbage");
    }
    
    public String getName() {
        return "MTree";
    }

    public String marshal(String name, TibrvMsg message) {
        MTree mtree = new MTree(name);
        mtree.use_tibrvMsg(message);
        return mtree.toString();

    }

    public TibrvMsg unmarshal(String string) {
        throw new UnsupportedOperationException();
    }

}
