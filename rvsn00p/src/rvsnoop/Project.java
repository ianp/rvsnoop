/*
 * Class:     Project
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.io.File;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.rvsnoop.Connections;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import rvsnoop.ui.UIManager;

/**
 * Handles all of the project specific settings.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class Project extends XMLConfigFile {

    private static Project currentProject;
    private static final String ROOT = "rvsnoopProject";
    private static final String SUBJECT = "subject";
    private static final String SUBJECT_EXPANDED = "expanded";
    private static final String SUBJECT_NAME = "name";
    private static final String SUBJECT_SELECTED = "selected";
    private static final String SUBJECTS = "subjects";

    /**
     * @return Returns the currentProject.
     */
    public static Project getCurrentProject() {
        return currentProject;
    }

    /**
     * @param currentProject The currentProject to set.
     */
    public static void setCurrentProject(Project currentProject) {
        Connections.getInstance().clear();
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

    public static void loadConnections(Element parent) {
        final Connections connections = Connections.getInstance();
        final Elements elements = parent.getChildElements(RvConnection.XML_ELEMENT, RvConnection.XML_NS);
        for (int i = 0, imax = elements.size(); i < imax; ++i) {
            final RvConnection conn = RvConnection.fromXml(elements.get(i));
            connections.add(conn);
            conn.start();
        }
    }

    private static void loadSubjectTree(Element parent) {
        final JTree tree = UIManager.INSTANCE.getSubjectExplorer();
        final Element subjectsRoot = parent.getFirstChildElement(SUBJECTS);
        if (subjectsRoot == null) return;
        final SubjectElement root = (SubjectElement) SubjectHierarchy.INSTANCE.getRoot();
        final Elements subjects = subjectsRoot.getChildElements(SUBJECT);
        for (int i = subjects.size(); i != 0;) {
            loadSubjectTreeElement(subjects.get(--i), root, tree);
        }
    }

    private static void loadSubjectTreeElement(Element xmlElement, SubjectElement parent, JTree tree) {
        final String name = xmlElement.getAttributeValue(SUBJECT_NAME);
        final boolean selected = getBoolean(xmlElement, SUBJECT_SELECTED, true);
        final SubjectElement node = SubjectHierarchy.INSTANCE.getSubjectElement(parent, name, selected);
        if (getBoolean(xmlElement, SUBJECT_EXPANDED, true))
            tree.expandPath(new TreePath(node.getPath()));
        final Elements children = xmlElement.getChildElements(SUBJECT);
        for (int i = children.size(); i != 0;) {
            loadSubjectTreeElement(children.get(--i), node, tree);
        }
    }

    private static void loadTypes(Element parent) {
        final Elements elements = parent.getChildElements(RecordType.XML_ELEMENT, RecordType.XML_NS);
        final RecordTypes types = RecordTypes.getInstance();
        // If there are types defined in the project then use them.
        // Otherwise add in the default set of types.
        if (elements.size() == 0) {
            types.reset();
        } else {
            types.clear();
            for (int i = 0, imax = elements.size(); i < imax; ++i) {
                try {
                    RecordType.fromXml(elements.get(i));
                } catch (Exception e) {
                    // Intentionally ignored.
                }
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

    public static void storeConnections(Element parent) {
        final RvConnection[] connections = Connections.getInstance().toArray();
        for (int i = 0, imax = connections.length; i < imax; ++i)
            parent.appendChild(connections[i].toXml());
    }

    private static void storeSubjectTree(Element parent) {
        final JTree tree = UIManager.INSTANCE.getSubjectExplorer();
        final SubjectHierarchy model = (SubjectHierarchy) tree.getModel();
        final Element subjects = appendElement(parent, SUBJECTS);
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        for (final Enumeration e = root.children(); e.hasMoreElements(); ) {
            storeSubjectTreeElement(subjects, (SubjectElement) e.nextElement());
        }
    }

    private static void storeSubjectTreeElement(Element parent, SubjectElement node) {
        final JTree tree = UIManager.INSTANCE.getSubjectExplorer();
        final Element child = new Element(SUBJECT);
        final TreePath path = new TreePath(node.getPath());
        child.addAttribute(new Attribute(SUBJECT_NAME, node.getElementName()));
        setBoolean(child, SUBJECT_EXPANDED, tree.isExpanded(path));
        setBoolean(child, SUBJECT_SELECTED, node.isSelected());
        parent.appendChild(child);
        for (final Enumeration e = node.children(); e.hasMoreElements(); ) {
            storeSubjectTreeElement(child, (SubjectElement) e.nextElement());
        }
    }

    private static void storeTypes(Element parent) {
        final RecordType[] types = RecordTypes.getInstance().getAllTypes();
        for (int i = 0, imax = types.length; i < imax; ++i)
            parent.appendChild(types[i].toXml());
    }

}
