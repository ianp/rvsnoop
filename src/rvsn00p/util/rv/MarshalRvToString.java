/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;


/**
 * MarshalRvToString state class
 * @author �rjan Lundberg
 */
public class MarshalRvToString {

    static IMarshalRvToStringImpl _impl = null;
    static String _sImplementationUsed = null;

    // selects the "best" possible implementation
    static {

        boolean bHasException = true;
        Error lastError = new Error("");


        if (bHasException == true) {
            try {

                _impl = new MarshalRvToStringRacoonImpl();
                _sImplementationUsed = "Raccoon";
                bHasException = false;
            } catch (Error ex) {
                bHasException = true;
                lastError = ex;
            }

        }


        if (bHasException == true) {
            try {

                _impl = new MarshalRvToStringRvScriptImpl();
                _sImplementationUsed = "Rvscript";
                bHasException = false;
            } catch (Error ex) {
                bHasException = true;
                lastError = ex;
            }

        }

        if (bHasException == true) {
            try {

                _impl = new MarshalRvToStringMtreeImpl();
                bHasException = false;
                _sImplementationUsed = "Mtree";
            } catch (Error ex) {
                bHasException = true;
                lastError = ex;
            }
        }

        if (bHasException == true) {
            _impl = new MarshalRvToStringRvMsgImpl();
            bHasException = false;
            _sImplementationUsed = "RvMsg";
        }

    }

    public static String rvmsgToString(TibrvMsg msg, String name) {
        return _impl.rvmsgToString(msg, name);
    }

    public static TibrvMsg stringToRvmsg(String s) throws Exception {
        return _impl.stringToRvmsg(s);
    }

}
