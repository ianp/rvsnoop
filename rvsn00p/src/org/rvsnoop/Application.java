/*
 * Class:     Application
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.actions.RvSnoopAction;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.PreferencesManager;
import rvsnoop.Project;
import rvsnoop.RecordTypes;
import rvsnoop.RvConnection;
import rvsnoop.actions.Actions;
import rvsnoop.ui.UIManager;

/**
 * The main application class.
 * <p>
 * This serves as a central location that components can use to access common
 * data and state.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class Application {

    static { NLSUtils.internationalize(Application.class); }

    private static final Log log = LogFactory.getLog(Application.class);

    static String ERROR_SHUTDOWN;

    private Actions actionFactory;

    private FilteredLedgerView filteredLedger;

    /** The main application frame. */
    private UIManager frame;

    private RecordLedger ledger;

    /** The current project. */
    private Project project;

    /** The session state for the application. */
    private PreferencesManager sessionState;

    /**
     * Get an action from the applications action factory.
     *
     * @param command The action command to get.
     * @return The action that corresponds to the supplied command, or
     *     <code>null</code> if no action exists for the command.
     * @see Actions#getAction(String)
     */
    public RvSnoopAction getAction(String command) {
        return getActionFactory().getAction(command);
    }

    /**
     * Get the action factory for this application.
     * <p>
     * Note that there is also a convenience method to get individual actions.
     *
     */
    public synchronized Actions getActionFactory() {
        if (actionFactory == null) { actionFactory = new Actions(this); }
        return actionFactory;
    }

    /**
     * Get the main filtered ledger view.
     * <p>
     * There is a single shared view used by the main frame of the application
     * (components are free to create additional views, but this one is common).
     * <p>
     * If no project has been loaded yet this method crfeates a new view and
     * returns it.
     *
     * @return The shared <code>FilteredLedgerView</code>.
     */
    public synchronized FilteredLedgerView getFilteredLedger() {
        if (filteredLedger == null) {
            filteredLedger = FilteredLedgerView.newInstance(getLedger(), false);
        }
        return filteredLedger;
    }

    /**
     * Get the record ledger.
     * <p>
     * If no project has been loaded yet this method creates a new in-memory
     * ledger and returns that.
     *
     * @return The ledger.
     */
    public synchronized RecordLedger getLedger() {
        if (ledger == null) {
            ledger = new InMemoryLedger();
        }
        return ledger;
    }

    /**
     * Get the record ledger table.
     *
     * @return The ledger table.
     */
    public RecordLedgerTable getLedgerTable() {
        return getFrame().getMessageLedger();
    }

    /**
     * Get the current project.
     *
     * @return The project, or <code>null</code> if the project has not been
     *     saved yet.
     */
    public synchronized Project getProject() {
        return project;
    }

    /**
     * Set a new current project.
     * <p>
     * This will close and unload the current project if there is one.
     *
     * @param project The project to set.
     * @throws IOException If there is a current ledger and it could not be
     *     synchronized (applies to persistent ledgers only).
     */
    public synchronized void setProject(Project project) throws IOException {
        // XXX when changing the ledger be sure to copy any listeners across
        //     to the new ledger instance.
        if (this.project != null) {
            ledger.syncronize();
            ledger = null;
            filteredLedger = null;
        }
        this.project = project;
        // TODO configure the ledger from data in the project file.
        ledger = new InMemoryLedger();
        filteredLedger = FilteredLedgerView.newInstance(ledger, false);
    }

    /**
     * @return the frame
     */
    public synchronized UIManager getFrame() {
        if (frame == null) {
            frame = new UIManager(this);
            getActionFactory().configureListeners();
        }
        return frame;
    }

    /**
     * Get all of the known record types.
     *
     * @return The record types.
     */
    public synchronized RecordTypes getRecordTypes() {
        // FIXME this should not use a static instance, they should be loaded from the project.
        return RecordTypes.getInstance();
    }

    /**
     * @return the sessionState
     */
    public synchronized PreferencesManager getSessionState() {
        if (sessionState == null) { sessionState = new PreferencesManager(this); }
        return sessionState;
    }

    /**
     * Shut down the applicatiopn and stop the VM.
     */
    public void shutdown() {
        try {
            RvConnection.shutdown();
            System.exit(0);
        } catch (Exception e) {
            if (log.isErrorEnabled()) { log.error(ERROR_SHUTDOWN, e); }
            System.exit(1);
        }
    }
}
