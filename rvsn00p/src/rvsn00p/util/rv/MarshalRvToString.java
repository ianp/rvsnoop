//:File:    MarshalRvToString.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsg;

/**
 * Utility methods to convert Rendezvous messages to specific string
 * representations and back again.
 * <p>
 * This class will select the "best" conversion method on loading. Not all
 * conversion methods work both ways, but all can convert messages to strings.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class MarshalRvToString {
    
    public static abstract class Implementation {
        private final String name;
        protected Implementation(String name) {
            this.name = name;
        }
        public final String getName() {
            return name;
        }
        public String marshal(String name, TibrvMsg message) {
            throw new UnsupportedOperationException();
        }
        public TibrvMsg unmarshal(String string) {
            throw new UnsupportedOperationException();
        }
    }

    static final Implementation implementation;

    private static final String[] PREFERRED = {
        "rvsn00p.util.rv.MarshalRvToStringRvTestImpl",
        "rvsn00p.util.rv.MarshalRvToStringRvScriptImpl",
        "rvsn00p.util.rv.MarshalRvToStringMtreeImpl",
        "rvsn00p.util.rv.MarshalRvToStringRvMsgImpl"
    };

    static {
        // Allow custom marshallers via a system preference.
        String additional = System.getProperty("rvsn00p.marshaller");
        String[] preferred = new String[additional == null ? 4 : 5];
        if (additional != null) preferred[0] = additional;
        System.arraycopy(PREFERRED, 0, preferred, additional == null ? 0 : 1, 4);
        Implementation impl = null;
        for (int i = 0; i < preferred.length; ++i)
            try {
                Class clazz = Class.forName(preferred[i]);
                impl = (Implementation) clazz.newInstance();
                break;
            } catch (Exception e) {
                // Try the next one then...
            } catch (NoClassDefFoundError e) {
                // Try the next one then...
            }
        implementation = impl;
    }

    /**
     * Get the name of the marshaller implementation that is in use.
     * 
     * @return The name of the marshaller.
     */
    public static String getImplementationName() {
        if (implementation == null) throw new UnsupportedOperationException();
        return implementation.getName();
    }

    /**
     * Marshal a given message to it's string representation.
     * 
     * @param name The "name" of the message. Some marshallers will use this as
     *        part of the output while others will ignore it.
     * @param message The message to marshal.
     * @return The message's string form
     */
    public static String marshal(String name, TibrvMsg message) {
        if (implementation == null) throw new UnsupportedOperationException();
        return implementation.marshal(name, message);
    }

    /**
     * Unmarshal a message from it's string form.
     * <p>
     * In general, only the more advanced marshallers can perform this operation.
     * 
     * @param string The string form of the message.
     * @return The message.
     * @throws UnsupportedOperationException
     */
    public static TibrvMsg unmarshal(String string) {
        if (implementation == null) throw new UnsupportedOperationException();
        return implementation.unmarshal(string);
    }

    /**
     * Do not instantiate.
     */
    private MarshalRvToString() {
        throw new UnsupportedOperationException();
    }

}
