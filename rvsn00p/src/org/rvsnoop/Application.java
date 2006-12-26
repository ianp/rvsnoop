/*
 * Class:     Application
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.IOException;

import rvsnoop.Project;

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

    /** The current project. */
    private Project project;

    private RecordLedger ledger;

    private FilteredLedgerView filteredLedger;

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
        if (this.project != null) {
            ledger.syncronize();
            ledger = null;
            filteredLedger = null;
        }
        this.project = project;
        // TODO: Configure the ledger from data in the project file.
        ledger = new InMemoryLedger();
        filteredLedger = new FilteredLedgerView(ledger);
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
            filteredLedger = new FilteredLedgerView(ledger);
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

}
