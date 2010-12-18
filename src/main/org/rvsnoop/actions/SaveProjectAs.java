// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.ProjectFileFilter;

/**
 * Save the current project in a new file.
 */
public final class SaveProjectAs extends RvSnoopAction {

    static { NLSUtils.internationalize(SaveProjectAs.class); }

    private static final long serialVersionUID = 6263559523473733671L;

    private static final Logger logger = Logger.getLogger();

    public static final String COMMAND = "saveProjectAs";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;
    static String ERROR_SAVING, INFO_SAVED;

    public SaveProjectAs(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new ProjectFileFilter());
        final int option = chooser.showSaveDialog(application.getFrame());
        if (JFileChooser.APPROVE_OPTION != option) { return; }
        File file = chooser.getSelectedFile();
        try {
            file = file.getCanonicalFile();
            application.setProject(file);
            logger.info(INFO_SAVED, file.getPath());
        } catch (IOException e) {
            logger.error(e, ERROR_SAVING, file.getPath());
        }
    }

}
