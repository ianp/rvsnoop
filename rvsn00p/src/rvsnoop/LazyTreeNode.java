//:File:    LazyTreeNode.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

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
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (!(value instanceof LazyTreeNode)) return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            final LazyTreeNode treeNode = (LazyTreeNode) value;
            final JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            label.setText(treeNode.getText());
            label.setIcon(treeNode.getIcon());
            return label;
        }
        
    }
    
    private List children;
    
    private final TreeNode parent;
    
    /**
     * @param parent The parent of this node.
     */
    public LazyTreeNode(TreeNode parent) {
        super();
        this.parent = parent;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    public final Enumeration children() {
        if (children == null)
            children = getAllowsChildren() ? createChildren() : Collections.EMPTY_LIST;
        return Collections.enumeration(children);
    }
    
    /**
     * Concrete subclasses should use this hook to create the list of children.
     * 
     * @return A list of tree nodes.
     */
    protected abstract List createChildren();
    
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public abstract boolean getAllowsChildren();

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public final TreeNode getChildAt(int childIndex) {
        if (children == null)
            children = getAllowsChildren() ? createChildren() : Collections.EMPTY_LIST;
        return (TreeNode) children.get(childIndex);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public final int getChildCount() {
        if (children == null)
            children = getAllowsChildren() ? createChildren() : Collections.EMPTY_LIST;
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
        if (children == null)
            children = getAllowsChildren() ? createChildren() : Collections.EMPTY_LIST;
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
    public abstract String getText();

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public final boolean isLeaf() {
        return !getAllowsChildren();
    }
}
