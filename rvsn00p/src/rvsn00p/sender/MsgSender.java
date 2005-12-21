//:File:    MsgSender.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.sender;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

/**
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class MsgSender extends Dialog {

    private static final long serialVersionUID = 1648352208409693782L;
    private Label _sendSubject = new Label();
    private Label _replySubject = new Label();
    private TextField _tfSendSubject = new TextField();
    private TextField _tfReplySubject = new TextField();
    private TextArea _taSendMsg = new TextArea();
    private TextArea _taReply = new TextArea();
    private Label _lSendMsg = new Label();
    private Label _lReplyMsg = new Label();
    private Button _bSend = new Button();
    private Label _lReplyTimeout = new Label();
    private TextField _tfTimeout = new TextField();
    private Button _bClose = new Button();
    private TextField _tfTimeout1 = new TextField();
    private Label _lRepeatTimeout1 = new Label();
    private TextField _tfRepeat = new TextField();
    private Label _lRepeatTimeout2 = new Label();
    private Label _lDaemon = new Label();
    private Label _lNetwork = new Label();
    private Label _lService = new Label();
    private TextField _tDaemon = new TextField();
    private TextField _tfService = new TextField();
    private TextField _tfNetwork = new TextField();

    protected String _titlePrefix = null;

    public MsgSender(Frame parent, String title, String subject, String msg, String daemon, String service,
                     String network) {
        super(parent, title, false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit(title, subject, msg, daemon, service, network);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void jbInit(String title, String subject, String msg, String daemon, String service, String network)
            throws Exception {
        this.setSize(new Dimension(537, 546));
        this.setLayout(null);
        this.setTitle("RvSn00p 1.1.7 - SendSubject");
        _sendSubject.setText("Send subject:");
        _sendSubject.setBounds(new Rectangle(20, 3, 70, 15));
        _replySubject.setText("Reply subject:");
        _replySubject.setBounds(new Rectangle(20, 26, 80, 15));
        _tfSendSubject.setBounds(new Rectangle(100, 0, 400, 20));
        _tfSendSubject.addTextListener(new TextListener() {
            public void textValueChanged(TextEvent e) {
                _tfsend_textValueChanged(e);
            }
        });
        _tfReplySubject.setBounds(new Rectangle(100, 23, 400, 20));
        _taSendMsg.setBounds(new Rectangle(20, 155, 500, 195));
        _taReply.setBounds(new Rectangle(20, 365, 500, 120));
        _lSendMsg.setText("Message to send:");
        _lSendMsg.setBounds(new Rectangle(20, 140, 120, 15));
        _lReplyMsg.setText("Reply Message:");
        _lReplyMsg.setBounds(new Rectangle(20, 350, 90, 15));
        _bSend.setLabel("Send");
        _bSend.setBounds(new Rectangle(350, 490, 71, 27));
        _lReplyTimeout.setText("ReplyTimeout:");
        _lReplyTimeout.setBounds(new Rectangle(20, 49, 70, 15));
        _tfTimeout.setText("100");
        _tfTimeout.setBounds(new Rectangle(100, 46, 35, 20));
        _tfTimeout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tfTimeout_actionPerformed(e);
            }
        });
        _bClose.setLabel("Close");
        _bClose.setBounds(new Rectangle(435, 490, 71, 27));
        _tfTimeout1.setText("1");
        _tfTimeout1.setBounds(new Rectangle(185, 45, 30, 20));
        _tfTimeout1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tfTimeout_actionPerformed(e);
            }
        });
        _lRepeatTimeout1.setText("Repeat:");
        _lRepeatTimeout1.setBounds(new Rectangle(145, 48, 40, 15));
        _tfRepeat.setText("1");
        _tfRepeat.setBounds(new Rectangle(270, 45, 30, 20));
        _tfRepeat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tfTimeout_actionPerformed(e);
            }
        });
        _lRepeatTimeout2.setText("Interval:");
        _lRepeatTimeout2.setBounds(new Rectangle(225, 48, 40, 15));
        _lDaemon.setText("Daemon:");
        _lDaemon.setBounds(new Rectangle(20, 72, 65, 15));
        _lNetwork.setText("Network:");
        _lNetwork.setBounds(new Rectangle(20, 118, 65, 15));
        _lService.setText("Service:");
        _lService.setBounds(new Rectangle(20, 95, 60, 15));
        _tDaemon.setBounds(new Rectangle(100, 69, 400, 20));
        _tDaemon.addTextListener(new TextListener() {
            public void textValueChanged(TextEvent e) {
                _tfsend_textValueChanged(e);
            }
        });
        _tfService.setText("7500");
        _tfService.setBounds(new Rectangle(100, 92, 40, 20));
        _tfService.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tfTimeout_actionPerformed(e);
            }
        });
        _tfNetwork.setBounds(new Rectangle(100, 115, 400, 20));
        _tfNetwork.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tfTimeout_actionPerformed(e);
            }
        });

        this.add(_tfNetwork, null);
        this.add(_tfService, null);
        this.add(_tDaemon, null);
        this.add(_lService, null);
        this.add(_lNetwork, null);
        this.add(_lDaemon, null);
        this.add(_lRepeatTimeout2, null);
        this.add(_tfRepeat, null);
        this.add(_lRepeatTimeout1, null);
        this.add(_tfTimeout1, null);
        this.add(_bClose, null);
        this.add(_tfTimeout, null);
        this.add(_lReplyTimeout, null);
        this.add(_bSend, null);
        this.add(_lReplyMsg, null);
        this.add(_lSendMsg, null);
        this.add(_taReply, null);
        this.add(_taSendMsg, null);
        this.add(_tfReplySubject, null);
        this.add(_tfSendSubject, null);
        this.add(_replySubject, null);
        this.add(_sendSubject, null);
    }

    protected void center(Dialog dialog) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension comp = dialog.getSize();

        dialog.setLocation(((screen.width - comp.width) / 2),
                           ((screen.height - comp.height) / 2));

    }

    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    protected void requestClose() {

        closeAfterConfirm();
    }

    protected void closeAfterConfirm() {
        StringBuffer message = new StringBuffer();

        message.append("Are you sure you want to exit?\n");

        String title = "Are you sure you want to exit?";

        int value = JOptionPane.showConfirmDialog(
                this,
                message.toString(),
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null
        );

        if (value == JOptionPane.OK_OPTION) {
            dispose();
        }
    }

    void cancel() {
        // XXX: What to do here? Close the dialog?
    }


    private void _tfsend_textValueChanged(TextEvent e) {
        this.setTitle(_titlePrefix + " Send - " + _tfSendSubject.getText());
    }

    private void _tfTimeout_actionPerformed(ActionEvent e) {
        // XXX: What to do here? Show an error?
    }

    class MsgSenderWindowAdaptor extends WindowAdapter {
        protected MsgSender _monitor;

        public MsgSenderWindowAdaptor(MsgSender monitor) {
            _monitor = monitor;
        }

        public void windowClosing(WindowEvent ev) {
            _monitor.requestClose();
        }
    }
}
