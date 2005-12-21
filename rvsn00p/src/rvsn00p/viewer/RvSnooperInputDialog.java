//:File:    RvSnooperInputDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * RvSnooperInputDialog
 *
 * Creates a popup input dialog box so that users can enter
 * a URL to open a log file from.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RvSnooperInputDialog extends RvSnooperDialog {

    private static final long serialVersionUID = 4206702664613863954L;

    public static final int SIZE = 30;

    private JTextField _textField;

    /**
     * Configures an input dialog box using a defualt size for the text field.
     * param jframe the frame where the dialog will be loaded from.
     * param title the title of the dialog box.
     * param label the label to be put in the dialog box.
     */
    public RvSnooperInputDialog(JFrame jframe, String title, String label) {
        this(jframe, title, label, SIZE);
    }

    /**
     * Configures an input dialog box.
     * param jframe the frame where the dialog will be loaded from.
     * param title the title of the dialog box.
     * param label the label to be put in the dialog box.
     * param size the size of the text field.
     */
    public RvSnooperInputDialog(JFrame jframe, String title, String label,
                                int size) {
        super(jframe, title, true);

        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        JPanel main = new JPanel();
        main.setLayout(new FlowLayout());
        main.add(new JLabel(label));
        _textField = new JTextField(size);
        main.add(_textField);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hide();
                }
            }
        });

        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
                // set the text field to blank just in case
                // a file was selected before the Cancel
                // button was pressed.
                _textField.setText("");
            }
        });

        bottom.add(ok);
        bottom.add(cancel);
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
        pack();
        centerOnScreen();
        setVisible(true);
    }

    public String getText() {
        String s = _textField.getText();
        if (s == null) return null;
        return s.trim();
    }

    public String setText(String text){
        _textField.setText(text);
        return text;
    }

    public String setToolTipText(String text) {
        _textField.setToolTipText(text);
        return text;
    }

}
