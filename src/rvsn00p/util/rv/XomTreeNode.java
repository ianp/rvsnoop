//:File:    XomTreeNode.java
//:Created: Dec 13, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import nu.xom.Node;
import nu.xom.ParentNode;

/**
 * A {@link TreeNode} that wraps XOM node objects to allow for visual display.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public class XomTreeNode implements TreeNode {
    
    private static final TreeNode[] NO_CHILDREN = new TreeNode[0];

    TreeNode[] children;
    
    private final Node node;
    
    private final TreeNode parent;
    
    public XomTreeNode(TreeNode parent, Node node) {
        this.parent = parent;
        this.node = node;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    public Enumeration children() {
        if (children == null && getAllowsChildren())
            fillInChildren();
        return new Enumeration() {
            int index = 0;
            public boolean hasMoreElements() {
                return index < children.length;
            }
            public Object nextElement() {
                return children[index++];
            }
        };
    }

    private void fillInChildren() {
        ParentNode pn = (ParentNode) node;
        int numChildren = pn.getChildCount();
        if (numChildren == 0) {
            children = NO_CHILDREN;
            return;
        }
        children = new TreeNode[numChildren];
        for (int i = 0; i < numChildren; ++i)
            children[i] = new XomTreeNode(this, pn.getChild(i));
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren() {
        return node instanceof ParentNode;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public TreeNode getChildAt(int childIndex) {
        if (children == null && getAllowsChildren())
            fillInChildren();
        return children[childIndex];
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public int getChildCount() {
        return getAllowsChildren() ? ((ParentNode) node).getChildCount() : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    public int getIndex(TreeNode node) {
        if (children == null && getAllowsChildren())
            fillInChildren();
        for (int i = 0, imax = children.length; i < imax; ++i)
            if (children[i].equals(node))
                return i;
        return -1;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getParent()
     */
    public TreeNode getParent() {
        return parent;
    }
    
    public Node getXomNode() {
        return node;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public boolean isLeaf() {
        return !(node instanceof ParentNode);
    }

}
