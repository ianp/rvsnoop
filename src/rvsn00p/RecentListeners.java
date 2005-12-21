//:File:    RecentListeners.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;
import nu.xom.Text;
import rvsn00p.util.IOUtils;
import rvsn00p.util.rv.RvParameters;

/**
 * Provides access to a list of recently used listeners.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecentListeners {

    private static final String CONFIG_DIRECTORY = ".rvsnoop";
    private static final String CONFIG_FILE = "recent.xml";
    private static final int DEFAULT_MAX_SIZE = 10;
    private static final String ROOT = "recentListeners";
    private static final String RV_DAEMON = "daemon";
    private static final String RV_LISTENER = "listener";
    private static final String RV_NETWORK = "network";
    private static final String RV_SERVICE = "service";
    private static final String SUBJECT = "subject";

    private static RecentListeners instance;
    
    private static Element appendElement(Element parent, String name) {
        Element child = new Element(name);
        parent.appendChild(child);
        return child;
    }
    
    public static synchronized RecentListeners getInstance() {
        return instance != null ? instance : (instance = new RecentListeners());
    }
    
    private static String getString(Element parent, String name) {
        Element child = parent.getFirstChildElement(name);
        return child != null ? child.getValue().trim() : null;
    }

    private static Element setString(Element parent, String name, String value) {
        if (value == null || value.length() == 0) return null;
        Element child = new Element(name);
        parent.appendChild(child);
        child.appendChild(new Text(value));
        return child;
    }

    private int maxSize = DEFAULT_MAX_SIZE;

    private final LinkedList listeners = new LinkedList();

    private RecentListeners() {
        load();
    }

    /**
     * Adds a listener to the list.
     * <p>
     * If the listener to be added is already in the list then it is promoted instead.
     * 
     * @param p The listener to add.
     * @see #promote(RvParameters)
     */
    public void add(RvParameters p) {
        if (listeners.contains(p))
            promote(p);
        else
            listeners.addFirst(p);
        while (listeners.size() > maxSize)
            listeners.removeLast();
    }

    /**
     * Gets a specific recent listener by index.
     * 
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    public Object get(int index) {
        return listeners.get(index);
    }

    private String getFilename() {
        String home = System.getProperty("user.home");
        String fs = System.getProperty("file.separator");
        return home + fs + CONFIG_DIRECTORY + fs + CONFIG_FILE;
    }
    
    /**
     * Gets a read only view of the list of listeners.
     * 
     * @return
     */
    public List getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    private void listenersExport(Element parent) {
        Iterator iter = listeners.iterator();
        if (iter == null) return;
        while (iter.hasNext()) {
            RvParameters params = (RvParameters) iter.next();
            Element listener = appendElement(parent, RV_LISTENER);
            setString(listener, RV_SERVICE, params.getService());
            setString(listener, RV_NETWORK, params.getNetwork());
            setString(listener, RV_DAEMON, params.getDaemon());
            Set subjects = params.getSubjects();
            if (subjects == null) continue;
            for (Iterator i = subjects.iterator(); i.hasNext(); )
                setString(listener, SUBJECT, (String) i.next());
        }
    }

    private void listenersImport(Element parent) {
        Elements listeners = parent.getChildElements(RV_LISTENER);
        for (int i = listeners.size(); i != 0;) {
            Element listener = listeners.get(--i);
            String service = getString(listener, RV_SERVICE);
            String network = getString(listener, RV_NETWORK);
            String daemon = getString(listener, RV_DAEMON);
            RvParameters param = new RvParameters(service, network, daemon);
            Elements subjects = listener.getChildElements(SUBJECT);
            for (int j = subjects.size(); j != 0;)
                param.addSubject(subjects.get(--j).getValue());
            add(param);
        }
    }

    private void load() {
        File file = new File(getFilename());
        if (!file.exists()) return;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            Document doc = new Builder().build(stream);
            Element root = doc.getRootElement();
            listenersImport(root);
        } catch (Exception e) {
            System.err.println("Unable to load configuration from " + file.getPath());
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Moves a listener to the top of the list.
     * 
     * @param p The listener to be promoted.
     * @return <code>true</code> if the parameters were promoted,
     *         <code>false</code> if they were not in the list.
     * @throws IndexOutOfBoundsException
     */
    public boolean promote(RvParameters p) {
        if (listeners.remove(p)) {
            listeners.addFirst(p);
            return true;
        }
        return false;
    }

    /**
     * Set a size limit for the list.
     * 
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        while (listeners.size() > maxSize)
            listeners.removeLast();
        this.maxSize = maxSize;
    }

    /**
     * Gets the current number of entries in the recent listeners list.
     * 
     * @return
     */
    public int size() {
        return listeners.size();
    }

    public void store() throws IOException {
        File file = new File(getFilename());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        Element config = new Element(ROOT);
        listenersExport(config);
        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            Serializer ser = new Serializer(stream);
            ser.setIndent(2);
            ser.setLineSeparator("\n");
            ser.write(new Document(config));
            ser.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
