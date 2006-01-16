//:File:    Open.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.Logger;
import rvsnoop.Project;
import rvsnoop.RvConnection;
import rvsnoop.ui.Icons;

import com.tibco.tibrv.TibrvException;

/**
 * Open a new project.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class Open extends AbstractAction {

    public static final String ID = "open";

    private static final Logger logger = Logger.getLogger(Open.class);

    static String NAME = "Open...";
    
    private static final long serialVersionUID = -454491079895050133L;

    static String TOOLTIP = "Open an existing project or initialize a new one";

    public Open() {
        super(NAME, Icons.OPEN);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));
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
                return "rvSnoop Project Files";
            }
        });
        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(RvSnooperGUI.getFrame()))
            return;
        try {
            final File file = chooser.getSelectedFile().getCanonicalFile();
            logger.info("Loading project from " + file.getName());
            Project.setCurrentProject(new Project(file));
            logger.info("Loaded project from " + file.getName());
        } catch (IOException e) {
            logger.error("Could not load project file.", e);
        }
        try {
            RvConnection.resumeQueue();
        } catch (TibrvException e) {
            logger.error("Could not restart all connections.", e);
        }
    }

}
