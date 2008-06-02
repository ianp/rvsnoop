/*
 * Class:     OpenProject
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
import javax.swing.JFileChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.Project;
import org.rvsnoop.ProjectFileFilter;

import rvsnoop.RvConnection;

import com.tibco.tibrv.TibrvException;

/**
 * Open a new project.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class OpenProject extends RvSnoopAction {

    static { NLSUtils.internationalize(OpenProject.class); }

    private static final long serialVersionUID = 7204749257785908263L;

    private static final Log log = LogFactory.getLog(OpenProject.class);

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
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format(INFO_LOADING,
                        new Object[] { file.getName() }));
            }
            application.setProject(new Project(file));
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format(INFO_LOADED,
                        new Object[] { file.getName() }));
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(ERROR_LOADING,
                        new Object[] { file.getName() }), e);
            }
        }
        try {
            // XXX we should really save connection state in the project file
            //     and restore it after re-opening
            RvConnection.resumeQueue();
        } catch (TibrvException e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(ERROR_STARTING,
                        new Object[] { new Integer(e.error) }), e);
            }
        }
    }

}
