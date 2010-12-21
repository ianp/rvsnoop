// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import java.beans.PropertyChangeListener;

import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.ApplicationContext;
import org.rvsnoop.actions.RvSnoopAction;
import org.rvsnoop.ui.MainFrame;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.RecordTypes;
import rvsnoop.SubjectHierarchy;
import rvsnoop.actions.Actions;

import com.google.inject.Inject;

/**
 * The main application class.
 * <p>
 * This serves as a central location that components can use to access common
 * data and state.
 */
public interface Application {

    /**
     * Get an action from the applications action factory.
     *
     * @param command The action command to get.
     * @return The action that corresponds to the supplied command, or
     *     <code>null</code> if no action exists for the command.
     * @see Actions#getAction(String)
     */
    public RvSnoopAction getAction(String command);

    /**
     * Get the action factory for this application.
     * <p>
     * Note that there is also a convenience method to get individual actions.
     *
     */
    public Actions getActionFactory();

    /**
     * Get the connections list for the current project.
     *
     * @return The connections list.
     */
    public Connections getConnections();

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
    public FilteredLedgerView getFilteredLedger();

    /**
     * @return the frame
     */
    public MainFrame getFrame();

    /**
     * Get the record ledger.
     * <p>
     * If no project has been loaded yet this method creates a new in-memory
     * ledger and returns that.
     *
     * @return The ledger.
     */
    public RecordLedger getLedger();

    /**
     * Get the record ledger table.
     *
     * @return The ledger table.
     */
    public RecordLedgerTable getLedgerTable();

    /**
     * Get the shared subject hierarchy.
     *
     * @return The subject hierarchy.
     */
    public SubjectHierarchy getSubjectHierarchy();

    @Deprecated
    public RecordTypes getRecordTypes();
    
    public static final class Impl extends AbstractBean implements Application {

        private final ApplicationContext context;

        private Actions actionFactory;

        private final Connections connections;

        private FilteredLedgerView filteredLedger;

        /** The main application frame. */
        private MainFrame frame;

        private RecordLedger ledger;

        private final RecordTypes types;

        private final ProjectService projectService;

        @Inject
        public Impl(ApplicationContext context, Connections connections, RecordTypes types, ProjectService projectService) {
            this.context = context;
            this.connections = connections;
            this.types = types;
            this.projectService = projectService;
        }

        public RvSnoopAction getAction(String command) {
            return getActionFactory().getAction(command);
        }

        public synchronized Actions getActionFactory() {
            if (actionFactory == null) { actionFactory = new Actions(this); }
            return actionFactory;
        }

        public synchronized Connections getConnections() {
            return connections;
        }

        public synchronized FilteredLedgerView getFilteredLedger() {
            if (filteredLedger == null) {
                filteredLedger = FilteredLedgerView.newInstance(getLedger(), types, false);
            }
            return filteredLedger;
        }

        public synchronized MainFrame getFrame() {
            if (frame == null) {
                frame = new MainFrame(context, this, types, projectService);
                getActionFactory().configureListeners();
            }
            return frame;
        }

        public synchronized RecordLedger getLedger() {
            if (ledger == null) {
                ledger = new InMemoryLedger(context, types);
            }
            return ledger;
        }

        public RecordLedgerTable getLedgerTable() {
            return getFrame().getRecordLedger();
        }

        public synchronized SubjectHierarchy getSubjectHierarchy() {
            // FIXME this should not use a static instance, they should be loaded from the project.
            return SubjectHierarchy.INSTANCE;
        }

        public RecordTypes getRecordTypes() {
            return types;
        }

    }

}
