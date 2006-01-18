//:File:    SearchDialog.java
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
 * A dialog to configure the search options.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class SearchDialog extends JDialog {

    private class CancelAction extends AbstractAction {
        private static final long serialVersionUID = -6922363199905124333L;
        CancelAction() {
            super("Cancel");
        }
        public void actionPerformed(ActionEvent e) {
            isCancelled = true;
            dispose();
        }
    }
    
    private class OKAction extends AbstractAction {
        private static final long serialVersionUID = -3493879318583249233L;
        OKAction() {
            super("OK");
        }
        public void actionPerformed(ActionEvent e) {
            isCancelled = false;
            dispose();
        }
    }

    private static final long serialVersionUID = -6097317818406358840L;

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
    
    private boolean isCancelled;

    private final JCheckBox  message      = new JCheckBox();
    private final JCheckBox  replySubject = new JCheckBox();
    private final JTextField searchText   = new JTextField(20);
    private final JCheckBox  sendSubject  = new JCheckBox();
    private final JCheckBox  trackingId   = new JCheckBox();
    
    public SearchDialog(String searchText, boolean message, boolean sendSubject, boolean replySubject, boolean trackingId) {
        super(UIManager.INSTANCE.getFrame(), "Find", true);
        buildContentArea();
        buildButtonArea();
        this.searchText.setText(searchText);
        this.message.setSelected(message);
        this.sendSubject.setSelected(sendSubject);
        this.replySubject.setSelected(replySubject);
        this.trackingId.setSelected(trackingId);
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
        builder.append("Search Text", searchText).setLabelFor(searchText);
        builder.nextLine();
        builder.append("Message?", message).setLabelFor(message);
        builder.nextLine();
        builder.append("Tracking ID?", trackingId).setLabelFor(trackingId);
        builder.nextLine();
        builder.append("Send Subbject?", sendSubject).setLabelFor(sendSubject);
        builder.nextLine();
        builder.append("Reply Subject?", replySubject).setLabelFor(replySubject);
        final JPanel panel = builder.getPanel();
        panel.setBorder(Borders.DLU2_BORDER);
        getContentPane().add(panel, BorderLayout.CENTER);
    }
    
    public boolean isCancelled() {
        return isCancelled;
    }

    public String getSearchText() {
        return searchText.getText();
    }
    
    public boolean isMessageSelected() {
        return message.isSelected();
    }
    
    public boolean isSendSubjectSelected() {
        return sendSubject.isSelected();
    }
    
    public boolean isReplySubjectSelected() {
        return replySubject.isSelected();
    }
    
    public boolean isTrackingIdSelected() {
        return trackingId.isSelected();
    }
    
}
