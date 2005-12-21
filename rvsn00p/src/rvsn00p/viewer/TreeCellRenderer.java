//:File:    TreeCellRenderer.java
//:Created: Sep 27, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.tibco.tibrv.TibrvIPAddr;
import com.tibco.tibrv.TibrvIPPort;
import com.tibco.tibrv.TibrvMsg;

import rvsn00p.util.rv.RvRootNode;
import rvsn00p.util.rv.RvTreeNode;
import rvsn00p.util.rv.XomTreeNode;

import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ProcessingInstruction;
import nu.xom.Text;

/**
 * A custom renderer for tree nodes containing Rendezvous message fields and XML nodes.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
final class TreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static final long serialVersionUID = -840075635921919631L;

    public TreeCellRenderer() {
        super();
    }

    private String getRvTreeCellValue(RvTreeNode node) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(node.getFieldName()).append(" = ");
        short type = node.getFieldType();
        if (type >= TibrvMsg.USER_FIRST && type <= TibrvMsg.USER_LAST)
            return buffer.append(((byte[]) node.getFieldData()).length).append(" bytes of custom user data.").toString();
        switch (type) {
        case TibrvMsg.ENCRYPTED:
            setIcon(Icons.RV_FIELD);
            return buffer.append(((byte[]) node.getFieldData()).length).append(" bytes of encrypted data.").toString();
        case TibrvMsg.F32ARRAY:
            setIcon(Icons.RV_FIELD);
            return buffer.append("Array of ").append(((Float[]) node.getFieldData()).length).append(" single precision floating point values.").toString();
        case TibrvMsg.F64ARRAY:
            setIcon(Icons.RV_FIELD);
            return buffer.append("Array of ").append(((Double[]) node.getFieldData()).length).append(" double precision floating point values.").toString();
        case TibrvMsg.I8ARRAY:
            setIcon(Icons.RV_FIELD);
            return buffer.append("Array of ").append(((Integer[]) node.getFieldData()).length).append(" 8-bit integer values.").toString();
        case TibrvMsg.I16ARRAY:
            setIcon(Icons.RV_FIELD);
            return buffer.append("Array of ").append(((Long[]) node.getFieldData()).length).append(" 16-bit integer values.").toString();
        case TibrvMsg.I32ARRAY:
            setIcon(Icons.RV_FIELD);
            return buffer.append("Array of ").append(((Integer[]) node.getFieldData()).length).append(" 32-bit integer values.").toString();
        case TibrvMsg.I64ARRAY:
            setIcon(Icons.RV_FIELD);
            return buffer.append("Array of ").append(((Long[]) node.getFieldData()).length).append(" 64-bit integer values.").toString();
        case TibrvMsg.IPADDR32:
            setIcon(Icons.RV_FIELD);
            return buffer.append("IP Address: ").append(((TibrvIPAddr) node.getFieldData()).getAsString()).toString();
        case TibrvMsg.IPPORT16:
            setIcon(Icons.RV_FIELD);
            return buffer.append("IP Port: ").append(Integer.toString(((TibrvIPPort) node.getFieldData()).getPort())).toString();
        case TibrvMsg.MSG:
            setIcon(Icons.RV_MESSAGE);
            return buffer.append("Nested Message").toString();
        case TibrvMsg.OPAQUE:
            setIcon(Icons.RV_FIELD);
            return buffer.append(((byte[]) node.getFieldData()).length).append(" bytes of opaque data.").toString();
        default:
            setIcon(Icons.RV_FIELD);
            return buffer.append(node.getFieldData()).toString();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // Set up selected highlighting, etc.
        super.getTreeCellRendererComponent(tree, "", sel, expanded, leaf, row, hasFocus);
        if (value instanceof RvTreeNode) {
            setText(getRvTreeCellValue((RvTreeNode) value));
        } else if (value instanceof XomTreeNode) {
            setText(getXomTreeCellValue((XomTreeNode) value));
        } else if (value instanceof RvRootNode) {
            setIcon(Icons.RV_MESSAGE);
            setText("Rendezvous Message");
        }
        // TODO: Add appropriate icons for all node types and remove the non I18N text markers.
        return this;
    }
    
    private String getXomTreeCellValue(XomTreeNode node) {
        Node value = node.getXomNode();
        if (value instanceof Attribute) {
            setIcon(Icons.XML_ATTRIBUTE);
            return ((Attribute) value).getLocalName();
        } else if (value instanceof Element) {
            setIcon(Icons.XML_ELEMENT);
            return ((Element) value).getLocalName();
        } else if (value instanceof Comment) {
            setIcon(null);
            return "<!-- " + ((Comment) value).getValue(); //$NON-NLS-1$
        } else if (value instanceof Document) {
            setIcon(null);
            return "XML Document"; //$NON-NLS-1$
        } else if (value instanceof DocType) {
            setIcon(null);
            return "<!DocType " + ((DocType) value).getRootElementName(); //$NON-NLS-1$
        } else if (value instanceof ProcessingInstruction) {
            setIcon(null);
            return "<? " + ((ProcessingInstruction) value).getTarget();
        } else if (value instanceof Text) {
            setIcon(null);
            return ((Text) value).getValue(); //$NON-NLS-1$
        } else {
            setIcon(null);
            return "[Unsupported node type!]"; //$NON-NLS-1$
        }
    }

}
