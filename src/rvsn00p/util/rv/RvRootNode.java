//:File:    RvRootNode.java
//:Created: Dec 12, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

/**
 * A {@link TreeNode} that wraps a Rendezvous message.
 * <p>
 * This class is designed to act as the root node in a tree model.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public class RvRootNode implements TreeNode {

    final RvTreeNode[] children;
    
    private final TibrvMsg message;
    
    /**
     * @param message The message represented by this node.
     */
    public RvRootNode(TibrvMsg message) {
        children = new RvTreeNode[message.getNumFields()];
        this.message = message;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    public Enumeration children() {
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

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren() {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public TreeNode getChildAt(int childIndex) {
        if (children[childIndex] == null) {
            try {
                TibrvMsgField f = message.getFieldByIndex(childIndex);
                children[childIndex] = new RvTreeNode(this, f);
            } catch (TibrvException e) {
                RvUtils.showTibrvException(null, "Error reading field", e);
            }
        }
        return children[childIndex];
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public int getChildCount() {
        return children.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    public int getIndex(TreeNode node) {
        for (int i = 0, imax = children.length; i < imax; ++i)
            if (children[i].equals(node))
                return i;
        return -1;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getParent()
     */
    public TreeNode getParent() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public boolean isLeaf() {
        return false;
    }

}
