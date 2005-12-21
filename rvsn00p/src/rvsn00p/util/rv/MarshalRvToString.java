//:File:    MarshalRvToString.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;

/**
 * MarshalRvToString state class
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class MarshalRvToString {

    static final IMarshalRvToStringImpl _impl;
    static String _sImplementationUsed = null;

    // selects the "best" possible implementation
    static {

        boolean bHasException = true;
        IMarshalRvToStringImpl tempImpl = null;

        if (bHasException == true) {
            try {

                tempImpl = new MarshalRvToStringRvTestImpl();
                _sImplementationUsed = "RvTest";
                bHasException = false;
            } catch (Error ex) {
                bHasException = true;
            }

        }

        if (bHasException == true) {
            try {

                tempImpl = new MarshalRvToStringRvScriptImpl();
                _sImplementationUsed = "Rvscript";
                bHasException = false;
            } catch (Error ex) {
                bHasException = true;
            }

        }

        if (bHasException == true) {
            try {

                tempImpl = new MarshalRvToStringMtreeImpl();
                bHasException = false;
                _sImplementationUsed = "Mtree";
            } catch (Error ex) {
                bHasException = true;
            }
        }

        if (bHasException == true) {
            tempImpl = new MarshalRvToStringRvMsgImpl();
            bHasException = false;
            _sImplementationUsed = "RvMsg";
        }

        _impl = tempImpl;

    }

    public static String rvmsgToString(TibrvMsg msg, String name) {
        return _impl.rvmsgToString(msg, name);
    }

    public static TibrvMsg stringToRvmsg(String s) throws Exception {
        return _impl.stringToRvmsg(s);
    }

}
