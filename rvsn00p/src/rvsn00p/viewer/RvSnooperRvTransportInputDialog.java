//:File:    RvSnooperRvTransportInputDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rvsn00p.StringUtils;
import rvsn00p.ui.UIUtils;
import rvsn00p.util.rv.RvParameters;

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
public final class RvSnooperRvTransportInputDialog extends JDialog {

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
    
    private final RvParameters params;

    private final JTextField service = new JTextField(10);
    
    private final JTextArea  subject = new JTextArea(3, 10);
    
    /**
     * Create a new dialog.
     * 
     * @param params The initial parameter values to display, may be <code>null</code>.
     */
    public RvSnooperRvTransportInputDialog(RvParameters params) {
        super(RvSnooperGUI.getAppFrame(), "New Listener", true);
        this.params = params;
        buildContentArea();
        buildButtonArea();
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!subject.hasFocus() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hide();
                }
            }
        });
        pack();
        ensureMinimumSize(160, 160);
        centerOnScreen();
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
        builder.addLabel("Description", cc.xy(1, 3));
        builder.add(description,        cc.xy(3, 3));
        builder.addLabel("Service", cc.xy(1, 5));
        builder.add(service,        cc.xy(3, 5));
        builder.addLabel("Network", cc.xy(1, 7));
        builder.add(network,        cc.xy(3, 7));
        builder.addLabel("Daemon",  cc.xy(1, 9));
        builder.add(daemon,         cc.xy(3, 9));
        builder.addSeparator("Subscribed Subjects", cc.xyw(1, 11, 3));
        builder.addLabel("Subjects", cc.xy(1, 13));
        builder.add(subject,         cc.xy(3, 13));
        description.setText(params.getDescription());
        service.setText(params.getService());
        network.setText(params.getNetwork());
        network.setToolTipText("networkcardid;networkaddress");
        daemon.setText(params.getDaemon());
        subject.setToolTipText("Put multiple subjects on separate lines");
        subject.setBorder(daemon.getBorder());
        final String[] s = params.getSubjects();
        if (s != null && s.length > 0) {
            final StringBuffer buffer = new StringBuffer(s.length * 32);
            for (int i = 0, imax = s.length; i < imax; ++i)
                buffer.append(s[i]).append("\n");
            subject.setText(buffer.toString());
        }
        final JPanel panel = builder.getPanel();
        panel.setBorder(Borders.DLU2_BORDER);
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    /**
     * Ensure that the dialog is small enough to fit on the screen and is centered.
     */
    protected void centerOnScreen() {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = getSize();
        size.width = Math.min(size.width, screen.width);
        size.height = Math.min(size.height, screen.height);
        setSize(size);
        setLocation((screen.width - size.width) / 2,
                    (screen.height - size.height) / 2);
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

    public RvParameters getParameters() {
        params.setService(service.getText());
        params.setNetwork(network.getText());
        params.setDaemon(daemon.getText());
        params.setDescription(description.getText());
        params.setSubjects(StringUtils.split(subject.getText()));
        return params;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

}
