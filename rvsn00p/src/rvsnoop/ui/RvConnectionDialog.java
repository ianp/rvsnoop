//:File:    RvConnectionDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.RecentConnections;
import rvsnoop.RvConnection;
import rvsnoop.StringUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
        super(RvSnooperGUI.getFrame(), "Add Connection", true);
        this.connection = initial;
        buildContentArea();
        buildButtonArea();
        pack();
        ensureMinimumSize(160, 160);
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
        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
            "r:default, 3dlu, p:grow",
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"));
        final CellConstraints cc = new CellConstraints();
        builder.addSeparator("Rendezvous Parameters", cc.xyw(1, 1, 3));
        builder.addLabel("Description", cc.xy(1, 3)).setLabelFor(description);
        builder.add(description,        cc.xy(3, 3));
        builder.addLabel("Service", cc.xy(1, 5)).setLabelFor(service);
        builder.add(service,        cc.xy(3, 5));
        builder.addLabel("Network", cc.xy(1, 7)).setLabelFor(network);
        builder.add(network,        cc.xy(3, 7));
        builder.addLabel("Daemon",  cc.xy(1, 9)).setLabelFor(daemon);
        builder.add(daemon,         cc.xy(3, 9));
        builder.addSeparator("Subscribed Subjects", cc.xyw(1, 11, 3));
        builder.addLabel("Subjects", cc.xy(1, 13)).setLabelFor(subject);
        builder.add(subject,         cc.xy(3, 13));
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

    /**
     * Ensure that the dialog is a certain minimum size.
     * 
     * @param w The minimum width in pixels.
     * @param h The minimum height in pixels.
     */
    protected void ensureMinimumSize(int w, int h) {
        final Dimension size = getSize();
        size.width = Math.max(size.width, w);
        size.height = Math.max(size.height, h);
        setSize(size);
    }

    public RvConnection getConnection() {
        final RvConnection newConnection = RvConnection.createConnection(service.getText(), network.getText(), daemon.getText());
        newConnection.setDescription(description.getText());
        newConnection.addSubjects(Arrays.asList(StringUtils.split(subject.getText())));
        RecentConnections.getInstance().add(newConnection);
        return newConnection;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

}
