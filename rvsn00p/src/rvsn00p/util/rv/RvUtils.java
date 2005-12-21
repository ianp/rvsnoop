//:File:    RvUtils.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$

/**
 * A collection of static utility methods for working with Rendezvous.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RvUtils {

    /**
     * Extracts an Active Enterprise style tracking ID from a message.
     * 
     * @param message The message to extract the tracking ID from.
     * @return A string containing tracking ID, or the empty string if no ID was found.
     * @throws TibrvException
     */
    public static String getTrackingId(TibrvMsg message) throws TibrvException {
        if (message == null) return "";
        TibrvMsgField field = message.getField("^tracking^");
        if (field == null) return "";
        return (String) ((TibrvMsg) field.data).get("^id^");
    }

    private static boolean isTextInFieldData(String text, TibrvMsgField field) throws TibrvException {
        switch (field.type) {
        case TibrvMsg.MSG:
            return isTextInMessageData(text, (TibrvMsg) field.data);
        case TibrvMsg.STRING:
        case TibrvMsg.XML:
            return ((String) field.data).equalsIgnoreCase(text);
        default:
            return false;
        }
    }
    
    /**
     * Search all of the text in a message for a given string.
     * <p>
     * This will search all fields which contain strings, and recursively search
     * all nested messages.
     * 
     * @param text The text to search for.
     * @param message The message to search.
     * @return <code>true</code> if the text was matched, <code>false</code>
     *         otherwise.
     * @throws TibrvException
     */
    public static boolean isTextInMessageData(String text, TibrvMsg message) throws TibrvException {
        for (int i = 0, imax = message.getNumFields(); i < imax; ++i)
            if (isTextInFieldData(text, message.getField(i)))
                return true;
        return false;
    }
    
    /**
     * Displays a Rendezvous exception to the user in a dialog.
     * 
     * @param parent The parent component for the dialog box.
     * @param message The message to pass to the user.
     * @param e The exception.
     */
    public static void showTibrvException(Component parent, String message, TibrvException e) {
        JOptionPane.showMessageDialog(null, new String[] {message, e.getLocalizedMessage()},
            "Rendezvous Error " + Integer.toString(e.error), JOptionPane.ERROR_MESSAGE);
    }
}
