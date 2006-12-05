//:File:    Marshaller.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import com.reuters.msgtest.MsgTestException;
import com.reuters.msgtest.XMLConverter;
import com.tibco.rvscript.tibrvXmlConvert;
import com.tibco.sdk.MTree;
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
public final class Marshaller {

    public static abstract class Implementation {
        private final String name;
        Implementation(String name) {
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

    /**
     * Marshaller implementation that uses SDK MTree's to do the work.
     *
     * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     */
    static final class MTreeImpl extends Marshaller.Implementation {

        MTreeImpl() {
            super("MTree");
            try {
                Class.forName("com.tibco.sdk.MTree");
            } catch (ClassNotFoundException e) {
                // Handled by Marshaller static initializer.
            }
        }

        public String marshal(String name, TibrvMsg message) {
            final MTree mtree = new MTree(name);
            mtree.use_tibrvMsg(message);
            return mtree.toString();

        }

    }

    /**
     * Marshaller implementation that just calls <code>toString()</code> to do the work.
     * <p>
     * This implementation does not support unmarshalling strings to messages.
     *
     * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$ $Date$
     */
    static final class RvMsgImpl extends Marshaller.Implementation {

        RvMsgImpl() {
            super("TibrvMsg");
        }

        public String marshal(String name, TibrvMsg message) {
            return message.toString();
        }

    }

    /**
     * Marshaller implementation that uses RvScript to do the work.
     *
     * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     */
    static final class RvScriptImpl extends Marshaller.Implementation {

        private final tibrvXmlConvert converter = new tibrvXmlConvert();

        RvScriptImpl() {
            super("RvScript");
        }

        public String marshal(String name, TibrvMsg message) {
            return converter.rvmsgToXml(message,name);
        }

        public TibrvMsg unmarshal(String string) {
            return converter.xmlToRvmsg(string);
        }

    }

    /**
     * Marshaller implementation that uses RvTest to do the work.
     * <p>
     * The XML String is rather verbose because it works with either plain
     * Rendezvous or SDK-style Active enterprise messages.
     *
     * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     * @since 1.2.6
     */
    static final class RvTestImpl extends Marshaller.Implementation {

        private final XMLConverter converter = new XMLConverter();

        RvTestImpl() {
            super("RvTest");
        }

        public String marshal(String name, TibrvMsg message) {
            try {
                return converter.createXML(message).toString();
            } catch (MsgTestException e) {
                if (Logger.isErrorEnabled()) logger.error("Unable to marshal message.", e);
                return "";
            }
        }

    }

    public static final String IMPL_MTREE = "rvsnoop.Marshaller$MTreeImpl";
    public static final String IMPL_RVMSG = "rvsnoop.Marshaller$RvMsgImpl";
    public static final String IMPL_RVSCRIPT = "rvsnoop.Marshaller$RvScriptImpl";
    public static final String IMPL_RVTEST = "rvsnoop.Marshaller$RvTestImpl";

    private static final Implementation implementation;

    private static final Logger logger = Logger.getLogger(Marshaller.class);

    private static final String[] PREFERRED = {
        IMPL_RVTEST, IMPL_RVSCRIPT, IMPL_MTREE, IMPL_RVMSG
    };

    public static Implementation getImplementation(String className) {
        try {
            return (Implementation) Class.forName(className).newInstance();
        } catch (Exception e) {
            if (Logger.isDebugEnabled())
                logger.debug("Failed to load marshaller: " + className);
            return null;
        } catch (NoClassDefFoundError e) {
            if (Logger.isDebugEnabled())
                logger.debug("Failed to load marshaller: " + className);
            return null;
        }
    }

    static {
        // Allow custom marshallers via a system preference.
        String additional = System.getProperty("rvsnoop.marshaller");
        String[] preferred = new String[additional == null ? 4 : 5];
        if (additional != null) preferred[0] = additional;
        System.arraycopy(PREFERRED, 0, preferred, additional == null ? 0 : 1, 4);
        Implementation impl = null;
        for (int i = 0; i < preferred.length; ++i)
            if ((impl = getImplementation(preferred[i])) != null)
                break;
        implementation = impl;
        if (Logger.isInfoEnabled()) {
            if (impl == null)
                logger.info("No marshaller loaded!");
            else
                logger.info("Using marshaller: " + impl.name);
        }
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
    private Marshaller() {
        throw new UnsupportedOperationException();
    }

}
