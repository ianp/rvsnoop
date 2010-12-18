/*
 * Class:     XMLBuilder
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Element;
import nu.xom.Text;
import org.rvsnoop.io.StreamSerializer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A builder for writing XML documents.
 */
public final class XMLBuilder {

    public static final String NS_CONNECTIONS = "http://rvsnoop.org/ns/connections/1";
    public static final String NS_RENDEZVOUS = "http://rvsnoop.org/ns/tibrv/1";

    public static final String PREFIX_RENDEZVOUS = "r";

    private boolean declared;

    private final String namespace;

    // URIs -> prefixes
    private final Map<String, String> namespaces = new LinkedHashMap<String, String>();

    private final StreamSerializer xml;

    private final Stack<Element> stack = new Stack<Element>();

    /**
     * @param stream The stream to write to.
     * @param ns The default namespace for the document.
     */
    public XMLBuilder(OutputStream stream, String ns) {
        if (!(stream instanceof BufferedOutputStream)) {
            stream = new BufferedOutputStream(stream);
        }
        try {
            this.xml = new StreamSerializer(stream);
            this.namespace = ns != null ? ns : "";
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public XMLBuilder attribute(String name, String value) throws IOException {
        try {
            xml.write(new Attribute(name, value));
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public void close() throws IOException {
        try {
            while (!stack.isEmpty()) {
                xml.writeEndTag(stack.pop());
            }
            xml.flush();
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder dtd(String name, String publicID, String systemID) throws IOException {
        try {
            xml.writeXMLDeclaration();
            declared = true;
            xml.write(new DocType(name, publicID, systemID));
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }

    public XMLBuilder endTag() throws IOException {
        try {
            xml.writeEndTag(stack.pop());
            return this;
        } catch (IllegalStateException e) {
            throw new CausedIOException(e);
        } catch (IllegalArgumentException e) {
            throw new CausedIOException(e);
        }
    }
    
    public XMLBuilder namespace(String prefix, String uri) {
        if (!stack.isEmpty()) { throw new IllegalStateException(); }
        namespaces.put(uri, prefix);
        return this;
    }

    private void namespaceDeclarations() throws IOException {
        if (namespace.length() > 0) {
            xml.writeNamespaceDeclaration("", namespace);
        }
        for (String nsuri : namespaces.keySet()) {
            if (namespace.equals(nsuri)) { continue; }
            xml.writeNamespaceDeclaration(namespaces.get(nsuri), nsuri);
        }
    }

    public XMLBuilder pcdata(String text) throws IOException {
        try {
            xml.write(new Text(text));
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
                xml.writeXMLDeclaration();
                declared = true;
            }
            Element elt = new Element(type);
            xml.writeStartTag(elt);
            if (stack.isEmpty()) {
                namespaceDeclarations();
            }
            stack.push(elt);
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
