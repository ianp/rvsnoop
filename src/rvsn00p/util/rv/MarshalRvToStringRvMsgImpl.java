//:File:    MarshalRvToStringRvMsgImpl.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;

/**
 * MarshalRvToString impementation with RvMsg
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 */
class MarshalRvToStringRvMsgImpl implements IMarshalRvToStringImpl {
    public MarshalRvToStringRvMsgImpl() {
        // check that the class exists in the classpath
        new TibrvMsg();
    }

    public String rvmsgToString(TibrvMsg msg,String name){

        return msg.toString();

    }

    public TibrvMsg stringToRvmsg(String s) throws Exception{
          throw new Exception("Not implemented");
    }

}
