// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
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
 */
public abstract class LazyTreeNode implements TreeNode {

    public static class Renderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = -1587671692812026948L;
        public Renderer() {
            super();
        }

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

    public abstract boolean getAllowsChildren();

    public final TreeNode getChildAt(int childIndex) {
        ensureChildrenCreated();
        return children.get(childIndex);
    }

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

    public final int getIndex(TreeNode node) {
        ensureChildrenCreated();
        return children.indexOf(node);
    }

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

    public final boolean isLeaf() {
        return !getAllowsChildren();
    }

}
