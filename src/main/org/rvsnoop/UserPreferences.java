// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import com.google.common.io.Files;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

import rvsnoop.RvConnection;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Handles the storage and retrival of the session state.
 * <p>
 * This class only handles global application state, there are also projects
 * which handle storing connection details and so on.
 */
public final class UserPreferences {

    private static final Logger logger = Logger.getLogger();

    private static UserPreferences instance;

    static String ERROR_CREATING_DIR, ERROR_STORING_CONNECTIONS;

    private static final String KEY_LAST_EXPORT_LOCATION = "LASTExportLocation";
    private static final String KEY_NUM_RECENT_CONNECTIONS = "numberOfRecentConnections";
    private static final String KEY_NUM_RECENT_PROJECTS = "numberOfRecentProjects";

    static { NLSUtils.internationalize(UserPreferences.class); }

    public static synchronized UserPreferences getInstance() {
        if (instance == null) { instance = new UserPreferences(); }
        return instance;
    }

    private final Preferences preferences =
        Preferences.userRoot().node("org").node("rvsnoop").node("sessionState");

    private final LinkedList<RvConnection> recentConnections = new LinkedList<RvConnection>();

    private final File recentConnectionsFile;

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

}
