/*
 * Class:     RvFieldTreeNode
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.ui.ImageFactory;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvIPAddr;
import com.tibco.tibrv.TibrvIPPort;
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
public final class RvFieldTreeNode extends LazyTreeNode {

    private static final Icon defaultIcon = new ImageIcon(ImageFactory.getInstance().getIconImage("rvField"));
    private static final Icon messageIcon = new ImageIcon(ImageFactory.getInstance().getIconImage("rvMessage"));

    static String ENCRYPTED, F32ARRAY, F64ARRAY, I8ARRAY, I16ARRAY, I32ARRAY, I64ARRAY;
    static String U8ARRAY, U16ARRAY, U32ARRAY, U64ARRAY, IPADDR32, IPPORT16;
    static String F32, F64, I8, I16, I32, I64, U8, U16, U32, U64, MSG, OPAQUE;
    static String BOOL, DATETIME, XML, STRING, STRING_LONG, STRING_XML, USER;
    static String ERROR_FIELD, ERROR_XML_IO, ERROR_XML_PARSE;

    static { NLSUtils.internationalize(RvFieldTreeNode.class); }

    private static String describeField(TibrvMsgField field) {
        final short type = field.type;
        switch (type) {
            case TibrvMsg.ENCRYPTED: return FD_ENCRYPTED.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.MSG:       return FD_MSG.format(new Object[] {field.name, new Integer(((TibrvMsg) field.data).getNumFields())});
            case TibrvMsg.OPAQUE:    return FD_OPAQUE.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.BOOL:      return FD_BOOL.format(new Object[] {field.name, field.data});
            case TibrvMsg.DATETIME:  return FD_DATETIME.format(new Object[] {field.name, field.data});
            case TibrvMsg.IPADDR32:  return FD_IPADDR32.format(new Object[] {field.name, ((TibrvIPAddr) field.data).getAsString()});
            case TibrvMsg.IPPORT16:  return FD_IPPORT16.format(new Object[] {field.name, new Integer(((TibrvIPPort) field.data).getPort())});
            case TibrvMsg.F32ARRAY:  return FD_F32ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.F64ARRAY:  return FD_F64ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.I8ARRAY:   return FD_I8ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.I16ARRAY:  return FD_I16ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.I32ARRAY:  return FD_I32ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.I64ARRAY:  return FD_I64ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.U8ARRAY:   return FD_U8ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.U16ARRAY:  return FD_U16ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.U32ARRAY:  return FD_U32ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
            case TibrvMsg.U64ARRAY:  return FD_U64ARRAY.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
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
            case TibrvMsg.XML:
                return FD_XML.format(new Object[] {field.name, new Integer(((TibrvXml) field.data).getBytes().length)});
            case TibrvMsg.STRING:
                final String s = (String) field.data;
                if (s.startsWith("<?xml ")) return FD_STRING_XML.format(new Object[] {field.name, new Integer(s.length())});
                if (s.length() > 60) return FD_STRING_LONG.format(new Object[] {field.name, new Integer(s.length()), s.substring(0, 60)});
                return FD_STRING.format(new Object[] {field.name, s});
            default: return FD_USER.format(new Object[] {field.name, new Integer(Array.getLength(field.data))});
        }
    }

    private static final MessageFormat FD_ENCRYPTED = new MessageFormat(ENCRYPTED);
    private static final MessageFormat FD_F32ARRAY = new MessageFormat(F32ARRAY);
    private static final MessageFormat FD_F64ARRAY = new MessageFormat(F64ARRAY);
    private static final MessageFormat FD_I8ARRAY = new MessageFormat(I8ARRAY);
    private static final MessageFormat FD_I16ARRAY = new MessageFormat(I16ARRAY);
    private static final MessageFormat FD_I32ARRAY = new MessageFormat(I32ARRAY);
    private static final MessageFormat FD_I64ARRAY = new MessageFormat(I64ARRAY);
    private static final MessageFormat FD_U8ARRAY = new MessageFormat(U8ARRAY);
    private static final MessageFormat FD_U16ARRAY = new MessageFormat(U16ARRAY);
    private static final MessageFormat FD_U32ARRAY = new MessageFormat(U32ARRAY);
    private static final MessageFormat FD_U64ARRAY = new MessageFormat(U64ARRAY);
    private static final MessageFormat FD_IPADDR32 = new MessageFormat(IPADDR32);
    private static final MessageFormat FD_IPPORT16 = new MessageFormat(IPPORT16);
    private static final MessageFormat FD_F32 = new MessageFormat(F32);
    private static final MessageFormat FD_F64 = new MessageFormat(F64);
    private static final MessageFormat FD_I8 = new MessageFormat(I8);
    private static final MessageFormat FD_I16 = new MessageFormat(I16);
    private static final MessageFormat FD_I32 = new MessageFormat(I32);
    private static final MessageFormat FD_I64 = new MessageFormat(I64);
    private static final MessageFormat FD_U8 = new MessageFormat(U8);
    private static final MessageFormat FD_U16 = new MessageFormat(U16);
    private static final MessageFormat FD_U32 = new MessageFormat(U32);
    private static final MessageFormat FD_U64 = new MessageFormat(U64);
    private static final MessageFormat FD_MSG = new MessageFormat(MSG);
    private static final MessageFormat FD_OPAQUE = new MessageFormat(OPAQUE);
    private static final MessageFormat FD_BOOL = new MessageFormat(BOOL);
    private static final MessageFormat FD_DATETIME = new MessageFormat(DATETIME);
    private static final MessageFormat FD_XML = new MessageFormat(XML);
    private static final MessageFormat FD_STRING = new MessageFormat(STRING);
    private static final MessageFormat FD_STRING_LONG = new MessageFormat(STRING_LONG);
    private static final MessageFormat FD_STRING_XML = new MessageFormat(STRING_XML);
    private static final MessageFormat FD_USER = new MessageFormat(USER);

    private final boolean allowsChildren;

    private final Icon icon;

    private final String text;

    private static final Log log = LogFactory.getLog(RvFieldTreeNode.class);

    private final TibrvMsgField field;

    /**
     * @param parent The parent of this node.
     * @param field The field represented by this node.
     * @param text A custom label to use when displaying this field.
     * @param icon A custom icon to use when displaying this field.
     */
    public RvFieldTreeNode(TreeNode parent, TibrvMsgField field, String text, Icon icon) {
        super(parent);
        this.text = text != null ? text : describeField(field);
        this.field = field;
        if (field.type == TibrvMsg.MSG) {
            allowsChildren = true;
            this.icon = icon != null ? icon : messageIcon;
        } else if (field.type == TibrvMsg.XML) {
            allowsChildren = true;
            this.icon = icon != null ? icon : defaultIcon;
        } else {
            allowsChildren = field.type == TibrvMsg.STRING && ((String) field.data).startsWith("<?xml ");
            this.icon = icon != null ? icon : defaultIcon;
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
                if (log.isErrorEnabled()) {
                    log.error(MessageFormat.format(ERROR_FIELD, new Object[] {
                            new Integer(e.error)
                    }), e);
                }
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
        } catch (ParsingException e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(ERROR_XML_PARSE, new Object[] {
                        new Integer(e.getLineNumber()), new Integer(e.getColumnNumber())
                }), e);
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(ERROR_XML_IO, e);
            }
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
