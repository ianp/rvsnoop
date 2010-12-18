// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.Project;
import org.rvsnoop.ProjectFileFilter;

import rvsnoop.RvConnection;

import com.tibco.tibrv.TibrvException;

/**
 * Open a new project.
 */
public final class OpenProject extends RvSnoopAction {

    static { NLSUtils.internationalize(OpenProject.class); }

    private static final long serialVersionUID = 7204749257785908263L;

    private static final Logger logger = Logger.getLogger();

    public static final String COMMAND = "open";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;
    static String ERROR_LOADING, ERROR_STARTING, INFO_LOADING, INFO_LOADED;

    public OpenProject(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new ProjectFileFilter());
        final int option = chooser.showOpenDialog(application.getFrame());
        if (JFileChooser.APPROVE_OPTION != option) { return; }
        File file = chooser.getSelectedFile();
        try {
            file = file.getCanonicalFile();
            logger.info(INFO_LOADING, file.getName());
            application.setProject(new Project(file));
            logger.info(INFO_LOADED, file.getName());
        } catch (IOException e) {
            logger.error(e, ERROR_LOADING, file.getName());
        }
        try {
            // XXX we should really save connection state in the project file
            //     and restore it after re-opening
            RvConnection.resumeQueue();
        } catch (TibrvException e) {
            logger.error(e, ERROR_STARTING, e.error);
        }
    }

}
