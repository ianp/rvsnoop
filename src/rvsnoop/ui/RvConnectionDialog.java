//:File:    RvConnectionDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import rvsnoop.RecentConnections;
import rvsnoop.RvConnection;
import rvsnoop.StringUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;

/**
 * An input dialog for entry of Rendezvous parameters and subjects.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
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
    
    private final JTextField daemon  = new JTextField(10);

    private final JTextField description = new JTextField(10);
    
    private boolean isCancelled = false;
    
    private final JTextField network = new JTextField(10);
    
    private final RvConnection connection;

    private final JTextField service = new JTextField(10);
    
    private final JTextArea  subject = new JTextArea(3, 10);

    /**
     * Create a new dialog.
     * 
     * @param initial The initial parameter values to display, may be <code>null</code>.
     */
    public RvConnectionDialog(RvConnection initial) {
        super(UIManager.INSTANCE.getFrame(), "Add Connection", true);
        this.connection = initial;
        buildContentArea();
        buildButtonArea();
        pack();
        final Dimension size = getSize();
        size.width = Math.max(size.width, MAX_WIDTH);
        size.height = Math.max(size.height, MAX_HEIGHT);
        setSize(size);
        UIUtils.centerWindowOnScreen(this);
    }
    
    private void buildButtonArea() {
        final Action ok = new OKAction();
        final Action cancel = new CancelAction();
        final JPanel buttons = ButtonBarFactory.buildOKCancelBar(new JButton(ok), new JButton(cancel));
        buttons.setBorder(Borders.DLU2_BORDER);
        UIUtils.configureOKAndCancelButtons(buttons, ok, cancel);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void buildContentArea() {
        final DefaultFormBuilder builder = new DefaultFormBuilder(
                new FormLayout("r:default, 3dlu, p:grow", ""));
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
        description.setText(connection != null ? connection.getDescription() : RvConnection.gensym());
        service.setText(connection != null ? connection.getService() : RvConnection.DEFAULT_SERVICE);
        network.setText(connection != null ? connection.getNetwork() : RvConnection.DEFAULT_NETWORK);
        network.setToolTipText("networkcardid;networkaddress");
        daemon.setText(connection != null ? connection.getDaemon() : RvConnection.DEFAULT_DAEMON);
        subject.setToolTipText("Put multiple subjects on separate lines");
        subject.setBorder(daemon.getBorder());
        if (connection != null && connection.getNumSubjects() > 0) { 
            final StringBuffer buffer = new StringBuffer();
            for (final Iterator i = connection.getSubjects().iterator(); i.hasNext(); )
                buffer.append(i.next()).append("\n");
            subject.setText(buffer.toString());
        }
        final JPanel panel = builder.getPanel();
        panel.setBorder(Borders.DLU2_BORDER);
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    public RvConnection getConnection() {
        final RvConnection newConnection = RvConnection.createConnection(service.getText(), network.getText(), daemon.getText());
        newConnection.setDescription(description.getText());
        newConnection.addSubjects(Arrays.asList(StringUtils.split(subject.getText())));
        RecentConnections.INSTANCE.add(newConnection);
        return newConnection;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

}
