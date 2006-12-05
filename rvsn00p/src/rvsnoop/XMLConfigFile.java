//:File:    XMLConfigFile.java
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;

import org.apache.commons.io.IOUtils;

/**
 * Handles storing and retreiving application state from XML files.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
abstract class XMLConfigFile {
    // TODO: Make final and use has-a rather than is-a for composition.
    //       Alter the API to match the OSGi Preferences node.

    private static final Logger logger = Logger.getLogger(XMLConfigFile.class);

    final File file;

    XMLConfigFile(File file) {
        super();
        this.file = file;
    }

    static final Element appendElement(Element parent, String name) {
        final Element child = new Element(name);
        parent.appendChild(child);
        return child;
    }

    /**
     * Delete the saved configuration.
     * <p>
     * Whether there was a saved configuration or not, this method will always
     * reset the state of the subject explorer tree.
     */
    public final void delete() {
        try {
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            if (Logger.isErrorEnabled())
                logger.error("Unable to delete file: " + file.getName(), e);
        }
        postDeleteHook();
    }

    static final boolean getBoolean(Element parent, String attr, boolean def) {
        try {
            final String value = parent.getAttributeValue(attr);
            return Boolean.valueOf(value).booleanValue();
        } catch (Exception e) {
            return def;
        }
    }

    protected abstract Document getDocument();

    /**
     * Get the file that is used to store the configuration.
     *
     * @return The file.
     */
    public File getFile() {
        return file;
    }

    static final int getInteger(Element parent, String name, int def) {
        try {
            final Element child = parent.getFirstChildElement(name);
            return Integer.parseInt(child.getValue().trim());
        } catch (Exception e) {
            return def;
        }
    }

    static final String getString(Element parent, String name) {
        final Element child = parent.getFirstChildElement(name);
        return child != null ? child.getValue().trim() : null;
    }

    /**
     * Load a saved configuration from a file.
     */
    public final void load() {
        if (!file.canRead()) {
            if (Logger.isWarnEnabled()) logger.warn("Cannot read file: " + file.getName());
            return;
        }
        if (file.length() == 0) {
            if (Logger.isWarnEnabled()) logger.warn("File is empty: " + file.getName());
            return;
        }
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            final Document doc = new Builder().build(stream);
            load(doc.getRootElement());
        } catch (Exception e) {
            if (Logger.isErrorEnabled())
                logger.error("Unable to load file: " + file.getName(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    // Subclasses may perform any work an a load, therefore we need to
    // handle any kind of exception here.
    protected abstract void load(Element root) throws Exception; //NOPMD

    /**
     * Called after a delete in case subclasses need to perform additional work.
     *
     */
    void postDeleteHook() {
        // Do nothing by default.
    }

    static final void setBoolean(Element parent, String name, boolean value) {
        parent.addAttribute(new Attribute(name, Boolean.toString(value)));
    }

    static final Element setInteger(Element parent, String name, int value) {
        final Element child = new Element(name);
        parent.appendChild(child);
        child.appendChild(new Text(Integer.toString(value)));
        return child;
    }

    static final Element setString(Element parent, String name, String value) {
        if (value == null || value.length() == 0) return null;
        final Element child = new Element(name);
        parent.appendChild(child);
        child.appendChild(new Text(value));
        return child;
    }

    /**
     * Save the current configuration to file.
     *
     * @throws IOException
     */
    public final void store() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            final Serializer ser = new Serializer(stream);
            ser.setIndent(2);
            ser.setLineSeparator("\n");
            ser.write(getDocument());
            ser.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
