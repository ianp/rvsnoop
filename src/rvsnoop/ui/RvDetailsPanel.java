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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import rvsnoop.LazyTreeNode;
import rvsnoop.Logger;
import rvsnoop.RvMsgTreeNode;

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
    
    static {
        Class aeMsgTreeNodeClass = null;
        Constructor aeMsgTreeNodeConstructor = null;
        try {
            aeMsgTreeNodeClass = Class.forName("rvsnoop.AeMsgTreeNode");
            aeMsgTreeNodeConstructor = aeMsgTreeNodeClass.getConstructor(new Class[] { TibrvMsg.class });
        } catch (Exception ignored) {
            // Do nothing if SDK not on class path.
        }
        aeMsgTreeNode = aeMsgTreeNodeConstructor;
    }
    
    private static final Logger logger = Logger.getLogger(RvDetailsPanel.class);

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
        setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        final JTextField[] fields = new JTextField[] {
            cmSender, cmSequence, numFields, replySubject, sendSubject, stringEncoding
        };
        for (int i = 0, imax = fields.length; i < imax; ++i)
            fields[i].setEditable(false);
        clearPanel();
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
        final JLabel ssLabel = builder.addLabel("Send Subject", cc.xy (1, 1));
        ssLabel.setFont(headerFont);
        ssLabel.setLabelFor(sendSubject);
        builder.add(sendSubject, cc.xyw(3, 1, 5)).setFont(headerFont);
        final JLabel rsLabel = builder.addLabel("Reply Subject", cc.xy (1, 3));
        rsLabel.setFont(headerFont);
        rsLabel.setLabelFor(sendSubject);
        builder.add(replySubject, cc.xyw(3, 3, 5)).setFont(headerFont);
        final JLabel nfLabel = builder.addLabel("Num. Fields", cc.xy (1, 5));
        nfLabel.setFont(headerFont);
        nfLabel.setLabelFor(sendSubject);
        builder.add(numFields, cc.xy(3, 5)).setFont(headerFont);
        final JLabel seLabel = builder.addLabel("String Encoding", cc.xy (5, 5));
        seLabel.setFont(headerFont);
        seLabel.setLabelFor(sendSubject);
        builder.add(stringEncoding, cc.xy(7, 5)).setFont(headerFont);
        final JLabel cmSendLabel = builder.addLabel("CM Sender", cc.xy (1, 7));
        cmSendLabel.setFont(headerFont);
        cmSendLabel.setLabelFor(sendSubject);
        builder.add(cmSender, cc.xy(3, 7)).setFont(headerFont);
        final JLabel cmSeqLabel = builder.addLabel("CM Sequence", cc.xy (5, 7));
        cmSeqLabel.setFont(headerFont);
        cmSeqLabel.setLabelFor(sendSubject);
        builder.add(cmSequence, cc.xy(7, 7)).setFont(headerFont);
        final JPanel headerFields = builder.getPanel();
        headerFields.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 4));
        iconLabel.setSize(56, iconLabel.getHeight());
        header.add(headerFields, BorderLayout.CENTER);
        header.add(iconLabel, BorderLayout.EAST);
        return header;
    }
    
    public void setMessage(TibrvMsg message) {
        if (message == null) {
            clearPanel();
            return;
        }
        icon = null;
        numFields.setText(Integer.toString(message.getNumFields()));
        replySubject.setText(message.getReplySubject());
        sendSubject.setText(message.getSendSubject());
        stringEncoding.setText(message.getMsgStringEncoding());
        try {
            cmSender.setText(TibrvCmMsg.getSender(message));
            cmSequence.setText(Long.toString(TibrvCmMsg.getSequence(message)));
            icon = Banners.MESSAGE_CM;
        } catch (TibrvException e) {
            cmSender.setText("");
            cmSequence.setText("");
        }
        if (icon == null)
            if (message.getSendSubject().startsWith("_RVFT."))
                icon = Banners.MESSAGE_FT;
            else if (message.getSendSubject().startsWith("_RV."))
                icon = Banners.MESSAGE_ADVISORY;
        iconLabel.setIcon(icon);
        try {
            // We need to do this reflectively in case the SDK isn't available.
            if (aeMsgTreeNode != null)
                model.setRoot((TreeNode) aeMsgTreeNode.newInstance(new Object[] { message }));
            else
                model.setRoot(new RvMsgTreeNode(message));
        } catch (Exception e) {
            if (logger.isWarnEnabled())
                logger.warn("", e);
            model.setRoot(new RvMsgTreeNode(message));
        }
    }

}
