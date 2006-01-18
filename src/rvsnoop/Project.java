//:File:    Project.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.awt.Color;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ca.odell.glazedlists.matchers.Matcher;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import rvsnoop.RecordMatcher.NameValueMatcher;
import rvsnoop.ui.UIManager;

/**
 * Handles all of the project specific settings.
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class Project extends XMLConfigFile {

    private static final String COLOUR = "colour";
    private static final String COLOUR_BLUE = "blue";
    private static final String COLOUR_GREEN = "green";
    private static final String COLOUR_RED = "red";
    private static Project currentProject;
    private static final String ROOT = "rvsnoop";
    private static final String RV_CONNECTION = "connection";
    private static final String RV_DAEMON = "daemon";
    private static final String RV_DESCRIPTION = "description";
    private static final String RV_NETWORK = "network";
    private static final String RV_SERVICE = "service";
    private static final String SUBJECT = "subject";
    private static final String SUBJECT_EXPANDED = "expanded";
    private static final String SUBJECT_SELECTED = "selected";
    private static final String SUBJECTS = "subjects";
    private static final String TYPE = "messageType";
    private static final String TYPE_NAME = "name";
    private static final String TYPE_MATCHER = "matcher";
    private static final String TYPE_MATCHER_NAME = "name";
    private static final String TYPE_MATCHER_VALUE = "value";
    private static final String TYPE_SELECTED = "selected";
    
    /**
     * @return Returns the currentProject.
     */
    public static Project getCurrentProject() {
        return currentProject;
    }

    /**
     * @param path The path to convert.
     * @param buffer The buffer to work with.
     * @return The converted path.
     */
    private static StringBuffer pathToString(TreePath path, StringBuffer buffer) {
        buffer.setLength(0);
        // Begin at one to skip the 'Categories' root node.
        final Object[] nodes = path.getPath();
        for (int i = 1; i < nodes.length; ++i)
            buffer.append(((SubjectElement) nodes[i]).getElementName()).append(".");
        buffer.setLength(buffer.length() - 1);
        return buffer;
    }

    /**
     * @param currentProject The currentProject to set.
     */
    public static void setCurrentProject(Project currentProject) {
        for (final Iterator i = RvConnection.allConnections().iterator(); i.hasNext(); )
            RvConnection.destroyConnection((RvConnection) i.next());
        SubjectHierarchy.INSTANCE.removeAll();
        Project.currentProject = currentProject;
        if (currentProject != null) {
            currentProject.load();
            RecentProjects.INSTANCE.add(currentProject);
        }
    }

    public Project(File file) {
        super(file);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (file != null)
            return obj instanceof Project && file.equals(((Project) obj).file);
        else
            return obj instanceof Project && ((Project) obj).file == null;
    }

    protected Document getDocument() {
        final Element root = new Element(ROOT);
        storeTypes(root);
        storeSubjectTree(root);
        storeConnections(root);
        return new Document(root);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return file.hashCode();
    }

    protected void load(Element root) {
        loadSubjectTree(root);
        loadTypes(root);
        loadConnections(root);
    }

    private static void loadConnections(Element parent) {
        final Elements elements = parent.getChildElements(RV_CONNECTION);
        for (int i = elements.size(); i != 0;) {
            final Element element = elements.get(--i);
            final String description = getString(element, RV_DESCRIPTION);
            final String service = getString(element, RV_SERVICE);
            final String network = getString(element, RV_NETWORK);
            final String daemon = getString(element, RV_DAEMON);
            final RvConnection connection = RvConnection.createConnection(service, network, daemon);
            connection.setDescription(description);
            final Elements subjects = element.getChildElements(SUBJECT);
            for (int j = subjects.size(); j != 0;)
                connection.addSubject(subjects.get(--j).getValue());
            connection.start();
        }
    }
    
    private static void loadSubjectTree(Element parent) {
        final JTree tree = UIManager.INSTANCE.getSubjectExplorer();
        final Element subjectsRoot = parent.getFirstChildElement(SUBJECTS);
        if (subjectsRoot == null) return;
        final Elements subjects = subjectsRoot.getChildElements(SUBJECT);
        for (int i = subjects.size(); i != 0;) {
            final Element subject = subjects.get(--i);
            final SubjectElement node = SubjectHierarchy.INSTANCE.getSubjectElement(subject.getValue().trim());
            node.setSelected(getBoolean(subject, SUBJECT_SELECTED, true));
            if (getBoolean(subject, SUBJECT_EXPANDED, false))
                tree.expandPath(new TreePath(node.getPath()));
        }
    }
    
    private static void loadTypes(Element parent) {
        final Elements types = parent.getChildElements(TYPE);
        if (types.size() > 0)
            // If there are types defined in the project then use them.
            RecordType.clear();
        else
            // Otherwise add in the default set of types.
            RecordType.reset();
        for (int i = types.size(); i != 0;) {
            try {
                final Element typeElt = types.get(--i);
                final String name = getString(typeElt, TYPE_NAME);
                final Element colourElt = typeElt.getFirstChildElement(COLOUR);
                // Using -1 for the deefault will cause no colour to be set if
                // any component is missing or corrupted.
                final int r = getInteger(colourElt, COLOUR_RED, -1);
                final int g = getInteger(colourElt, COLOUR_GREEN, -1);
                final int b = getInteger(colourElt, COLOUR_BLUE, -1);
                final Color colour = new Color(r, g, b);
                final Element matcherElt = typeElt.getFirstChildElement(TYPE_MATCHER);
                final String matcherName = getString(matcherElt, TYPE_MATCHER_NAME);
                final String matcherValue = getString(matcherElt, TYPE_MATCHER_VALUE);
                final Matcher matcher = RecordMatcher.getMatcher(matcherName, matcherValue);
                final RecordType type = RecordType.createType(name, colour, matcher);
                type.setSelected(getBoolean(typeElt, TYPE_SELECTED, true));
            } catch (Exception ignored) {
                // Intentionally ignored.
            }
        }
    }

    /**
     * Resets the state of the subject explorer tree.
     * <p>
     * Whether there was a saved configuration or not, this method will always
     * reset the state of the subject explorer tree.
     */
    protected void postDeleteHook() {
        final JTree tree = UIManager.INSTANCE.getSubjectExplorer();
        final SubjectHierarchy model = (SubjectHierarchy) tree.getModel();
        // Collapse everything except the root node.
        for (int i = tree.getRowCount() - 1; i != 0; --i)
            tree.collapseRow(i);
        final Enumeration nodes = ((DefaultMutableTreeNode) model.getRoot()).breadthFirstEnumeration();
        while (nodes.hasMoreElements())
            ((SubjectElement) nodes.nextElement()).setSelected(true);
    }

    private static void storeConnections(Element parent) {
        for (final Iterator i = RvConnection.allConnections().iterator(); i.hasNext(); ) {
            final RvConnection connection = (RvConnection) i.next();
            final Element element = appendElement(parent, RV_CONNECTION);
            setString(element, RV_DESCRIPTION, connection.getDescription());
            setString(element, RV_SERVICE, connection.getService());
            setString(element, RV_NETWORK, connection.getNetwork());
            setString(element, RV_DAEMON, connection.getDaemon());
            for (final Iterator j = connection.getSubjects().iterator(); j.hasNext(); )
                setString(element, SUBJECT, (String) j.next());
        }
    }

    private static void storeSubjectTree(Element parent) {
        final JTree tree = UIManager.INSTANCE.getSubjectExplorer();
        final SubjectHierarchy model = (SubjectHierarchy) tree.getModel();
        final Element subjects = appendElement(parent, SUBJECTS);
        final Enumeration nodes = ((DefaultMutableTreeNode) model.getRoot()).breadthFirstEnumeration();
        final StringBuffer buffer = new StringBuffer();
        // Skip the root node, which does not represent a subject name element.
        nodes.nextElement();
        while (nodes.hasMoreElements()) {
            final SubjectElement node = (SubjectElement) nodes.nextElement();
            final TreePath path = new TreePath(node.getPath());
            final Element subject = setString(subjects, SUBJECT, pathToString(path, buffer).toString());
            setBoolean(subject, SUBJECT_EXPANDED, tree.isExpanded(path));
            setBoolean(subject, SUBJECT_SELECTED, node.isSelected());
        }
    }

    private static void storeTypes(Element parent) {
        final Iterator i = RecordType.getAllMessageTypes().iterator();
        while (i.hasNext()) {
            final RecordType type = (RecordType) i.next();
            if (RecordType.DEFAULT_TYPE.equals(type)) continue;
            final Element typeElt = appendElement(parent, TYPE);
            setString(typeElt, TYPE_NAME, type.getName());
            setBoolean(typeElt, TYPE_SELECTED, type.isSelected());
            final RecordMatcher.NameValueMatcher matcher = (NameValueMatcher) type.getMatcher();
            final Element matcherElt = appendElement(typeElt, TYPE_MATCHER);
            setString(matcherElt, TYPE_MATCHER_NAME, matcher.getName());
            setString(matcherElt, TYPE_MATCHER_VALUE, matcher.getValue());
            final Color colour = type.getColor();
            final Element colourElt = appendElement(typeElt, COLOUR);
            setInteger(colourElt, COLOUR_RED, colour.getRed());
            setInteger(colourElt, COLOUR_GREEN, colour.getGreen());
            setInteger(colourElt, COLOUR_BLUE, colour.getBlue());
        }
    }

}
