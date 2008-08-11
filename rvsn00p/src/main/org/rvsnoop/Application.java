/*
 * Class:     Application
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.bushe.swing.event.EventService;
import org.jdesktop.application.AbstractBean;
import org.rvsnoop.actions.RvSnoopAction;
import org.rvsnoop.ui.MainFrame;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.MessageLedger;
import rvsnoop.RecordTypes;
import rvsnoop.SubjectHierarchy;
import rvsnoop.actions.Actions;

import com.google.inject.Inject;

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
public interface Application {

    /** Key for the 'project' JavaBean property. */
    public static final String KEY_PROJECT = "project";

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

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
     * Get the current project.
     *
     * @return The project, or <code>null</code> if the project has not been
     *     saved yet.
     */
    public Project getProject();

    /**
     * Get all of the known record types.
     *
     * @return The record types.
     */
    public RecordTypes getRecordTypes();

    /**
     * Get the shared subject hierarchy.
     *
     * @return The subject hierarchy.
     */
    public SubjectHierarchy getSubjectHierarchy();

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Set a new current project.
     * <p>
     * This will close and unload the current project if there is one.
     *
     * @param project The project to set.
     * @throws IOException If there is a current ledger and it could not be
     *     synchronized (applies to persistent ledgers only).
     */
    public void setProject(Project project) throws IOException;

    /**
     * Set a project for the application.
     * <p>
     * This should only be called if no current project is set. This method will
     * cause the project to be written to disk.
     *
     * @param directory The directory to store the project in.
     * @throws IOException If there is a problem storing the project.
     */
    public void setProject(File directory) throws IOException;
    
    
    public static final class Impl extends AbstractBean implements Application {

        private Actions actionFactory;

        private final Connections connections;

        private final EventService eventService;
        
        private FilteredLedgerView filteredLedger;

        /** The main application frame. */
        private MainFrame frame;

        private RecordLedger ledger;

        /** The current project. */
        private Project project;

        @Inject
        public Impl(Connections connections, EventService eventService) {
            this.connections = connections;
            this.eventService = eventService;
            UserPreferences.getInstance().listenToChangesFrom(this);
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
                filteredLedger = FilteredLedgerView.newInstance(getLedger(), false);
            }
            return filteredLedger;
        }

        public synchronized MainFrame getFrame() {
            if (frame == null) {
                frame = new MainFrame(this);
                getActionFactory().configureListeners();
            }
            return frame;
        }

        public synchronized RecordLedger getLedger() {
            if (ledger == null) {
                ledger = new InMemoryLedger(eventService);
                MessageLedger.RECORD_LEDGER = ledger;
            }
            return ledger;
        }

        public RecordLedgerTable getLedgerTable() {
            return getFrame().getRecordLedger();
        }

        public synchronized Project getProject() {
            return project;
        }

        public synchronized RecordTypes getRecordTypes() {
            // FIXME this should not use a static instance, they should be loaded from the project.
            return RecordTypes.getInstance();
        }

        public synchronized SubjectHierarchy getSubjectHierarchy() {
            // FIXME this should not use a static instance, they should be loaded from the project.
            return SubjectHierarchy.INSTANCE;
        }

        public synchronized void setProject(Project project) throws IOException {
            final Project oldProject = this.project;
            if (project.equals(oldProject)) { return; }
            // XXX when changing the ledger be sure to copy any listeners across
            //     to the new ledger instance.
            if (this.project != null) {
                ledger.syncronize();
                ledger = null;
                MessageLedger.RECORD_LEDGER = null;
                filteredLedger = null;
            }
            this.project = project;
            // TODO configure the ledger from data in the project file.
            ledger = new InMemoryLedger(eventService);
            MessageLedger.RECORD_LEDGER = ledger;
            filteredLedger = FilteredLedgerView.newInstance(ledger, false);

            getConnections().clear();
            project.loadConnections(getConnections());

            getRecordTypes().clear();
            project.loadRecordTypes(getRecordTypes());

            firePropertyChange(KEY_PROJECT, oldProject, project);
            eventService.publish(new Project.LoadedEvent(project));
        }

        public synchronized void setProject(File directory) throws IOException {
            project = new Project(directory);
            project.store(this);
            firePropertyChange(KEY_PROJECT, null, project);
            eventService.publish(new Project.LoadedEvent(project));
        }
    }

}
