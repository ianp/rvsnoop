//:File:    SaveAs.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import rvsnoop.Logger;
import rvsnoop.Project;
import rvsnoop.ProjectFileFilter;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * Save the current project in a new file.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class SaveAs extends AbstractAction {

    private static final String ID = "saveAs";

    private static final Logger logger = Logger.getLogger(SaveAs.class);

    private static String NAME = "Save As...";

    private static final long serialVersionUID = 369112751492856127L;

    private static String TOOLTIP = "Save the current project into a new file";

    public SaveAs() {
        super(NAME, Icons.SAVE_AS);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_DOWN_MASK;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(ProjectFileFilter.INSTANCE);
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(UIManager.INSTANCE.getFrame()))
            return;
        try {
            final File file = chooser.getSelectedFile().getCanonicalFile();
            new Project(file).store();
            logger.info("Saved project to " + file.getName());
        } catch (IOException e) {
            logger.error("There was a problem saving the project.", e);
        }
    }

}
