// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import rvsnoop.RecordMatcher;
import rvsnoop.RecordType;
import rvsnoop.RecordTypes;
import rvsnoop.RvConnection;
import rvsnoop.SubjectElement;
import rvsnoop.SubjectHierarchy;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Handles all of the project specific settings.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class Project {

    private static final String NAMESPACE = "http://rvsnoop.org/ns/rvsnoop/1";

    private static String colourToHexString(Color c) {
        return String.format("#%02X%02X%02X" ,c.getRed(), c.getGreen(), c.getBlue());
    }

    private final File projectDirectory;

    public Project(File file) {
        this.projectDirectory = file;
    }

    public void loadConnections(Connections connections) throws IOException {
        final File file = new File(projectDirectory, "Connections.xml");
        InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            final Element root = new Builder().build(stream).getRootElement();
            final Elements elts = root.getChildElements();
            for (int i = 0, imax = elts.size(); i < imax; ++i) {
                connections.add(RvConnection.fromXML(elts.get(i)));
            }
        } catch (ParsingException e) {
            // TODO log this exception, maybe pop a dialog.
        }
    }

    private void loadRecordType(Element element, RecordTypes types) {
        final Element matcherElt = element.getFirstChildElement("matcher", NAMESPACE);
        final RecordMatcher matcher = RecordMatcher.fromXml(matcherElt);
        final Color colour = Color.decode(element.getAttributeValue(RecordType.KEY_COLOUR));
        if (RecordMatcher.DEFAULT_MATCHER.equals(matcher)) {
            RecordTypes.DEFAULT.setColour(colour);
        } else {
            final String name = element.getAttributeValue(RecordType.KEY_NAME);
            final String selected = element.getAttributeValue(RecordType.KEY_SELECTED);
            final RecordType type = types.createType(name, colour, matcher);
            type.setSelected(Boolean.parseBoolean(selected));
        }
    }

    public void loadRecordTypes(RecordTypes types) throws IOException {
        final File file = new File(projectDirectory, "Record Types.xml");
        InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            final Element root = new Builder().build(stream).getRootElement();
            final Elements elements = root.getChildElements("type", NAMESPACE);
            for (int i = 0, imax = elements.size(); i < imax; ++i) {
                loadRecordType(elements.get(i), types);
            }
        } catch (ParsingException e) {
            // TODO log the exception
        }
    }

    private void loadSubject(Element xmlElt, SubjectElement parent, SubjectHierarchy hierarchy, JTree tree) {
        final String name = xmlElt.getAttributeValue("name");
        final boolean selected = Boolean.parseBoolean(xmlElt.getAttributeValue("selected"));
        final SubjectElement child = hierarchy.getSubjectElement(parent, name, selected);
        if (Boolean.parseBoolean(xmlElt.getAttributeValue("expanded"))) {
            tree.expandPath(new TreePath(child.getPath()));
        }
        final Elements xmlElts = xmlElt.getChildElements();
        for (int i = 0, imax = xmlElts.size(); i < imax; ++i) {
            loadSubject(xmlElts.get(i), parent, hierarchy, tree);
        }
    }

    public SubjectHierarchy loadSubjects(JTree tree) throws IOException {
        final File file = new File(projectDirectory, "Subjects.xml");
        InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            final Element root = new Builder().build(stream).getRootElement();
            final SubjectHierarchy hierarchy = new SubjectHierarchy();
            final Elements xmlElts = root.getChildElements();
            final SubjectElement parent = (SubjectElement) hierarchy.getRoot();
            for (int i = 0, imax = xmlElts.size(); i < imax; ++i) {
                loadSubject(xmlElts.get(i), parent, hierarchy, tree);
            }
            return hierarchy;
        } catch (ParsingException e) {
            // TODO log this exception, maybe pop a dialog.
            return new SubjectHierarchy();
        }
    }

    public File getDirectory() {
        return projectDirectory;
    }

    public void store(Application application) throws IOException {
        storeMetadata(application);
        storeConnections(application.getConnections());
        storeSubjects(application.getSubjectHierarchy(),
                application.getFrame().getSubjectExplorer());
        //storeTypes(application.getRecordTypes());
    }

    public void storeConnections(Connections connections) throws IOException {
        final File file = new File(projectDirectory, "Connections.xml");
        final OutputStream stream = new FileOutputStream(file);
        try {
            Connections.toXML(connections.toArray(), stream);
        } finally {
            closeQuietly(stream);
        }
    }

    private void storeMetadata(Application application) throws IOException {
        final File file = new File(projectDirectory, "RvSnoop Project.xml");
        final OutputStream stream = new FileOutputStream(file);
        try {
            new XMLBuilder(stream, NAMESPACE)
                .startTag("project", NAMESPACE)
                    .startTag("creationDate", NAMESPACE)
                        .pcdata(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()))
                        .endTag()
                    .startTag("ledger", NAMESPACE)
                        .attribute("class", InMemoryLedger.class.getName())
                        .endTag()
                .endTag().close();
        } finally {
            closeQuietly(stream);
        }
    }

    private void storeSubject(SubjectElement element, XMLBuilder builder, JTree tree) throws IOException {
        boolean expanded = tree == null || tree.isExpanded(new TreePath(element.getPath()));
        builder.startTag("subject", XMLBuilder.NS_CONNECTIONS)
            .attribute("name", element.getElementName())
            .attribute("selected", Boolean.toString(element.isSelected()))
            .attribute("expanded", Boolean.toString(expanded));
        for (Enumeration e = element.children(); e.hasMoreElements(); ) {
            storeSubject((SubjectElement) e.nextElement(), builder, tree);
        }
        builder.endTag();
    }

    public void storeSubjects(SubjectHierarchy subjects, JTree tree) throws IOException {
        final File file = new File(projectDirectory, "Subjects.xml");
        final OutputStream stream = new FileOutputStream(file);
        try {
            final XMLBuilder builder = new XMLBuilder(stream, NAMESPACE)
                .namespace(XMLBuilder.PREFIX_RENDEZVOUS, XMLBuilder.NS_RENDEZVOUS)
                .startTag("subjects", NAMESPACE);
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) subjects.getRoot();
        for (Enumeration e = root.children(); e.hasMoreElements(); ) {
            storeSubject((SubjectElement) e.nextElement(), builder, tree);
        }
        builder.endTag().close();
        } finally {
            closeQuietly(stream);
        }
    }

    public void storeTypes(RecordTypes types) throws IOException {
        final File file = new File(projectDirectory, "Record Types.xml");
        final OutputStream stream = new FileOutputStream(file);
        try {
            final XMLBuilder builder = new XMLBuilder(stream, NAMESPACE);
            builder.startTag("types", NAMESPACE);
            final RecordType[] allTypes = types.getAllTypes();
            for (int i = 0, imax = allTypes.length; i < imax; ++i) {
                final RecordType type = allTypes[i];
                builder.startTag("type", NAMESPACE)
                    .attribute(RecordType.KEY_NAME, type.getName())
                    .attribute(RecordType.KEY_SELECTED, Boolean.toString(type.isSelected()))
                    .attribute(RecordType.KEY_COLOUR, colourToHexString(type.getColour()))
                    .startTag("matcher", NAMESPACE)
                        .attribute(RecordMatcher.PROP_TYPE, type.getMatcherName())
                        .attribute(RecordMatcher.PROP_VALUE, type.getMatcherValue())
                        .endTag()
                    .endTag();
            }
            builder.endTag().close();
        } finally {
            closeQuietly(stream);
        }
    }

}
