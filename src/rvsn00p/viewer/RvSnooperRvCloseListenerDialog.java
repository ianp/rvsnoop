/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.viewer;

import rvsn00p.util.rv.RvParameters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * RvSnooperInputDialog
 *
 * Creates a popup input dialog box so that users can enter
 * a URL to open a log file from.
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 */

public class RvSnooperRvCloseListenerDialog extends RvSnooperDialog {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------
    private JTextField _tService;
    private JTextField _tNetwork;
    private JTextField _tDaemon;
    private JTextField _tSubject;
    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------


    /**
     * Retrieves tibco transport information from the user.
     * param jframe the frame where the dialog will be loaded from.
     * param title the title of the dialog box.
     */
    public RvSnooperRvCloseListenerDialog(JFrame jframe, String title, RvParameters defaultParameters) {
        super(jframe, title, true);

        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        JPanel main = new JPanel();
        main.setLayout(new FlowLayout());
        JPanel pservice = new JPanel();
        pservice.setLayout(new FlowLayout());
        pservice.add(new JLabel("Service"));
        _tService = new JTextField(5);
        // TODO: configure default
        _tService.setText(defaultParameters.getService());
        pservice.add(_tService);
        main.add(pservice);

        JPanel pDaemon = new JPanel();
        pDaemon.add(new JLabel("Daemon"));
        _tDaemon = new JTextField(30);

        _tDaemon.setText(defaultParameters.getDaemon());
        pDaemon.add(_tDaemon);
        main.add(pDaemon);

        JPanel pNetwork = new JPanel();
        pNetwork.add(new JLabel("Network"));
        _tNetwork = new JTextField(20);
        _tNetwork.setText(defaultParameters.getNetwork());
        pNetwork.add(_tNetwork);
        main.add(pNetwork);

        JPanel pSubject = new JPanel();
        pSubject.add(new JLabel("Subject"));
        _tSubject = new JTextField(20);
        _tSubject.setText(defaultParameters.getSubject());
        pSubject.add(_tSubject);
        main.add(pSubject);

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
            }
        });

        bottom.add(ok);
        bottom.add(cancel);
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
        minimumSizeDialog(this, 100, 60);
        pack();
        centerWindow(this);
        show();
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------
    public RvParameters getRvParameters() {

        RvParameters p = new RvParameters();

        p.setDeamon(_tDaemon.getText());
        p.setNetwork(_tNetwork.getText());
        p.setService(_tService.getText());
        p.setSubject(_tSubject.getText());

        return p;
    }



    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces
    //--------------------------------------------------------------------------
}