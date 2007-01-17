/*
 * Class:     Quit
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.ui.ImageFactory;

/**
 * Quit the application.
 * <p>
 * Prompt the user for confirmation before quitting.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class Quit extends RvSnoopAction {

    static { NLSUtils.internationalize(Quit.class); }

    private static final long serialVersionUID = 4723433926433208758L;

    public static final String COMMAND = "quit";
    static String ACCELERATOR, CONFIRM_QUESTION, CONFIRM_TITLE, MNEMONIC, NAME,
        TOOLTIP;

    public Quit(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final JFrame frame = application.getFrame().getFrame();
        final Image banner = ImageFactory.getInstance().getBannerImage(COMMAND);
        int option = JOptionPane.showConfirmDialog(frame,
                CONFIRM_QUESTION, CONFIRM_TITLE, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, new ImageIcon(banner));
        if (option != JOptionPane.YES_OPTION) { return; }
        frame.dispose();
        application.shutdown();
    }

}
