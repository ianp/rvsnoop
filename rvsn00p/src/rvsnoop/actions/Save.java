//:File:    Save.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.Logger;
import rvsnoop.Project;
import rvsnoop.ui.Icons;

/**
 * Save the current project.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class Save extends AbstractAction {

    private static final String ID = "save";

    private static final Logger logger = Logger.getLogger(Save.class);

    private static String NAME = "Save";

    private static final long serialVersionUID = 1873782974936025367L;

    private static String TOOLTIP = "Save the current project";

    public Save() {
        super(NAME, Icons.SAVE);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final Project project = Project.getCurrentProject();
        if (project == null || project.getFile() == null) {
            Actions.SAVE_AS.actionPerformed(event);
        } else {
            try {
                project.store();
                logger.info("Saved project to " + project.getFile().getName());
            } catch (IOException e) {
                logger.error("There was a problem saving the project.", e);
            }
        }
    }

}
