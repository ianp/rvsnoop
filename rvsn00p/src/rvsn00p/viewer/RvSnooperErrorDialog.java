/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * RvSnooperErrorDialog
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public class RvSnooperErrorDialog extends RvSnooperDialog {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------
    public RvSnooperErrorDialog(JFrame jframe, String message) {
        super(jframe, "Error", true);

        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        bottom.add(ok);

        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        wrapStringOnPanel(message, main);

        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
        show();

    }
    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

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