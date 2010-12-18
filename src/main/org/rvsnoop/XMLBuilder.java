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

    public static final String NS_CONNECTIONS = "http://rvsnoop.org/ns/connections/1";
    public static final String NS_MATCHER = "http://rvsnoop.org/ns/matcher/1";
    public static final String NS_RENDEZVOUS = "http://rvsnoop.org/ns/tibrv/1";
    public static final String NS_TYPES = "http://rvsnoop.org/ns/types/1";

    public static final String PREFIX_CONNECTIONS = "c";
    public static final String PREFIX_MATCHER= "m";
    public static final String PREFIX_RENDEZVOUS = "r";
    public static final String PREFIX_TYPES = "t";

    private boolean declared;

    private final String namespace;

    // URIs -> prefixes
    private final Map<String, String> namespaces = new LinkedHashMap<String, String>();

    private boolean started;

    private final XMLOutputter xml;

    /**
     * @param stream The stream to write to.
     * @param ns The default namespace for the document.
     */
    public XMLBuilder(OutputStream stream, String ns) {
        if (!(stream instanceof BufferedOutputStream)) {
            stream = new BufferedOutputStream(stream);
        }
        try {
            this.xml = new XMLOutputter(new OutputStreamWriter(stream), "UTF-8");
            xml.setEscaping(true);
            xml.setLineBreak(LineBreak.UNIX);
            xml.setIndentation("  ");
            xml.setQuotationMark('\'');
            this.namespace = ns != null ? ns : "";
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
        return attribute(prefixify(name, ns), value);
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
    
    public XMLBuilder namespace(String prefix, String uri) {
        if (started) { throw new IllegalStateException(); }
        namespaces.put(uri, prefix);
        return this;
    }

    private void namespaceDeclarations() throws IOException {
        if (namespace.length() > 0) {
            xml.attribute("xmlns", namespace);
        }
        for (String nsuri : namespaces.keySet()) {
            if (namespace.equals(nsuri)) {
                continue;
            }
            final String prefix = namespaces.get(nsuri);
            xml.attribute("xmlns:" + prefix, nsuri);
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

    private String prefixify(String name, String ns) {
        if (namespace.equals(ns)) { return name; }
        return namespaces.get(ns) + ':' + name;
    }

    public XMLBuilder startTag(String type) throws IOException {
        try {
            if (!declared) {
                xml.declaration();
                declared = true;
            }
            xml.startTag(type);
            if (!started) {
                namespaceDeclarations();
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
        return startTag(prefixify(type, ns));
    }

}
