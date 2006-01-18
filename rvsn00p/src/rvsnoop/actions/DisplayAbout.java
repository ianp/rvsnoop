//:File:    DisplayAbout.java
//:Created: Dec 24, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
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
    
    private static String NAME = "About RvSn00p";
    
    private static final long serialVersionUID = 1725655570348239778L;

    private static String TOOLTIP = "Show some information about RvSn00p";

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
            new String[] { "RvSn00p " + Version.getAsString(),
                           " ",
                           "Constructed by Örjan Lundberg (lundberg@home.se)",
                           " and Ian Phillips (ianp@ianp.org)",
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
                           "Copyright © 2002-@year@ Apache Software Foundation.",
                           "Copyright © 2005-@year@ Ian Phillips.",
                           " ",
                           "Licensed under the Apache License, Version 2.0.",
                           "\tA copy of the license has been included with this",
                           "\tdistribution as doc/license.@suffix@, or may be obtained",
                           "\tfrom http://www.apache.org/licenses/LICENSE-2.0.html" },
            NAME, JOptionPane.PLAIN_MESSAGE);
    }

}
