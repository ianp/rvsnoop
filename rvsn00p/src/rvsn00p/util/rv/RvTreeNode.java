//:File:    RvTreeNode.java
//:Created: Dec 12, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import nu.xom.Builder;
import nu.xom.Document;
import rvsn00p.IOUtils;
import rvsn00p.InterningTrimmingNodeFactory;
import rvsn00p.ui.UIUtils;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;
import com.tibco.tibrv.TibrvXml;

/**
 * A {@link TreeNode} that wraps a Rendezvous message field.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public final class RvTreeNode implements TreeNode {

    private static final RvTreeNode[] NO_CHILDREN = new RvTreeNode[0];
    
    TreeNode[] children;
    
    private final TibrvMsgField field;
    
    private final TreeNode parent;
    
    /**
     * @param parent The parent of this node.
     * @param field The field represented by this node.
     */
    public RvTreeNode(TreeNode parent, TibrvMsgField field) {
        super();
        this.field = field;
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    public Enumeration children() {
        if (children == null)
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
        if (field.type == TibrvMsg.XML) {
            final InputStream stream = new ByteArrayInputStream(((TibrvXml) field.data).getBytes());
            final Builder builder = new Builder(false, new InterningTrimmingNodeFactory());
            try {
                final Document doc = builder.build(stream).getDocument();
                final int numChildren = doc.getChildCount();
                children = new TreeNode[numChildren];
                for (int i = 0; i < numChildren; ++i)
                    children[i] = new XomTreeNode(this, doc.getChild(i));
            } catch (Exception e) {
                // What to do here?
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } else if (field.type == TibrvMsg.MSG) {
            final TibrvMsg msg = (TibrvMsg) field.data;
            final int numChildren = msg.getNumFields();
            children = new RvTreeNode[numChildren];
            try {
                for (int i = 0; i < numChildren; ++i)
                    children[i] = new RvTreeNode(this, msg.getFieldByIndex(i));
            } catch (TibrvException e) {
                UIUtils.showTibrvException("Error reading field", e);
            }
        } else {
            children = NO_CHILDREN;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren() {
        return field.type == TibrvMsg.MSG || field.type == TibrvMsg.XML;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public TreeNode getChildAt(int childIndex) {
        if (children == null)
            fillInChildren();
        return children[childIndex];
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public int getChildCount() {
        if (children == null)
            fillInChildren();
        return children.length;
    }

    public Object getFieldData() {
        return field.data;
    }
    
    public int getFieldIdentifier() {
        return field.id;
    }
    
    public String getFieldName() {
        return field.name;
    }
    
    public short getFieldType() {
        return field.type;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    public int getIndex(TreeNode node) {
        if (children == null)
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

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public boolean isLeaf() {
        return field.type != TibrvMsg.MSG && field.type != TibrvMsg.XML;
    }
}
