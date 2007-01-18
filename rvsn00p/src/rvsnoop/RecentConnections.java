/*
 * Class:     RecentConnections
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Elements;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Connections;
import org.rvsnoop.XMLBuilder;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Provides access to a list of recently used connections.
 * <p>
 * This class listens for additions to the connections list and keeps track
 * of them. It is also resposible for saving this information to a file when
 * the application shuts down and for reloading in at startup.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecentConnections implements ListEventListener {

    private static final int DEFAULT_MAX_SIZE = 10;

    public static RecentConnections instance;

    private static final Log log = LogFactory.getLog(RecentConnections.class);

    public static synchronized RecentConnections getInstance() {
        if (instance == null) {
            final String home = System.getProperty("user.home");
            final String fs = System.getProperty("file.separator");
            final File file = new File(home + fs + ".rvsnoop" + fs + "recentConnections.xml");
            instance = new RecentConnections(file);
        }
        return instance;
    }

    // This needs to be LinkedList then we can use the removeLast method.
    private final LinkedList connections = new LinkedList();

    private final File file;

    private int maxSize = DEFAULT_MAX_SIZE;

    private RecentConnections(File file) {
        this.file = file;
        load();
        Connections.getInstance().addListEventListener(this);
    }

    /**
     * Gets the last connection used during this session.
     *
     * @return The last connection or <code>null</code> if there has been no connection this session.
     */
    public RvConnection getLastConnection() {
        return (RvConnection) (connections.size() > 0 ? connections.getFirst() : null);
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists.event.ListEvent)
     */
    public void listChanged(ListEvent changes) {
        final EventList list = changes.getSourceList();
        list.getReadWriteLock().readLock().lock();
        try {
            while (changes.next()) {
                if (changes.getType() == ListEvent.INSERT) {
                    final Object o = list.get(changes.getIndex());
                    connections.remove(o);
                    connections.add(o);
                    if (connections.size() > maxSize)
                        connections.removeLast();
                }
            }
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    public void load() {
        if (!file.exists()) return;
        if (!file.canRead()) {
            if (log.isWarnEnabled()) {
                log.warn("Cannot read file: " + file.getName());
            }
            return;
        }
        if (file.length() == 0) {
            if (log.isWarnEnabled()) {
                log.warn("File is empty: " + file.getName());
            }
            return;
        }
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            final Document doc = new Builder().build(stream);
            final Elements elements = doc.getRootElement().getChildElements(RvConnection.XML_ELEMENT, RvConnection.XML_NS);
            for (int i = 0, imax = elements.size(); i < imax; ++i) {
                final RvConnection conn = RvConnection.fromXml(elements.get(i));
                connections.add(conn);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Unable to load file: " + file.getName(), e);
            }
        } finally {
            IOUtils.closeQuietly(stream);
            setMaxSize(maxSize);
        }
    }

    /**
     * Set a size limit for the list.
     *
     * @param maxSize
     */
    private void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        while (connections.size() > maxSize)
            connections.removeLast();
    }

    /**
     * Save the recent connections list.
     *
     * @throws IOException
     */
    public void store() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            String namespace = Connections.NAMESPACE;
            Map namespaces = ArrayUtils.toMap(new String[][] {
                    { Connections.NAMESPACE, "c" },
                    { RvConnection.XML_NS, "rv" } } );
            XMLBuilder builder = new XMLBuilder(stream, namespace, namespaces);
            new Connections(connections, false).toXML(builder);
            builder.close();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
