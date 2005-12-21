//:File:    RvSnooperErrorDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * RvSnooperErrorDialog
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RvSnooperErrorDialog extends RvSnooperDialog {

    private static final long serialVersionUID = 1385873124819463709L;

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
        setVisible(true);

    }

}
