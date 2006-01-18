//:File:    FilterDialog.java
//:Created: Jan 17, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A dialog to configure the filter options.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class FilterDialog extends JDialog {

    private class CancelAction extends AbstractAction {
        private static final long serialVersionUID = -9052778551703744478L;
        CancelAction() {
            super("Cancel");
        }
        public void actionPerformed(ActionEvent e) {
            isCancelled = true;
            dispose();
        }
    }
    
    private class OKAction extends AbstractAction {
        private static final long serialVersionUID = 2218686250288868092L;
        OKAction() {
            super("OK");
        }
        public void actionPerformed(ActionEvent e) {
            isCancelled = false;
            dispose();
        }
    }

    private static final long serialVersionUID = 8760469653655791342L;

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
    
    private final JCheckBox  sendSubjectEnabled  = new JCheckBox();
    private final JCheckBox  trackingIdEnabled   = new JCheckBox();

    private boolean isCancelled;
    
    private final JTextField  sendSubject  = new JTextField(20);
    private final JTextField  trackingId   = new JTextField(20);
    
    public FilterDialog(String sendSubject, String trackingId, boolean sendSubjectEnabled, boolean trackingIdEnabled) {
        super(UIManager.INSTANCE.getFrame(), "Filter", true);
        buildContentArea();
        buildButtonArea();
        this.sendSubject.setText(sendSubject);
        this.trackingId.setText(trackingId);
        this.sendSubjectEnabled.setSelected(sendSubjectEnabled);
        this.trackingIdEnabled.setSelected(trackingIdEnabled);
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
                new FormLayout("r:default, 3dlu, p, 3dlu, p:grow", ""));
        builder.append("Send Subbject?", sendSubjectEnabled, sendSubject).setLabelFor(sendSubject);
        builder.nextLine();
        builder.append("Tracking ID?", trackingIdEnabled, trackingId).setLabelFor(trackingId);
        builder.nextLine();
        final JPanel panel = builder.getPanel();
        panel.setBorder(Borders.DLU2_BORDER);
        getContentPane().add(panel, BorderLayout.CENTER);
    }
    
    public String getSendSubject() {
        return sendSubject.getText();
    }

    public String getTrackingId() {
        return trackingId.getText();
    }
    
    public boolean isCancelled() {
        return isCancelled;
    }
    
    public boolean isSendSubjectSelected() {
        return sendSubjectEnabled.isSelected();
    }
    
    public boolean isTrackingIdSelected() {
        return trackingIdEnabled.isSelected();
    }

}
