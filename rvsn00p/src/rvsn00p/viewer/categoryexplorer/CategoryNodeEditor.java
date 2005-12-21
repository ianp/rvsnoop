//:File:    CategoryNodeEditor.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer.categoryexplorer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * CategoryNodeEditor
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class CategoryNodeEditor extends CategoryAbstractCellEditor {
    protected CategoryNodeEditorRenderer _renderer;
    protected CategoryNode _lastEditedNode;
    protected JCheckBox _checkBox;
    protected CategoryExplorerModel _categoryModel;
    protected JTree _tree;

    public CategoryNodeEditor(CategoryExplorerModel model) {
        _renderer = new CategoryNodeEditorRenderer();
        _checkBox = _renderer.getCheckBox();
        _categoryModel = model;

        _checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _categoryModel.update(_lastEditedNode, _checkBox.isSelected());
                stopCellEditing();
            }
        });

        _renderer.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                    showPopup(_lastEditedNode, e.getX(), e.getY());
                }
                stopCellEditing();
            }
        });
    }

    public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                boolean selected, boolean expanded,
                                                boolean leaf, int row) {
        _lastEditedNode = (CategoryNode) value;
        _tree = tree;

        return _renderer.getTreeCellRendererComponent(tree,
                                                      value, selected, expanded,
                                                      leaf, row, true);
        // hasFocus ignored
    }

    public Object getCellEditorValue() {
        return _lastEditedNode.getUserObject();
    }
    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    protected JMenuItem createPropertiesMenuItem(final CategoryNode node) {
        JMenuItem result = new JMenuItem("Properties");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPropertiesDialog(node);
            }
        });
        return result;
    }

    protected void showPropertiesDialog(CategoryNode node) {
        JOptionPane.showMessageDialog(
                _tree,
                getDisplayedProperties(node),
                "Subject Properties: " + node.getTitle(),
                JOptionPane.PLAIN_MESSAGE
        );
    }

    protected Object getDisplayedProperties(CategoryNode node) {
        ArrayList result = new ArrayList();
        result.add("Subject: " + node.getTitle());
        if (node.hasFatalRecords()) {
            result.add("Contains at least one fatal LogRecord.");
        }
        if (node.hasFatalChildren()) {
            result.add("Contains descendants with a fatal LogRecord.");
        }
        result.add("LogRecords in this subject path alone: " +
                   node.getNumberOfContainedRecords());
        result.add("LogRecords in descendant subject path: " +
                   node.getNumberOfRecordsFromChildren());
        result.add("LogRecords in this subject path including descendants: " +
                   node.getTotalNumberOfRecords());
        return result.toArray();
    }

    protected void showPopup(CategoryNode node, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setSize(150, 400);
        //
        // Configure the Popup
        //
        if (node.getParent() == null) {
            popup.add(createRemoveMenuItem());
            popup.addSeparator();
        }
        popup.add(createSelectDescendantsMenuItem(node));
        popup.add(createUnselectDescendantsMenuItem(node));
        popup.addSeparator();
        popup.add(createExpandMenuItem(node));
        popup.add(createCollapseMenuItem(node));
        popup.addSeparator();
        popup.add(createPropertiesMenuItem(node));
        popup.show(_renderer, x, y);
    }

    protected JMenuItem createSelectDescendantsMenuItem(final CategoryNode node) {
        JMenuItem selectDescendants =
                new JMenuItem("Select All Descendant Subjects");
        selectDescendants.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _categoryModel.setDescendantSelection(node, true);
                    }
                }
        );
        return selectDescendants;
    }

    protected JMenuItem createUnselectDescendantsMenuItem(final CategoryNode node) {
        JMenuItem unselectDescendants =
                new JMenuItem("Deselect All Descendant Subjects");
        unselectDescendants.addActionListener(

                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _categoryModel.setDescendantSelection(node, false);
                    }
                }

        );
        return unselectDescendants;
    }

    protected JMenuItem createExpandMenuItem(final CategoryNode node) {
        JMenuItem result = new JMenuItem("Expand All Descendant Subjects");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expandDescendants(node);
            }
        });
        return result;
    }

    protected JMenuItem createCollapseMenuItem(final CategoryNode node) {
        JMenuItem result = new JMenuItem("Collapse All Descendant Subjects");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                collapseDescendants(node);
            }
        });
        return result;
    }

    /**
     * This featured was moved from the RvSnooperGUI class
     * to the CategoryNodeExplorer so that the Category tree
     * could be pruned from the Category Explorer popup menu.
     * This menu option only appears when a user right clicks on
     * the Category parent node.
     *
     * See removeUnusedNodes()
     */
    protected JMenuItem createRemoveMenuItem() {
        JMenuItem result = new JMenuItem("Remove All Empty Subjects");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                while (removeUnusedNodes() > 0) {
                    // removeUnusedNodes() called for side effect.
                }
            }
        });
        return result;
    }

    protected void expandDescendants(CategoryNode node) {
        Enumeration descendants = node.depthFirstEnumeration();
        CategoryNode current;
        while (descendants.hasMoreElements()) {
            current = (CategoryNode) descendants.nextElement();
            expand(current);
        }
    }

    protected void collapseDescendants(CategoryNode node) {
        Enumeration descendants = node.depthFirstEnumeration();
        CategoryNode current;
        while (descendants.hasMoreElements()) {
            current = (CategoryNode) descendants.nextElement();
            collapse(current);
        }
    }

    /**
     * Removes any inactive nodes from the Category tree.
     */
    protected int removeUnusedNodes() {
        int count = 0;
        CategoryNode root = _categoryModel.getRootCategoryNode();
        Enumeration e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            CategoryNode node = (CategoryNode) e.nextElement();
            if (node.isLeaf() && node.getNumberOfContainedRecords() == 0
                    && node.getParent() != null) {
                _categoryModel.removeNodeFromParent(node);
                ++count;
            }
        }

        return count;
    }

    protected void expand(CategoryNode node) {
        _tree.expandPath(getTreePath(node));
    }

    protected TreePath getTreePath(CategoryNode node) {
        return new TreePath(node.getPath());
    }

    protected void collapse(CategoryNode node) {
        _tree.collapsePath(getTreePath(node));
    }

    //-----------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

}
