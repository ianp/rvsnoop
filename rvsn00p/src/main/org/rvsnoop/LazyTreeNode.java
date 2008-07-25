/*
 * Class:     LazyTreeNode
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.awt.Component;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

/**
 * A {@link TreeNode} that lazily created it's children.
 * <p>
 * This class also supports providing icons and text as suitable for use in a renderer.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public abstract class LazyTreeNode implements TreeNode {

    public static class Renderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = -1587671692812026948L;
        public Renderer() {
            super();
        }
        /* (non-Javadoc)
         * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (!(value instanceof LazyTreeNode)) return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            final LazyTreeNode treeNode = (LazyTreeNode) value;
            final JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            label.setText(treeNode.getText());
            label.setIcon(treeNode.getIcon());
            label.setToolTipText(treeNode.getTooltip());
            return label;
        }

    }

    private List<TreeNode> children;

    private final TreeNode parent;

    /**
     * @param parent The parent of this node.
     */
    protected LazyTreeNode(TreeNode parent) {
        super();
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    public final Enumeration<TreeNode> children() {
        ensureChildrenCreated();
        return Collections.enumeration(children);
    }

    /**
     * Concrete subclasses should use this hook to create the list of children.
     *
     * @return A list of tree nodes.
     */
    protected abstract List<TreeNode> createChildren();

    private void ensureChildrenCreated() {
        if (children == null) {
            if (getAllowsChildren()) {
                children = createChildren();
            } else {
                children = Collections.emptyList();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public abstract boolean getAllowsChildren();

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public final TreeNode getChildAt(int childIndex) {
        ensureChildrenCreated();
        return (TreeNode) children.get(childIndex);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public final int getChildCount() {
        ensureChildrenCreated();
        return children.size();
    }

    /**
     * Get an icon suitable for decorating a graphical widget.
     *
     * @return The icon or <code>null</code>.
     */
    public Icon getIcon() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    public final int getIndex(TreeNode node) {
        ensureChildrenCreated();
        return children.indexOf(node);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getParent()
     */
    public final TreeNode getParent() {
        return parent;
    }

    /**
     * Get a text string suitable for displaying in a graphical widget.
     *
     * @return The text string, should not be <code>null</code>.
     */
    protected abstract String getText();

    /**
     * Get a text string suitable for displaying in a graphical widget.
     *
     * @return The text string, may be <code>null</code>.
     */
    protected String getTooltip() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public final boolean isLeaf() {
        return !getAllowsChildren();
    }
}
