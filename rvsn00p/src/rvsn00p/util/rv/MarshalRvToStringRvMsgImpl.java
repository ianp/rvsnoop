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
 *  MarshalRvToString impementation with RvMsg
 * @author Örjan Lundberg
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
