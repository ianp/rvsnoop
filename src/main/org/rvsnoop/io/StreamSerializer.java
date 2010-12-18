// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.io;

import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class StreamSerializer extends Serializer {

    public StreamSerializer(OutputStream out) throws UnsupportedEncodingException {
        super(out, "UTF-8");
        setIndent(2);
        setLineSeparator("\n");
        setMaxLength(120);
    }

    @Override
    public void writeXMLDeclaration() throws IOException {
        super.writeXMLDeclaration();
    }

    @Override
    public void writeEndTag(Element element) throws IOException {
        super.writeEndTag(element);
    }

    @Override
    public void writeEmptyElementTag(Element element) throws IOException {
        super.writeEmptyElementTag(element);
    }

    @Override
    public void writeNamespaceDeclaration(String prefix, String uri) throws IOException {
        super.writeNamespaceDeclaration(prefix, uri);
    }

    @Override
    public void writeStartTag(Element element) throws IOException {
        super.writeStartTag(element);
    }

    @Override
    public void write(Attribute attribute) throws IOException {
        super.write(attribute);
    }

    @Override
    public void write(Comment comment) throws IOException {
        super.write(comment);
    }

    @Override
    public void write(DocType doctype) throws IOException {
        super.write(doctype);
    }

    @Override
    public void write(Text text) throws IOException {
        super.write(text);
    }

}
