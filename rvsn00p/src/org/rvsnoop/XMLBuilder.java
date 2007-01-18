/*
 * Class:     XMLBuilder
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.znerd.xmlenc.LineBreak;
import org.znerd.xmlenc.XMLOutputter;

/**
 * A builder for writing XML documents.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class XMLBuilder {

    private boolean declared;

    private final String namespace;

    private final Map namespaces;

    private boolean started;

    private final XMLOutputter xml;

    /**
     * @param stream The stream to write to.
     * @param ns The default namespace for the document.
     * @param namespaces A namespace to prefix mapping for the document.
     */
    public XMLBuilder(OutputStream stream, String ns, Map namespaces) {
        if (!(stream instanceof BufferedOutputStream)) {
            stream = new BufferedOutputStream(stream);
        }
        try {
            this.xml = new XMLOutputter(new OutputStreamWriter(stream), "UTF-8");
            xml.setEscaping(true);
            xml.setIndentation("  ");
            xml.setLineBreak(LineBreak.UNIX);
            xml.setQuotationMark('\'');
            this.namespace = ns != null ? ns : "";
            this.namespaces = new LinkedHashMap(namespaces);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public XMLBuilder attribute(String name, String value) throws IOException {
        try {
            xml.attribute(name, value);
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder attribute(String name, String value, String ns) throws IOException {
        return attribute(namespaces.get(ns).toString() + ':' + name, value);
    }

    public void close() throws IOException {
        try {
            xml.endDocument();
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder comment(String text) throws IOException {
        try {
            xml.comment(text);
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder dtd(String name, String publicID, String systemID) throws IOException {
        try {
            xml.declaration();
            declared = true;
            xml.dtd(name, publicID, systemID);
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder endTag() throws IOException {
        try {
            xml.endTag();
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder pcdata(String text) throws IOException {
        try {
            xml.pcdata(text);
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder startTag(String type) throws IOException {
        try {
            if (!declared) {
                xml.declaration();
                declared = true;
            }
            xml.startTag(type);
            if (!started) {
                if (namespace.length() > 0) {
                    xml.attribute("xmlns", namespace);
                }
                for (Iterator i = namespaces.keySet().iterator(); i.hasNext(); ) {
                    final String nsuri = (String) i.next();
                    final String prefix = (String) namespaces.get(nsuri);
                    xml.attribute("xmlns:" + prefix, nsuri);
                }
                started = true;
            }
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder startTag(String type, String ns) throws IOException {
        return startTag(namespaces.get(ns).toString() + ':' + type);
    }

}
