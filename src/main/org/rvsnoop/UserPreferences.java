// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.google.common.io.Files;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.rvsnoop.RecordLedgerFormat.ColumnFormat;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.RvConnection;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Handles the storage and retrival of the session state.
 * <p>
 * This class only handles global application state, there are also projects
 * which handle storing connection details and so on.
 */
public final class UserPreferences {

    private final class LedgerColumnsListener implements TableColumnModelListener {
        public void columnAdded(TableColumnModelEvent e) {
            setLedgerColumns((TableColumnModel) e.getSource());
        }
        public void columnMarginChanged(ChangeEvent e) { /* NO-OP */ }
        public void columnMoved(TableColumnModelEvent e) { /* NO-OP */ }
        public void columnRemoved(TableColumnModelEvent e) {
            setLedgerColumns((TableColumnModel) e.getSource());
        }
        public void columnSelectionChanged(ListSelectionEvent e) { /* NO-OP */ }
    }

    private final class RecentProjectsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            updateRecentProjectsList((Project) event.getNewValue());
        }
    }

    private static final Logger logger = Logger.getLogger();

    private static UserPreferences instance;

    static String ERROR_CREATING_DIR, ERROR_STORING_CONNECTIONS;

    private static final String KEY_LAST_EXPORT_LOCATION = "LASTExportLocation";
    private static final String KEY_LEDGER_COLUMNS = "ledgerColumns";
    private static final String KEY_NUM_RECENT_CONNECTIONS = "numberOfRecentConnections";
    private static final String KEY_NUM_RECENT_PROJECTS = "numberOfRecentProjects";
    private static final String KEY_RECENT_PROJECTS = "recentProjects";

    static { NLSUtils.internationalize(UserPreferences.class); }

    public static synchronized UserPreferences getInstance() {
        if (instance == null) { instance = new UserPreferences(); }
        return instance;
    }

    private final Preferences preferences =
        Preferences.userRoot().node("org").node("rvsnoop").node("sessionState");

    private final LinkedList<RvConnection> recentConnections = new LinkedList<RvConnection>();

    private final File recentConnectionsFile;

    private final LinkedList<File> recentProjects = new LinkedList<File>();

    private UserPreferences() {
        String path;
        if (SystemUtils.IS_OS_WINDOWS) {
            path = System.getProperty("user.home") + "/Application Data/RvSnoop";
        } else if (SystemUtils.IS_OS_MAC) {
            path = System.getProperty("user.home") + "/Library/Application Support/RvSnoop";
        } else {
            path = System.getProperty("user.home") + "/.rvsnoop";
        }
        final File recentConnectionsDir = new File(path);
        File f = null;
        try {
            f = new File(recentConnectionsDir, "Recent Connections.xml");
            Files.createParentDirs(f);
        } catch (IOException e) {
            logger.error(e, ERROR_CREATING_DIR);
        }
        recentConnectionsFile = f;
        final Preferences node = preferences.node(KEY_RECENT_PROJECTS);
        for (int i = 0, imax = getNumberOfRecentProjects(); i < imax; ++i) {
            final String project = node.get(Integer.toString(i), null);
            if (project != null) { recentProjects.add(new File(project)); }
        }
        EventBus.subscribe(Connections.AddedEvent.class, new EventSubscriber<Connections.AddedEvent>() {
            public void onEvent(Connections.AddedEvent event) {
                updateRecentConnectionsList(event.getConnection());
            }
        });

    }

    /**
     * @param pcl
     * @see java.util.prefs.Preferences#addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.addPreferenceChangeListener(pcl);
    }

    public String getLastExportLocation() {
        return preferences.get(KEY_LAST_EXPORT_LOCATION, System.getProperty("user.dir"));
    }

    public List<ColumnFormat> getLedgerColumns() {
        final List<ColumnFormat> columns = new ArrayList<ColumnFormat>(RecordLedgerFormat.ALL_COLUMNS.size());
        final Preferences node = preferences.node(KEY_LEDGER_COLUMNS);
        try {
            final String[] keys = node.keys();
            for (int i = 0, imax = keys.length; i < imax; ++i) {
                if (!node.getBoolean(keys[i], false)) { continue; }
                final ColumnFormat column = RecordLedgerFormat.getColumn(keys[i]);
                if (column != null) { columns.add(column); }
            }
        } catch (BackingStoreException e) {
            columns.clear();
            columns.addAll(RecordLedgerFormat.ALL_COLUMNS);
            columns.remove(RecordLedgerFormat.MESSAGE);
        }
        return columns;
    }

    public RvConnection getMostRecentConnection() {
        try {
            return recentConnections.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public int getNumberOfRecentConnections() {
        return preferences.getInt(KEY_NUM_RECENT_CONNECTIONS, 10);
    }

    public int getNumberOfRecentProjects() {
        return preferences.getInt(KEY_NUM_RECENT_PROJECTS, 10);
    }

    public List<RvConnection> getRecentConnections() {
        return Collections.unmodifiableList(recentConnections);
    }

    public List<File> getRecentProjects() {
        return Collections.unmodifiableList(recentProjects);
    }

    public void listenToChangesFrom(Application application) {
        application.addPropertyChangeListener(Application.KEY_PROJECT, new RecentProjectsListener());
    }

    public void listenToChangesFrom(JFrame frame, RecordLedgerTable table, JSplitPane[] splits) {
        table.getColumnModel().addColumnModelListener(new LedgerColumnsListener());
    }

    /**
     * @param pcl
     * @see java.util.prefs.Preferences#removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.removePreferenceChangeListener(pcl);
    }

    public void getLastExportLocation(String path) {
        preferences.put(KEY_LAST_EXPORT_LOCATION, path);
    }

    @SuppressWarnings("unchecked")
	private void setLedgerColumns(final TableColumnModel model) {
        final List columns = new ArrayList(model.getColumnCount());
        for (Enumeration e = model.getColumns(); e.hasMoreElements(); ) {
            columns.add(((TableColumn) e.nextElement()).getHeaderValue());
        }
        final Preferences node = preferences.node(KEY_LEDGER_COLUMNS);
        for (ColumnFormat column : RecordLedgerFormat.ALL_COLUMNS) {
            node.putBoolean(column.getIdentifier(), columns.contains(column.getName()));
        }
    }

    public void setNumberOfRecentConnections(int number) {
        preferences.putInt(KEY_NUM_RECENT_CONNECTIONS, number);
    }

    public void store() {
        if (recentConnectionsFile == null) { return; }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(recentConnectionsFile);
            Connections.toXML(recentConnections.toArray(new RvConnection[0]), stream);
        } catch (IOException e) {
            logger.error(e, ERROR_STORING_CONNECTIONS);
        } finally {
            closeQuietly(stream);
        }
    }

    private void updateRecentConnectionsList(RvConnection connection) {
        final int max = getNumberOfRecentConnections();
        recentConnections.remove(connection);
        recentConnections.add(0, connection);
        if (recentConnections.size() > max) {
            recentConnections.removeLast();
        }
    }

    private void updateRecentProjectsList(Project newProject) {
        final File path = newProject.getDirectory();
        final int max = getNumberOfRecentProjects();
        recentProjects.remove(path);
        recentProjects.add(0, path);
        while (recentProjects.size() > max) {
            recentProjects.removeLast();
        }
        final Preferences node = preferences.node(KEY_RECENT_PROJECTS);
        for (int i = 0, imax = recentProjects.size(); i < imax; ++i) {
            node.put(Integer.toString(i), (recentProjects.get(i)).getPath());
        }
    }

}
