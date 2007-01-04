/*
 * Class:     DisplayAbout
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import rvsnoop.Version;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

/**
 * Display the about dialog.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class DisplayAbout extends AbstractAction {

    private static final String ID = "displayAbout";

    private static String NAME = "About RvSnoop";

    private static final long serialVersionUID = 1725655570348239778L;

    private static String TOOLTIP = "Show some information about RvSnoop";

    public DisplayAbout() {
        super(NAME);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.SMALL_ICON, Icons.ABOUT);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        JOptionPane.showMessageDialog(
            UIManager.INSTANCE.getFrame(),
            new String[] { Version.getAsStringWithName(),
                           " ",
                           "Constructed by Örjan Lundberg (lundberg@home.se)",
                           "and Ian Phillips (ianp@ianp.org)",
                           " ",
                           "This product includes software developed by",
                           "The Apache Software Foundation (http://www.apache.org).",
                           " ",
                           "Thanks goes to (in no special order):",
                           "\tEric Albert, Stefan Axelsson, Thomas Bonderud,",
                           "\tStefan Farestam, Johan Hjort, Joe Jensen",
                           "\tMagnus L Johansson, Anders Lindlof, Linda Lundberg",
                           "\tStephanie Lundberg, Cedric Rouvrais, and Richard Valk.",
                           " ",
                           "Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.",
                           " ",
                           "Licensed under the Apache Software License (Version 2.0).",
                           "\tA copy of the license has been included with this",
                           "\tdistribution in the file doc/license.html." },
            NAME, JOptionPane.PLAIN_MESSAGE);
    }

}
