//:File:    SubjectHierarchy.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * A hierarchy of rendezvous subjects.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor
 * 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
// Class provides static instance instead of getInstance() method.
// @PMD:REVIEWED:MissingStaticMethodInNonInstantiatableClass: by ianp on 1/4/06 2:03 PM
public final class SubjectHierarchy extends DefaultTreeModel {

    private static class SubjectHierarchyMatcherEditor extends AbstractMatcherEditor {
        SubjectHierarchyMatcherEditor() {
            super();
        }
        public Matcher getMatcher() {
            return new Matcher() {
                public boolean matches(Object item) {
                    SubjectElement elt = ((Record) item).getSubject();
                    if (!elt.isSelected()) return false;
                    while ((elt = ((SubjectElement) elt.getParent())) != null)
                        if (!elt.isSelected()) return false;
                    return true;
                }
            };
        }
        void update(boolean selected) {
            if (selected)
                fireRelaxed(getMatcher());
            else
                fireConstrained(getMatcher());
        }
    }

    public static final SubjectHierarchy INSTANCE = new SubjectHierarchy();

    private static final long serialVersionUID = -3629858078509052804L;

    private static final Pattern SPLITTER = Pattern.compile("\\.");

    private final SubjectHierarchyMatcherEditor matcherEditor = new SubjectHierarchyMatcherEditor();

    private SubjectElement noSubjectElement;

    private SubjectHierarchy() {
        super(new SubjectElement());
    }

    public void addRecord(Record record) {
        final SubjectElement element = record.getSubject();
        element.incNumRecordsHere();
        if (RecordTypes.ERROR.matches(record))
            element.setErrorHere();
        final TreeNode[] nodes = element.getPath();
        for (int i = 0, imax = nodes.length; i < imax; ++i)
            nodeChanged(nodes[i]);
    }

    public MatcherEditor getMatcherEditor() {
        return matcherEditor;
    }

    private synchronized SubjectElement getNoSubjectElement() {
        if (noSubjectElement == null)
            noSubjectElement = insertNewChild((SubjectElement) getRoot(), "[No Subject!]", 0, true);
        return noSubjectElement;
    }

    /**
     * Convert a subject string into a subject element object.
     * <p>
     * The element will be created if it does not already exists.
     *
     * @param subject The subject as a string.
     * @return The <code>SubjectElement</code> representing the subject.
     */
    public SubjectElement getSubjectElement(String subject) {
        if (subject == null || subject.length() == 0)
            return getNoSubjectElement();
        SubjectElement current = (SubjectElement) getRoot();
        final String[] subjectElts = SPLITTER.split(subject);
        final String[] pathElts = new String[subjectElts.length + 1];
        pathElts[0] = (String) current.getUserObject();
        System.arraycopy(subjectElts, 0, pathElts, 1, subjectElts.length);
        boolean isSelected = true;
        // Skipping the root node...
        for (int i = 1, imax = pathElts.length; i < imax; ++i) {
            final String element = pathElts[i];
            final int numChildren = current.getChildCount();
            // If there are no children just add a child and continue.
            if (numChildren == 0) {
                current = insertNewChild(current, element, 0, isSelected);
            } else {
                current = getSubjectElement(current, element, isSelected);
            }
            isSelected = current.isSelected();
        }
        return current;
    }

    /**
     * Get a subject element given a parent element.
     * <p>
     * This method will return an existing element if there is one, or will create
     * a new element if one does not currently exist.
     *
     * @param parent The parent of the new element.
     * @param name The name of the new element.
     * @param selected Should the element be selected or not.
     * @return The subject element.
     */
    public SubjectElement getSubjectElement(SubjectElement parent, String name, boolean selected) {
        final int numChildren = parent.getChildCount();
        for (int j = 0; j < numChildren; ++j) {
            final SubjectElement child = (SubjectElement) parent.getChildAt(j);
            final int compared = name.compareTo(child.getElementName());
            // If this child already exists scan it.
            if (compared == 0) {
                return child;
            } else if (compared < 0) {
                // If we have found a child which should be sorted after this then
                // we should insert the new child here and immediately scan it.
                return insertNewChild(parent, name, j, selected && parent.isSelected());
            }
        }
        // If we have reached the end of the children with no match then
        // append a new child and scan it.
        return insertNewChild(parent, name, numChildren, selected && parent.isSelected());
    }

    private SubjectElement insertNewChild(final SubjectElement current,
                                          final String element, final int index, final boolean isSelected) {
        final SubjectElement newChild = new SubjectElement(current, element);
        newChild.setSelected(isSelected);
        insertNodeInto(newChild, current, index);
        return newChild;
    }

    public void removeAll() {
        final SubjectElement root = (SubjectElement) this.root;
        root.removeAllChildren();
    }

    /**
     * Resets all counters and error flags in the hierarchy.
     */
    public void reset() {
        final Enumeration nodes = ((DefaultMutableTreeNode) getRoot()).depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            final SubjectElement current = (SubjectElement) nodes.nextElement();
            current.reset();
            nodeChanged(current);
        }
    }

    /**
     * Set the selction on a given node and all descendants.
     *
     * @param node The root of the subtree to set.
     * @param selected The new selection value.
     */
    public void setAllSelected(SubjectElement node, boolean selected) {
        final Enumeration descendants = node.depthFirstEnumeration();
        while (descendants.hasMoreElements())
            updateElement((SubjectElement) descendants.nextElement(), selected);
        matcherEditor.update(selected);
    }

    /**
     * Set the selection on a given node.
     * <p>
     * If <code>selection</code> is <code>false</code> then this also
     * unselects all descendants, if it is <code>true</code> then this also
     * selects the path to root.
     *
     * @param node The node to select from.
     * @param selected the new selection value.
     */
    public void setSelected(SubjectElement node, boolean selected) {
        if (node.isSelected() == selected) return;
        // Select parents or deselect children?
        if (selected) {
            final TreeNode[] nodes = node.getPath();
            // Skip the 0-index root node.
            for (int i = nodes.length - 1; i != 0; --i)
                updateElement((SubjectElement) nodes[i], selected);
            matcherEditor.update(selected);
        } else {
            setAllSelected(node, false);
        }
    }

    private void updateElement(final SubjectElement current, boolean selected) {
        if (current.isSelected() != selected) {
            current.setSelected(selected);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    nodeChanged(current);
                }
            });
        }
    }

}
