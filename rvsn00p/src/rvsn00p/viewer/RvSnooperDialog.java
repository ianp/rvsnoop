//:File:    RvSnooperDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * A common superclass to provide behaviours for all RvSn00p dialogs.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class RvSnooperDialog extends JDialog {

    protected RvSnooperDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            pack();
            ensureMinimumSize(240, 120);
            centerOnScreen();
        }
        super.setVisible(visible);
    }

    /**
     * Ensure that the dialog is small enough to fit on the screen and is centered.
     */
    protected void centerOnScreen() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = getSize();
        size.width = Math.min(size.width, screen.width);
        size.height = Math.min(size.height, screen.height);
        setSize(size);
        setLocation((screen.width - size.width) / 2,
                    (screen.height - size.height) / 2);
    }

    /**
     * Wrap a string onto a panel using a series of stacked labels to represent lines.
     * <p>
     * The panel <em>must</em> already have a {@link java.awt.GridBagLayout} manager
     * on it.
     * 
     * @param message The message to split and display.
     * @param panel The panel to display the message on.
     */
    protected void wrapStringOnPanel(String message, Container panel) {
        while (message.length() > 0) {
            int index = message.indexOf('\n');
            String line = index != -1 ? message.substring(0, index) : message;
            message = message.substring(index + 1);
            panel.add(new Label(line), defaultConstraints());
        }
    }

    private GridBagConstraints defaultConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridheight = 1; // rows
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(0, 0, 0, 0); // top, left, bottom, right
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        return constraints;
    }

    /**
     * Ensure that the dialog is a certain minimum size.
     * 
     * @param w The minimum width in pixels.
     * @param h The minimum height in pixels.
     */
    protected void ensureMinimumSize(int w, int h) {
        Dimension size = getSize();
        size.width = Math.max(size.width, w);
        size.height = Math.max(size.height, h);
        setSize(size);
    }

}
