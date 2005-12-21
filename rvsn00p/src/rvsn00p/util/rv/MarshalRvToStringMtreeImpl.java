//:File:    MarshalRvToStringMtreeImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.sdk.MTree;
import com.tibco.tibrv.TibrvMsg;

/**
 * MarshalRvToString impementation with Mtree
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
class MarshalRvToStringMtreeImpl implements IMarshalRvToStringImpl {
    public MarshalRvToStringMtreeImpl() {
        // check that the class exists in the classpath
        new MTree("dummy");
    }

    public String rvmsgToString(TibrvMsg msg,String name){

        MTree a = new MTree(name);
        a.use_tibrvMsg(msg);
        return a.toString();

    }

    public TibrvMsg stringToRvmsg(String s) throws Exception{
          throw new Exception("Not implemented");
    }

}
