/*
 * Class:     PreferencesManager
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.RecordLedgerFormat.ColumnFormat;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.RvConnection;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * Handles the storage and retrival of the session state.
 * <p>
 * This class only handles global application state, there are also projects
 * which handle storing connection details and so on.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class UserPreferences {
    
    private final class DividerLocationListener implements HierarchyBoundsListener {
        public void ancestorMoved(HierarchyEvent e) {
            final Component c = e.getChanged();
            if (c instanceof JSplitPane) { setDividerLocation((JSplitPane) c); }
        }
        public void ancestorResized(HierarchyEvent e) {
            final Component c = e.getChanged();
            if (c instanceof JSplitPane) { setDividerLocation((JSplitPane) c); }
        }
    }

    private final class FontChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            if (!"font".equals(event.getPropertyName())) { return; }
            setLedgerFont((Font) event.getNewValue());
        }
    }

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

    private final class RecentConnectionsListener implements ListEventListener {
        public void listChanged(ListEvent changes) {
            updateRecentConnectionsList(changes);
        }
    }

    private final class RecentProjectsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            updateRecentProjectsList((Project) event.getNewValue());
        }
    }

    private final class WindowBoundsListener extends WindowAdapter implements HierarchyBoundsListener {
        public void ancestorMoved(HierarchyEvent e) {
            final Component component = e.getChanged();
            if (component instanceof Window) { setWindowBounds((Window) component); }
        }
        public void ancestorResized(HierarchyEvent e) {
            final Component component = e.getChanged();
            if (component instanceof Window) { setWindowBounds((Window) component); }
        }
        public void windowClosed(WindowEvent e) {
            setWindowBounds(e.getWindow());
        }
    }

    private static final Log log = LogFactory.getLog(UserPreferences.class);

    private static UserPreferences instance;

    static String ERROR_CREATING_DIR, ERROR_STORING_CONNECTIONS;

    private static final String KEY_DIVIDER_LOCATION = "DividerLocation";
    private static final String KEY_LAST_EXPORT_LOCATION = "LASTExportLocation";
    private static final String KEY_LEDGER_COLUMNS = "ledgerColumns";
    public  static final String KEY_LEDGER_FONT = "ledgerFont";
    private static final String KEY_NUM_RECENT_CONNECTIONS = "numberOfRecentConnections";
    private static final String KEY_NUM_RECENT_PROJECTS = "numberOfRecentProjects";
    private static final String KEY_RECENT_PROJECTS = "recentProjects";
    private static final String KEY_WINDOW_BOUNDS = "windowBounds";

    static { NLSUtils.internationalize(UserPreferences.class); }

    public synchronized static UserPreferences getInstance() {
        if (instance == null) { instance = new UserPreferences(); }
        return instance;
    }

    private Preferences preferences =
        Preferences.userRoot().node("org").node("rvsnoop").node("sessionState");

    private final LinkedList recentConnections = new LinkedList();

    private final File recentConnectionsFile;

    private final LinkedList recentProjects = new LinkedList();

    private UserPreferences() {
        String path = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            path = SystemUtils.USER_HOME + "/Application Data/RvSnoop";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            path = SystemUtils.USER_HOME + "/Library/Application Support/RvSnoop";
        } else {
            path = SystemUtils.USER_HOME + "/.rvsnoop";
        }
        final File recentConnectionsDir = new File(path);
        File f = null;
        try {
            FileUtils.forceMkdir(recentConnectionsDir);
            f = new File(recentConnectionsDir, "Recent Connections.xml");
        } catch (IOException e) {
            if (log.isErrorEnabled()) { log.error(ERROR_CREATING_DIR, e); }
        }
        recentConnectionsFile = f;
        final Preferences node = preferences.node(KEY_RECENT_PROJECTS);
        for (int i = 0, imax = getNumberOfRecentProjects(); i < imax; ++i) {
            final String project = node.get(Integer.toString(i), null);
            if (project != null) { recentProjects.add(new File(project)); }
        }
    }

    /**
     * @param pcl
     * @see java.util.prefs.Preferences#addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.addPreferenceChangeListener(pcl);
    }

    /**
     * Return the divider location for a named {@link JSplitPane}.
     *
     * @param name The name of the split pane to get the location for.
     * @return The location, or <code>-1</code> if no location was stored.
     */
    public int getDividerLocation(String name) {
        return preferences.getInt(name + KEY_DIVIDER_LOCATION, -1);
    }

    public String getLastExportLocation() {
        return preferences.get(KEY_LAST_EXPORT_LOCATION, SystemUtils.USER_DIR);
    }
    
    public List getLedgerColumns() {
        final List columns = new ArrayList(RecordLedgerFormat.ALL_COLUMNS.size());
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

    public Font getLedgerFont() {
        final Font font = Font.decode(preferences.get(KEY_LEDGER_FONT, ""));
        return font != null ? font : UIManager.getFont("Table.font");
    }

    public RvConnection getMostRecentConnection() {
        try {
            return (RvConnection) recentConnections.get(0);
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

    // List<RvConnection>
    public List getRecentConnections() {
        return Collections.unmodifiableList(recentConnections);
    }

    // List<File>
    public List getRecentProjects() {
        return Collections.unmodifiableList(recentProjects);
    }

    public Rectangle getWindowBounds() {
        try {
            final String bounds = preferences.get(KEY_WINDOW_BOUNDS, "100,100,800,600");
            final StringTokenizer stok = new StringTokenizer(bounds, ",");
            final int x = Integer.parseInt(stok.nextToken());
            final int y = Integer.parseInt(stok.nextToken());
            final int w = Integer.parseInt(stok.nextToken());
            final int h = Integer.parseInt(stok.nextToken());
            return new Rectangle(x, y, w, h);
        } catch (Exception e) {
            return new Rectangle(100, 100, 800, 600);
        }
    }

    public void listenToChangesFrom(Application application) {
        application.getConnections().addListEventListener(new RecentConnectionsListener());
        application.addPropertyChangeListener(Application.KEY_PROJECT, new RecentProjectsListener());
    }

    public void listenToChangesFrom(JFrame frame, RecordLedgerTable table, JSplitPane[] splits) {
        frame.addPropertyChangeListener("font", new FontChangeListener());
        final WindowBoundsListener wbl = new WindowBoundsListener();
        frame.addHierarchyBoundsListener(wbl);
        frame.addWindowListener(wbl);
        table.getColumnModel().addColumnModelListener(new LedgerColumnsListener());
        final DividerLocationListener dll = new DividerLocationListener();
        for (int i = 0, imax = splits.length; i < imax; ++i) {
            splits[i].addHierarchyBoundsListener(dll);
        }
    }

    /**
     * @param pcl
     * @see java.util.prefs.Preferences#removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.removePreferenceChangeListener(pcl);
    }

    private void setDividerLocation(final JSplitPane splitpane) {
        final String name = splitpane.getName();
        final int location = splitpane.getDividerLocation();
        preferences.putInt(name + KEY_DIVIDER_LOCATION, location);
    }

    public void getLastExportLocation(String path) {
        preferences.put(KEY_LAST_EXPORT_LOCATION, path);
    }

    private void setLedgerColumns(final TableColumnModel model) {
        final List columns = new ArrayList(model.getColumnCount());
        for (Enumeration e = model.getColumns(); e.hasMoreElements(); ) {
            columns.add(((TableColumn) e.nextElement()).getHeaderValue());
        }
        final Preferences node = preferences.node(KEY_LEDGER_COLUMNS);
        for (Iterator i = RecordLedgerFormat.ALL_COLUMNS.iterator(); i.hasNext(); ) {
            final ColumnFormat column = (ColumnFormat) i.next();
            node.putBoolean(column.getIdentifier(), columns.contains(column.getName()));
        }
    }

    public void setLedgerFont(final Font font) {
        if (font == null) { return; }
        final StrBuilder builder = new StrBuilder();
        builder.append(font.getFamily()).append('-');
        if (font.isPlain()) {
            builder.append("PLAIN");
        } else {
            if (font.isBold()) { builder.append("BOLD"); }
            if (font.isItalic()) { builder.append("ITALIC"); }
        }
        builder.append('-').append(Integer.toString(font.getSize()));
        preferences.put(KEY_LEDGER_FONT, builder.toString());
    }

    public void setNumberOfRecentConnections(int number) {
        preferences.putInt(KEY_NUM_RECENT_CONNECTIONS, number);
    }

    private void setWindowBounds(Window window) {
        final StrBuilder builder = new StrBuilder();
        builder.append(Integer.toString(window.getX())).append(',');
        builder.append(Integer.toString(window.getY())).append(',');
        builder.append(Integer.toString(window.getWidth())).append(',');
        builder.append(Integer.toString(window.getHeight()));
        preferences.put(KEY_LEDGER_FONT, builder.toString());
    }

    private void updateRecentConnectionsList(ListEvent changes) {
        final EventList list = changes.getSourceList();
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            final int max = getNumberOfRecentConnections();
            while (changes.next()) {
                if (changes.getType() == ListEvent.INSERT) {
                    final Object o = list.get(changes.getIndex());
                    recentConnections.remove(o);
                    recentConnections.add(0, o);
                    if (recentConnections.size() > max) {
                        recentConnections.removeLast();
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        if (recentConnectionsFile == null) { return; }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(recentConnectionsFile);
            new Connections(recentConnections, false).toXML(stream);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(ERROR_STORING_CONNECTIONS, e);
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void updateRecentProjectsList(Project newProject) {
        final String path = newProject.getDirectory().getPath();
        final int max = getNumberOfRecentProjects();
        recentProjects.remove(path);
        recentProjects.add(0, path);
        while (recentProjects.size() > max) {
            recentProjects.removeLast();
        }
        final Preferences node = preferences.node(KEY_RECENT_PROJECTS);
        for (int i = 0, imax = recentProjects.size(); i < imax; ++i) {
            node.put(Integer.toString(i), ((File) recentProjects.get(i)).getPath());
        }
    }

}
