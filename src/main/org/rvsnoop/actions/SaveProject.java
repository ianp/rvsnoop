// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.Project;


/**
 * Save the current project.
 */
public final class SaveProject extends RvSnoopAction {

    static { NLSUtils.internationalize(SaveProject.class); }

    private static final long serialVersionUID = 6263559523473733671L;

    private static final Logger logger = Logger.getLogger();

    public static final String COMMAND = "saveProject";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;
    static String ERROR_SAVING, INFO_SAVED;

    public SaveProject(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final Project project = application.getProject();
        if (project == null) {
            application.getAction(SaveProjectAs.COMMAND).actionPerformed(event);
            return;
        }
        final File file = project.getDirectory();
        if (file == null) {
            application.getAction(SaveProjectAs.COMMAND).actionPerformed(event);
            return;
        }
        final String path = file.getPath();
        try {
            project.store(application);
            logger.info(INFO_SAVED, path);
        } catch (IOException e) {
            logger.error(e, ERROR_SAVING, path);
        }
    }

}
