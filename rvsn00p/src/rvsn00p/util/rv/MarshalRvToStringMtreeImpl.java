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
import  com.tibco.sdk.MTree;

/**
 *   MarshalRvToString impementation with Mtree
 * @author Örjan Lundberg
 */
class MarshalRvToStringMtreeImpl implements IMarshalRvToStringImpl {

    public String rvmsgToString(TibrvMsg msg){

        MTree a = new MTree("");
        a.use_tibrvMsg(msg);
        return a.toString();

    }

    public TibrvMsg stringToRvmsg(String s) throws Exception{
          throw new Exception("Not implemented");
    }

}
