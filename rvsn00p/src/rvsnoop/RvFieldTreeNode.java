//:File:    RvFieldTreeNode.java
//:Created: Dec 12, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvIPAddr;
import com.tibco.tibrv.TibrvIPPort;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;
import com.tibco.tibrv.TibrvXml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import nu.xom.Builder;
import nu.xom.Document;
import rvsnoop.ui.Icons;

/**
 * A {@link TreeNode} that wraps a Rendezvous message field.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
final class RvFieldTreeNode extends LazyTreeNode {

    private static Icon setIcon(TibrvMsgField field) {
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

    static String getFieldDescription(TibrvMsgField field) {
        final short type = field.type;
        switch (type) {
            case TibrvMsg.ENCRYPTED: return FD_ENCRYPTED.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.MSG:       return FD_MSG.format(new Object[] {field.name, Integer.toString(((TibrvMsg) field.data).getNumFields())});
            case TibrvMsg.OPAQUE:    return FD_OPAQUE.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.BOOL:      return FD_BOOL.format(new Object[] {field.name, field.data});
            case TibrvMsg.DATETIME:  return FD_DATETIME.format(new Object[] {field.name, field.data});
            case TibrvMsg.IPADDR32:  return FD_IPADDR32.format(new Object[] {field.name, ((TibrvIPAddr) field.data).getAsString()});
            case TibrvMsg.IPPORT16:  return FD_IPPORT16.format(new Object[] {field.name, Integer.toString(((TibrvIPPort) field.data).getPort())});
            case TibrvMsg.F32ARRAY:  return FD_F32ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.F64ARRAY:  return FD_F64ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.I8ARRAY:   return FD_I8ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.I16ARRAY:  return FD_I16ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.I32ARRAY:  return FD_I32ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.I64ARRAY:  return FD_I64ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.U8ARRAY:   return FD_U8ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.U16ARRAY:  return FD_U16ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.U32ARRAY:  return FD_U32ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.U64ARRAY:  return FD_U64ARRAY.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
            case TibrvMsg.F32: return FD_F32.format(new Object[] {field.name, field.data});
            case TibrvMsg.F64: return FD_F64.format(new Object[] {field.name, field.data});
            case TibrvMsg.I8:  return FD_I8.format(new Object[] {field.name, field.data});
            case TibrvMsg.I16: return FD_I16.format(new Object[] {field.name, field.data});
            case TibrvMsg.I32: return FD_I32.format(new Object[] {field.name, field.data});
            case TibrvMsg.I64: return FD_I64.format(new Object[] {field.name, field.data});
            case TibrvMsg.U8:  return FD_U8.format(new Object[] {field.name, field.data});
            case TibrvMsg.U16: return FD_U16.format(new Object[] {field.name, field.data});
            case TibrvMsg.U32: return FD_U32.format(new Object[] {field.name, field.data});
            case TibrvMsg.U64: return FD_U64.format(new Object[] {field.name, field.data});
            case TibrvMsg.XML: return FD_XML.format(new Object[] {field.name, Integer.toString(((TibrvXml) field.data).getBytes().length)});
            case TibrvMsg.STRING:
                final String s = (String) field.data;
                if (s.startsWith("<?xml ")) return FD_STRING_XML.format(new Object[] {field.name, Integer.toString(s.length())});
                if (s.length() > 60) return FD_STRING_LONG.format(new Object[] {field.name, Integer.toString(s.length()), s.substring(0, 60)});
                return FD_STRING.format(new Object[] {field.name, s});
            default: return FD_USER.format(new Object[] {field.name, Integer.toString(Array.getLength(field.data))});
        }
    }

    private static final MessageFormat FD_ENCRYPTED = new MessageFormat("{0} ({1} bytes of encrypted data)");
    private static final MessageFormat FD_F32ARRAY = new MessageFormat("{0} ({1} single precision floating point values)");
    private static final MessageFormat FD_F64ARRAY = new MessageFormat("{0} ({1} double precision floating point values)");
    private static final MessageFormat FD_I8ARRAY = new MessageFormat("{0} ({1} 8-bit signed integer values)");
    private static final MessageFormat FD_I16ARRAY = new MessageFormat("{0} ({1} 16-bit signed integer values)");
    private static final MessageFormat FD_I32ARRAY = new MessageFormat("{0} ({1} 32-bit signed integer values)");
    private static final MessageFormat FD_I64ARRAY = new MessageFormat("{0} ({1} 64-bit signed integer values)");
    private static final MessageFormat FD_U8ARRAY = new MessageFormat("{0} ({1} 8-bit unsigned integer values)");
    private static final MessageFormat FD_U16ARRAY = new MessageFormat("{0} ({1} 16-bit unsigned integer values)");
    private static final MessageFormat FD_U32ARRAY = new MessageFormat("{0} ({1} 32-bit unsigned integer values)");
    private static final MessageFormat FD_U64ARRAY = new MessageFormat("{0} ({1} 64-bit unsigned integer values)");
    private static final MessageFormat FD_IPADDR32 = new MessageFormat("{0} (IP Address: {1})");
    private static final MessageFormat FD_IPPORT16 = new MessageFormat("{0} (IP Port Number: {1})");
    private static final MessageFormat FD_F32 = new MessageFormat("{0} ({1}, with single/32-bit precision)");
    private static final MessageFormat FD_F64 = new MessageFormat("{0} ({1}, with double/64-bit precision)");
    private static final MessageFormat FD_I8 = new MessageFormat("{0} ({1}, as an 8-bit signed integer)");
    private static final MessageFormat FD_I16 = new MessageFormat("{0} ({1}, as a 16-bit signed integer)");
    private static final MessageFormat FD_I32 = new MessageFormat("{0} ({1}, as a 32-bit signed integer)");
    private static final MessageFormat FD_I64 = new MessageFormat("{0} ({1}, as a 64-bit signed integer)");
    private static final MessageFormat FD_U8 = new MessageFormat("{0} ({1}, as an 8-bit unsigned integer)");
    private static final MessageFormat FD_U16 = new MessageFormat("{0} ({1}, as a 16-bit unsigned integer)");
    private static final MessageFormat FD_U32 = new MessageFormat("{0} ({1}, as a 32-bit unsigned integer)");
    private static final MessageFormat FD_U64 = new MessageFormat("{0} ({1}, as a 64-bit unsigned integer)");
    private static final MessageFormat FD_MSG = new MessageFormat("{0} (Message with {1,choice,0# no fields|1# 1 field|1< {1,number,integer} fields})");
    private static final MessageFormat FD_OPAQUE = new MessageFormat("{0} ({1} bytes of opaque/binary data)");
    private static final MessageFormat FD_BOOL = new MessageFormat("{0} ({1} as a boolean value)");
    private static final MessageFormat FD_DATETIME = new MessageFormat("{0} ({1} as a Rendezvous time object)");
    private static final MessageFormat FD_XML = new MessageFormat("{0} ({1} bytes of compressed XML data)");
    private static final MessageFormat FD_STRING = new MessageFormat("{0} (\"{1}\")");
    private static final MessageFormat FD_STRING_LONG = new MessageFormat("{0} ({1} characters of string data, starting with \"{2} ...\")");
    private static final MessageFormat FD_STRING_XML = new MessageFormat("{0} ({1} characters of string encoded XML data)");
    private static final MessageFormat FD_USER = new MessageFormat("{0} ({1} bytes of custom user data)");

    private final boolean allowsChildren;

    private final Icon icon;

    private final String text;

    private static final Logger logger = Logger.getLogger(RvFieldTreeNode.class);

    private final TibrvMsgField field;

    /**
     * @param parent The parent of this node.
     * @param field The field represented by this node.
     * @param text A custom label to use when displaying this field.
     * @param icon A custom icon to use when displaying this field.
     */
    public RvFieldTreeNode(TreeNode parent, TibrvMsgField field, String text, Icon icon) {
        super(parent);
        this.icon = icon != null ? icon : setIcon(field);
        this.text = text != null ? text : getFieldDescription(field);
        this.field = field;
        if (field.type == TibrvMsg.MSG || field.type == TibrvMsg.XML) {
            allowsChildren = true;
        } else {
            allowsChildren = field.type == TibrvMsg.STRING && ((String) field.data).startsWith("<?xml ");
        }
    }

    /**
     * @param parent The parent of this node.
     * @param field The field represented by this node.
     */
    public RvFieldTreeNode(TreeNode parent, TibrvMsgField field) {
        this(parent, field, null, null);
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
                if (Logger.isErrorEnabled())
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
        return allowsChildren;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public String getTooltip() {
        return TibrvMsg.getTypeName(field.type);
    }
}
