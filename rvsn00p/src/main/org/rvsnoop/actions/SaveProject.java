/*
 * Class:     SaveProject
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.Action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.Project;


/**
 * Save the current project.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class SaveProject extends RvSnoopAction {

    static { NLSUtils.internationalize(SaveProject.class); }

    private static final long serialVersionUID = 6263559523473733671L;

    private static final Log log = LogFactory.getLog(SaveProject.class);

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

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
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
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format(INFO_SAVED, new Object[] { path }));
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(ERROR_SAVING, new Object[] { path }), e);
            }
        }
    }

}
