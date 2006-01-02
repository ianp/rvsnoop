//:File:    Quit.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import rvsn00p.RecentListeners;
import rvsn00p.ui.Banners;
import rvsn00p.ui.Icons;
import rvsn00p.ui.UIUtils;
import rvsn00p.util.rv.RvController;
import rvsn00p.viewer.RvSnooperGUI;

/**
 * Quit the application.
 * <p>
 * Prompt the user for confirmation before quitting.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class Quit extends AbstractAction {

    static String CONFIRM_QUESTION = "Are you sure that you want to quit?";

    private static final String ID = "quit";

    static String NAME = "Quit";
    
    private static final long serialVersionUID = -75328795847562924L;
    
    static String TOOLTIP = "Close all listeners and exit RvSn00p";
    
    public Quit() {
        super(NAME, Icons.QUIT);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        try {
            RecentListeners.getInstance().store();
        } catch (IOException ignored) {
            // We can live with not saving the recent listeners list.
        }
        final JFrame frame = RvSnooperGUI.getAppFrame();
        if (!UIUtils.askForConfirmation(CONFIRM_QUESTION, Banners.QUIT))
            frame.dispose();
        try {
            RvController.shutdownAll();
            RvController.close();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        } finally {
            System.exit(0);
        }   
    }

}
