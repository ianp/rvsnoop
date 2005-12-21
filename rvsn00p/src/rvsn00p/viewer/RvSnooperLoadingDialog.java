//:File:    RvSnooperLoadingDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * RvSnooperLoadingDialog
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RvSnooperLoadingDialog extends RvSnooperDialog {

    private static final long serialVersionUID = 5981647803283267279L;

    public RvSnooperLoadingDialog(JFrame jframe, String message) {
        super(jframe, "RvSn00p", false);

        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        wrapStringOnPanel(message, main);

        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
        setVisible(true);

    }
}
