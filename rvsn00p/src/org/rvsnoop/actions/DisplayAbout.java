/*
 * Class:     DisplayAbout
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;

import rvsnoop.Version;

/**
 * Display the about dialog.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class DisplayAbout extends RvSnoopAction {

    static { NLSUtils.internationalize(DisplayAbout.class); }

    private static final long serialVersionUID = -7216295133474774451L;

    public static final String COMMAND = "displayAbout";
    static String MNEMONIC, NAME, TOOLTIP;

    public DisplayAbout(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        // TODO add a real about dialog
        JOptionPane.showMessageDialog(
            application.getFrame().getFrame(),
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
