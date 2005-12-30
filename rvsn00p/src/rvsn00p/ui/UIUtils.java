//:File:    UIUtils.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:SVNID:   $Id$
package rvsn00p.ui;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.tibco.tibrv.TibrvException;

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
    
    public static void configureOKAndCancelButtons(JPanel panel, Action ok, Action cancel) {
        final ActionMap actionMap = panel.getActionMap();
        actionMap.put(cancel.getValue(Action.ACTION_COMMAND_KEY), cancel);
        actionMap.put(ok.getValue(Action.ACTION_COMMAND_KEY), ok);
        final InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancel.getValue(Action.ACTION_COMMAND_KEY));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ok.getValue(Action.ACTION_COMMAND_KEY));
    }
    
    public static void showError(String message, Exception exception) {
        final String s = exception.getLocalizedMessage();
        if (s == null)
            JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), message, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), new String[] { message, s }, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showInformation(Object message) {
        JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), message, INFORMATION_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Do not instantiate.
     */
    private UIUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Displays a Rendezvous exception to the user in a dialog.
     * 
     * @param parent The parent component for the dialog box.
     * @param message The message to pass to the user.
     * @param e The exception.
     */
    public static void showTibrvException(String message, TibrvException e) {
        JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), new String[] {message, e.getLocalizedMessage()},
            "Rendezvous Error " + Integer.toString(e.error), JOptionPane.ERROR_MESSAGE);
    }

}
