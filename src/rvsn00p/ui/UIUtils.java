//:File:    UIUtils.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:SVNID:   $Id$
package rvsn00p.ui;

import javax.swing.JOptionPane;

import rvsn00p.viewer.RvSnooperGUI;

/**
 * A collection of static utility methods for working with Swing.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class UIUtils {
    
    static String ERROR_TITLE = "Error";

    static String INFORMATION_TITLE = "Information";
    
    public static void showError(String message, Exception exception) {
        String s = exception.getLocalizedMessage();
        if (s == null)
            JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), message, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), new String[] { message, s }, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showInformation(Object message) {
        JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), message, INFORMATION_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private UIUtils() {
        // Do not instantiate.
    }

}
