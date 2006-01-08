//:File:    SaveAs.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.Logger;
import rvsnoop.Project;
import rvsnoop.ui.Icons;

/**
 * Save the current project in a new file.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class SaveAs extends AbstractAction {

    public static final String ID = "saveAs";

    private static final Logger logger = Logger.getLogger(SaveAs.class);

    static String NAME = "Save As...";

    private static final long serialVersionUID = 369112751492856127L;

    static String TOOLTIP = "Save the current project into a new file";

    public SaveAs() {
        super(NAME, Icons.SAVE_AS);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".rsp");
            }
            public String getDescription() {
                return "RvSnoop Project Files";
            }
        });
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(RvSnooperGUI.getFrame()))
            return;
        final File file = chooser.getSelectedFile();
        Project project = Project.getCurrentProject();
        if (project != null)
            project.setFile(file);
        else
            project = new Project(file);
        try {
            project.store();
            logger.info("Saved project to " + file.getName());
        } catch (IOException e) {
            logger.error("There was a problem saving the project.", e);
        }
    }

}
