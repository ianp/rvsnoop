//:File:    RvFieldTreeNode.java
//:Created: Dec 12, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvIPAddr;
import com.tibco.tibrv.TibrvIPPort;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;
import com.tibco.tibrv.TibrvXml;
import nu.xom.Builder;
import nu.xom.Document;
import rvsnoop.ui.Icons;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link TreeNode} that wraps a Rendezvous message field.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
final class RvFieldTreeNode extends LazyTreeNode {

    private static String ARRAY_OF = "Array of ";
    
    private static final Logger logger = Logger.getLogger(RvFieldTreeNode.class);
    
    private final TibrvMsgField field;

    /**
     * @param parent The parent of this node.
     * @param field The field represented by this node.
     */
    public RvFieldTreeNode(TreeNode parent, TibrvMsgField field) {
        super(parent);
        this.field = field;
    }

    protected List createChildren() {
        List children = null;
        if (field.type == TibrvMsg.XML) {
            final InputStream stream = new ByteArrayInputStream(((TibrvXml) field.data).getBytes());
            children = createChildrenFromXML(new InputStreamReader(stream));
        } else if (field.type == TibrvMsg.STRING && ((String) field.data).startsWith("<?xml ")) {
            children = createChildrenFromXML(new StringReader((String) field.data));
        } else if (field.type == TibrvMsg.MSG) {
            final TibrvMsg msg = (TibrvMsg) field.data;
            final int numChildren = msg.getNumFields();
            children = new ArrayList(numChildren);
            try {
                for (int i = 0; i < numChildren; ++i)
                    children.add(new RvFieldTreeNode(this, msg.getFieldByIndex(i)));
            } catch (TibrvException e) {
                logger.error("Error reading field.", e);
            }
        }
        return children != null ? children : Collections.EMPTY_LIST;
    }

    private List createChildrenFromXML(final Reader reader) {
        List children = null;
        final Builder builder = new Builder(false, new InterningTrimmingNodeFactory());
        try {
            final Document doc = builder.build(reader).getDocument();
            final int numChildren = doc.getChildCount();
            children = new ArrayList(numChildren);
            for (int i = 0; i < numChildren; ++i)
                children.add(new XMLTreeNode(this, doc.getChild(i)));
        } catch (Exception e) {
            logger.error("Error extracting XML.", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return children;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren() {
        return field.type == TibrvMsg.MSG || field.type == TibrvMsg.XML;
    }
    
    public Icon getIcon() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(field.name).append(" = ");
        switch (field.type) {
        case TibrvMsg.ENCRYPTED:
            return Icons.RV_FIELD;
        case TibrvMsg.F32ARRAY:
            return Icons.RV_FIELD;
        case TibrvMsg.F64ARRAY:
            return Icons.RV_FIELD;
        case TibrvMsg.I8ARRAY:
            return Icons.RV_FIELD;
        case TibrvMsg.I16ARRAY:
            return Icons.RV_FIELD;
        case TibrvMsg.I32ARRAY:
            return Icons.RV_FIELD;
        case TibrvMsg.I64ARRAY:
            return Icons.RV_FIELD;
        case TibrvMsg.IPADDR32:
            return Icons.RV_FIELD;
        case TibrvMsg.IPPORT16:
            return Icons.RV_FIELD;
        case TibrvMsg.MSG:
            return Icons.RV_MESSAGE;
        case TibrvMsg.OPAQUE:
            return Icons.RV_FIELD;
        default:
            return Icons.RV_FIELD;
        }
    }
    
    public String getText() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(field.name).append(" = ");
        final short type = field.type;
        if (type >= TibrvMsg.USER_FIRST && type <= TibrvMsg.USER_LAST)
            return buffer.append(((byte[]) field.data).length).append(" bytes of custom user data.").toString();
        switch (type) {
        case TibrvMsg.ENCRYPTED:
            return buffer.append(((byte[]) field.data).length).append(" bytes of encrypted data.").toString();
        case TibrvMsg.F32ARRAY:
            return buffer.append(ARRAY_OF).append(((float[]) field.data).length).append(" single precision floating point values.").toString();
        case TibrvMsg.F64ARRAY:
            return buffer.append(ARRAY_OF).append(((double[]) field.data).length).append(" double precision floating point values.").toString();
        case TibrvMsg.I8ARRAY:
            return buffer.append(ARRAY_OF).append(((int[]) field.data).length).append(" 8-bit integer values.").toString();
        case TibrvMsg.I16ARRAY:
            return buffer.append(ARRAY_OF).append(((long[]) field.data).length).append(" 16-bit integer values.").toString();
        case TibrvMsg.I32ARRAY:
            return buffer.append(ARRAY_OF).append(((int[]) field.data).length).append(" 32-bit integer values.").toString();
        case TibrvMsg.I64ARRAY:
            return buffer.append(ARRAY_OF).append(((long[]) field.data).length).append(" 64-bit integer values.").toString();
        case TibrvMsg.IPADDR32:
            return buffer.append("IP Address: ").append(((TibrvIPAddr) field.data).getAsString()).toString();
        case TibrvMsg.IPPORT16:
            return buffer.append("IP Port: ").append(Integer.toString(((TibrvIPPort) field.data).getPort())).toString();
        case TibrvMsg.MSG:
            return buffer.append("Nested Message").toString();
        case TibrvMsg.OPAQUE:
            return buffer.append(((byte[]) field.data).length).append(" bytes of opaque data.").toString();
        default:
            return buffer.append(field.data).toString();
        }
    }

}
