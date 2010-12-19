// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import rvsnoop.RvConnection;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An input dialog for entry of Rendezvous parameters and subjects.
 */
public final class RvConnectionDialog extends JDialog {

    private class CancelAction extends AbstractAction {
        private static final long serialVersionUID = -6383727262191312352L;
        CancelAction() {
            super("Cancel");
        }
        public void actionPerformed(ActionEvent e) {
            isCancelled = true;
            dispose();
        }
    }

    private class OKAction extends AbstractAction {
        private static final long serialVersionUID = -6455892125502790205L;
        OKAction() {
            super("OK");
        }
        public void actionPerformed(ActionEvent e) {
            isCancelled = false;
            dispose();
        }
    }

    /**
     * The maximum height of the dialog.
     * <p>
     * This helps to protect us from buggy {@link #pack()} implementations.
     */
    private static final int MAX_HEIGHT = 160;

    /**
     * The maximum width of the dialog.
     * <p>
     * This helps to protect us from buggy {@link #pack()} implementations.
     */
    private static final int MAX_WIDTH = 160;

    private static final long serialVersionUID = 8834430809279507334L;

    private final Splitter splitter = Splitter.on("\n").omitEmptyStrings().trimResults();

    private final JTextField daemon  = new JTextField(10);

    private final JTextField description = new JTextField(10);

    private boolean isCancelled = false;

    private final JTextField network = new JTextField(10);

    private final JTextField service = new JTextField(10);

    private final JTextArea  subject = new JTextArea(3, 10);

    /**
     * Create a new dialog.
     *
     * @param parent the parent frame for the dialog.
     * @param initial the initial parameter values to display, or {@code null}.
     */
    public RvConnectionDialog(Frame parent, RvConnection initial) {
        super(parent, "Add Connection", true);
        buildContentArea(initial != null ? initial : new RvConnection());
        buildButtonArea();
        pack();
        final Dimension size = getSize();
        size.width = Math.max(size.width, MAX_WIDTH);
        size.height = Math.max(size.height, MAX_HEIGHT);
        setSize(size);
        setLocationRelativeTo(parent);
    }

    private void buildButtonArea() {
        final Action ok = new OKAction();
        final Action cancel = new CancelAction();
        final JPanel buttons = ButtonBarFactory.buildOKCancelBar(new JButton(ok), new JButton(cancel));
        buttons.setBorder(Borders.DLU2_BORDER);
        UIUtils.configureOKAndCancelButtons(buttons, ok, cancel);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void buildContentArea(RvConnection conn) {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("r:default, 3dlu, p:grow", ""));
        builder.appendSeparator("Rendezvous Parameters");
        builder.append("Description", description).setLabelFor(description);
        builder.nextLine();
        builder.append("Service", service).setLabelFor(service);
        builder.nextLine();
        builder.append("Network", network).setLabelFor(network);
        builder.nextLine();
        builder.append("Daemon",  daemon).setLabelFor(daemon);
        builder.nextLine();
        builder.appendSeparator("Subscribed Subjects");
        builder.append("Subjects", subject).setLabelFor(subject);
        builder.nextLine();
        description.setText(conn.getDescription());
        service.setText(conn.getService());
        network.setText(conn.getNetwork());
        network.setToolTipText("networkcardid;networkaddress");
        daemon.setText(conn.getDaemon());
        subject.setToolTipText("Put multiple subjects on separate lines");
        subject.setBorder(daemon.getBorder());
        if (conn.getNumSubjects() > 0) {
            subject.setText(Joiner.on("\n").join(conn.getSubjects()));
        } else {
            subject.setText(">\n");
        }
        final JPanel panel = builder.getPanel();
        panel.setBorder(Borders.DLU2_BORDER);
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    public RvConnection getConnection() {
        final RvConnection conn = new RvConnection(service.getText(), network.getText(), daemon.getText());
        conn.setDescription(description.getText());
        for (String s : splitter.split(subject.getText())) {
            conn.addSubject(s);
        }
        return conn;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

}
