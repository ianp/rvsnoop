// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Constructor;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.rvsnoop.LazyTreeNode;
import org.rvsnoop.Logger;
import org.rvsnoop.RvMessageTreeNode;

import rvsnoop.Record;

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

    /** Standard message banner for details panel. */
    private static final Icon MESSAGE = new ImageIcon("/resources/banners/message.png");

    /** Advisory message banner for details panel. */
    private static final Icon MESSAGE_ADVISORY = new ImageIcon("/resources/banners/message_advisory.png");

    /** Certified messaging message banner for details panel. */
    private static final Icon MESSAGE_CM = new ImageIcon("/resources/banners/message_rvcm.png");

    /** Default empty banner for details panel. */
    private static final Icon MESSAGE_NONE = new ImageIcon("/resources/banners/message_none.png");

    /** Fault tolerant message banner for details panel. */
    private static final Icon MESSAGE_FT = new ImageIcon("/resources/banners/message_rvft.png");

    private static final Constructor aeMsgTreeNode;

    private static final Logger logger = Logger.getLogger();

    private static final NumberFormat INT_FORMAT = NumberFormat.getIntegerInstance();

    static {
        Constructor aeMsgTreeNodeConstructor = null;
        try {
            Class.forName("com.tibco.sdk.MTree");
            Class aeMsgTreeNodeClass = Class.forName("org.rvsnoop.AeMessageTreeNode");
            aeMsgTreeNodeConstructor = aeMsgTreeNodeClass.getConstructor(new Class[] { TibrvMsg.class });
            logger.info("SDK found, AE tree view will be enabled.");
        } catch (Exception e) {
            logger.info("SDK not found, AE tree view will be disabled.", e);
            // Do nothing if SDK not on class path.
        }
        aeMsgTreeNode = aeMsgTreeNodeConstructor;
    }

    private static final long serialVersionUID = -4899980613877741151L;

    private final JTextField cmSender = new JTextField();

    private final JTextField cmSequence = new JTextField();

    private final TreeNode emptyRoot = new DefaultMutableTreeNode("[Nothing Selected]", false);

    private final Font headerFont = getFont().deriveFont(getFont().getSize() - 2.0f);

    private final JLabel iconLabel = new JLabel();

    private final DefaultTreeModel model = new DefaultTreeModel(emptyRoot);

    private final JTextField replySubject = new JTextField();

    private final JTextField sendSubject = new JTextField();

    private final JTextField stringEncoding = new JTextField();

    private final JTree tree = new JTree(model);

    private final JTextField wireSize = new JTextField();

    public RvDetailsPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        final JTextField[] fields = new JTextField[] {
            cmSender, cmSequence, wireSize, replySubject, sendSubject, stringEncoding
        };
        for (int i = 0, imax = fields.length; i < imax; ++i)
            fields[i].setEditable(false);
        clearPanel();
        ToolTipManager.sharedInstance().registerComponent(tree);
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
            cmSender, cmSequence, wireSize, replySubject, sendSubject, stringEncoding
        };
        for (int i = 0, imax = fields.length; i < imax; ++i)
            fields[i].setText("");
        iconLabel.setIcon(MESSAGE_NONE);
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
        addNamedField("Wire Size",       wireSize,       1, 5, 1, builder, cc, textBorder);
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

    public void setMessage(Record record) {
        if (record == null) {
            clearPanel();
            return;
        }
        final TibrvMsg message = record.getMessage();
        wireSize.setText(INT_FORMAT.format(record.getSizeInBytes()) + " bytes");
        replySubject.setText(record.getReplySubject());
        final String ss = record.getSendSubject();
        sendSubject.setText(ss);
        stringEncoding.setText(""); // TODO: Replace this with something useful.
        if (setCertifiedFields(message)) {
            setIconAndTooltip(MESSAGE_CM, "Certified messaging co-ordination message");
        } else if (ss.startsWith("_RVFT.")) {
            setIconAndTooltip(MESSAGE_FT, "Fault tolerance co-ordination message");
        } else if (ss.startsWith("_RV.")) {
            setIconAndTooltip(MESSAGE_ADVISORY, "Rendezvous advisory message");
        } else {
            setIconAndTooltip(MESSAGE, "");
        }
        if (aeMsgTreeNode == null) {
            model.setRoot(new RvMessageTreeNode(message));
        } else {
            logger.debug("Trying to create AE details tree from message.");
            try {
                // We need to do this reflectively in case the SDK isn't available.
                model.setRoot((TreeNode) aeMsgTreeNode.newInstance(message));
            } catch (Exception e) {
                model.setRoot(new RvMessageTreeNode(message));
            }
        }
    }

    private boolean setCertifiedFields(TibrvMsg message) {
        try {
            final String sender = TibrvCmMsg.getSender(message);
            if (sender != null && sender.length() > 0) {
                cmSender.setText(sender);
                cmSequence.setText(Long.toString(TibrvCmMsg.getSequence(message)));
                cmSender.getParent().validate();
                return true;
            } else {
                cmSender.setText("");
                cmSequence.setText("");
            }
        } catch (TibrvException e) {
            cmSender.setText("");
            cmSequence.setText("");
        }
        return false;
    }

    private void setIconAndTooltip(Icon icon, String tip) {
        iconLabel.setIcon(icon);
        iconLabel.setToolTipText(tip);
    }
}
