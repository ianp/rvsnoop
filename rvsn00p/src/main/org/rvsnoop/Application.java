/*
 * Class:     Application
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.actions.RvSnoopAction;
import org.rvsnoop.ui.MainFrame;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.MessageLedger;
import rvsnoop.RecordTypes;
import rvsnoop.RvConnection;
import rvsnoop.SubjectHierarchy;
import rvsnoop.actions.Actions;

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

    /** Key for the 'project' JavaBean property. */
    public static final String KEY_PROJECT = "project";
    
    private static final Log log = LogFactory.getLog(Application.class);

    static { NLSUtils.internationalize(Application.class); }

    static String ERROR_SHUTDOWN;

    private Actions actionFactory;

    private Connections connections;

    private FilteredLedgerView filteredLedger;

    /** The main application frame. */
    private MainFrame frame;

    private RecordLedger ledger;

    /** The current project. */
    private Project project;

    private final PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);
    
    public Application() {
        UserPreferences.getInstance().listenToChangesFrom(this);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

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
     * Get the connections list for the current project.
     *
     * @return The connections list.
     */
    public synchronized Connections getConnections() {
        if (connections == null) { connections = new Connections(null, true); }
        return connections;
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
     * @return the frame
     */
    public synchronized MainFrame getFrame() {
        if (frame == null) {
            frame = new MainFrame(this);
            getActionFactory().configureListeners();
        }
        return frame;
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
            MessageLedger.RECORD_LEDGER = ledger;
        }
        return ledger;
    }

    /**
     * Get the record ledger table.
     *
     * @return The ledger table.
     */
    public RecordLedgerTable getLedgerTable() {
        return getFrame().getRecordLedger();
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
     * Get all of the known record types.
     *
     * @return The record types.
     */
    public synchronized RecordTypes getRecordTypes() {
        // FIXME this should not use a static instance, they should be loaded from the project.
        return RecordTypes.getInstance();
    }

    /**
     * Get the shared subject hierarchy.
     *
     * @return The subject hierarchy.
     */
    public synchronized SubjectHierarchy getSubjectHierarchy() {
        // FIXME this should not use a static instance, they should be loaded from the project.
        return SubjectHierarchy.INSTANCE;
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
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
        ledger = new InMemoryLedger();
        MessageLedger.RECORD_LEDGER = ledger;
        filteredLedger = FilteredLedgerView.newInstance(ledger, false);

        getConnections().clear();
        project.loadConnections(getConnections());

        getRecordTypes().clear();
        project.loadRecordTypes(getRecordTypes());
        
        propertyChangeSupport.firePropertyChange(KEY_PROJECT, oldProject, project);
    }
    
    /**
     * Set a project for the application.
     * <p>
     * This should only be called if no current project is set. This method will
     * cause the project to be written to disk.
     *
     * @param directory The directory to store the project in.
     * @throws IOException If there is a problem storing the project.
     */
    public synchronized void setProject(File directory) throws IOException {
        if (project != null) { throw new IllegalStateException(); }
        project = new Project(directory);
        project.store(this);
        propertyChangeSupport.firePropertyChange(KEY_PROJECT, null, project);
    }

    /**
     * Shut down the applicatiopn and stop the VM.
     */
    public void shutdown() {
        try {
            final RvConnection[] conns = connections.toArray();
            for (int i = 0, imax = conns.length; i < imax; ++i) {
                conns[i].stop();
            }
            RvConnection.shutdown();
            System.exit(0);
        } catch (Exception e) {
            if (log.isErrorEnabled()) { log.error(ERROR_SHUTDOWN, e); }
            System.exit(1);
        }
    }
}
