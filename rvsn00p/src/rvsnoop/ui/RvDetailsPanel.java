//:File:    RvDetailsPanel.java
//:Created: Jan 6, 2006
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Constructor;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import rvsnoop.LazyTreeNode;
import rvsnoop.Logger;
import rvsnoop.RvMessageTreeNode;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.tibrv.TibrvCmMsg;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * A panel that can show details about a Rendezvous message.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class RvDetailsPanel extends JPanel {
    
    private static final Constructor aeMsgTreeNode;
    
    private static final Logger logger = Logger.getLogger(RvDetailsPanel.class);
    
    static {
        Class aeMsgTreeNodeClass;
        Constructor aeMsgTreeNodeConstructor = null;
        try {
            aeMsgTreeNodeClass = Class.forName("rvsnoop.AeMsgTreeNode");
            aeMsgTreeNodeConstructor = aeMsgTreeNodeClass.getConstructor(new Class[] { TibrvMsg.class });
            if (logger.isInfoEnabled())
                logger.info("SDK found, AE tree view will be enabled.");
        } catch (Exception e) {
            if (logger.isInfoEnabled())
                logger.info("SDK not found, AE tree view will be disabled.", e);
            // Do nothing if SDK not on class path.
        }
        aeMsgTreeNode = aeMsgTreeNodeConstructor;
    }

    private static final long serialVersionUID = -4899980613877741151L;
    
    private final JTextField cmSender = new JTextField();
    
    private final JTextField cmSequence = new JTextField();

    private final TreeNode emptyRoot = new DefaultMutableTreeNode("[Nothing Selected]", false);

    final Font headerFont = getFont().deriveFont(getFont().getSize() - 2.0f);

    private Icon icon;
    
    private final JLabel iconLabel = new JLabel();

    private final DefaultTreeModel model = new DefaultTreeModel(emptyRoot);

    private final JTextField numFields = new JTextField();
    
    private final JTextField replySubject = new JTextField();

    private final JTextField sendSubject = new JTextField();
    
    private final JTextField stringEncoding = new JTextField();

    private final JTree tree = new JTree(model);
    
    public RvDetailsPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        final JTextField[] fields = new JTextField[] {
            cmSender, cmSequence, numFields, replySubject, sendSubject, stringEncoding
        };
        for (int i = 0, imax = fields.length; i < imax; ++i)
            fields[i].setEditable(false);
        clearPanel();
    }
    
    private void addNamedField(String name, JTextField field, int x, int y, int w, DefaultFormBuilder builder, CellConstraints cc, Border textBorder) {
        final JLabel label = builder.addLabel(name, cc.xy (x, y));
        label.setFont(headerFont);
        label.setLabelFor(field);
        builder.add(field, cc.xyw(x + 2, y, w));
        field.setFont(headerFont);
        field.setBorder(textBorder);
    }
    
    private void clearPanel() {
        final JTextField[] fields = new JTextField[] {
            cmSender, cmSequence, numFields, replySubject, sendSubject, stringEncoding
        };
        for (int i = 0, imax = fields.length; i < imax; ++i)
            fields[i].setText("");
        iconLabel.setIcon(Banners.MESSAGE_NONE);
        model.setRoot(emptyRoot);
    }
    
    private JScrollPane createBody() {
        tree.setBorder(BorderFactory.createEmptyBorder());
        tree.setRootVisible(true);
        tree.setCellRenderer(new LazyTreeNode.Renderer());
        final JScrollPane scroller = new JScrollPane(tree);
        scroller.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        return scroller;
    }

    private JPanel createHeader() {
        final JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder());
        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
            "r:default, 3dlu, p:grow, 7dlu, r:default, 3dlu, p:grow",
            "p, 3dlu, p, 3dlu, p, 3dlu, p"));
        final CellConstraints cc = new CellConstraints();
        final Border textBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
        addNamedField("Send Subject",    sendSubject,    1, 1, 5, builder, cc, textBorder);
        addNamedField("Reply Subject",   replySubject,   1, 3, 5, builder, cc, textBorder);
        addNamedField("Num. Fields",     numFields,      1, 5, 1, builder, cc, textBorder);
        addNamedField("String Encoding", stringEncoding, 5, 5, 1, builder, cc, textBorder);
        addNamedField("CM Sender",       cmSender,       1, 7, 1, builder, cc, textBorder);
        addNamedField("CM Sequence",     cmSequence,     5, 7, 1, builder, cc, textBorder);
        final JPanel headerFields = builder.getPanel();
        headerFields.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 4));
        iconLabel.setSize(iconLabel.getHeight(), iconLabel.getHeight());
        header.add(headerFields, BorderLayout.CENTER);
        header.add(iconLabel, BorderLayout.EAST);
        return header;
    }
    
    public void setMessage(TibrvMsg message) {
        if (message == null) {
            clearPanel();
            return;
        }
        numFields.setText(Integer.toString(message.getNumFields()));
        replySubject.setText(message.getReplySubject());
        sendSubject.setText(message.getSendSubject());
        stringEncoding.setText(message.getMsgStringEncoding());
        setCertifiedFields(message);
        if (icon == null)
            if (message.getSendSubject().startsWith("_RVFT."))
                icon = Banners.MESSAGE_FT;
            else if (message.getSendSubject().startsWith("_RV."))
                icon = Banners.MESSAGE_ADVISORY;
            else
                icon = Banners.MESSAGE;
        iconLabel.setIcon(icon);
        if (aeMsgTreeNode == null) {
            model.setRoot(new RvMessageTreeNode(message));
        } else {
            try {
                // We need to do this reflectively in case the SDK isn't available.
                model.setRoot((TreeNode) aeMsgTreeNode.newInstance(new Object[] { message }));
            } catch (Exception e) {
                model.setRoot(new RvMessageTreeNode(message));
            }
        }
    }

    private void setCertifiedFields(TibrvMsg message) {
        try {
            final String sender = TibrvCmMsg.getSender(message); 
            if (sender != null && sender.length() > 0) {
                cmSender.setText(sender);
                cmSequence.setText(Long.toString(TibrvCmMsg.getSequence(message)));
                icon = Banners.MESSAGE_CM;
                cmSender.getParent().validate();
            } else {
                cmSender.setText("");
                cmSequence.setText("");
            }
        } catch (TibrvException e) {
            cmSender.setText("");
            cmSequence.setText("");
        }
    }

}
