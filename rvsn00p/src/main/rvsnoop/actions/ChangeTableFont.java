/*
 * Class:     ChangeTableFont
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rvsnoop.UserPreferences;
import org.rvsnoop.ui.MainFrame;

import rvsnoop.ui.FontChooser;
import rvsnoop.ui.Icons;

/**
 * Change the font used to display text in the message ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class ChangeTableFont extends AbstractAction {

    private static final String ID = "changeTableFont";

    private static String NAME = "Change Font";

    private static final long serialVersionUID = 7334880831350593586L;

    private static String TOOLTIP = "Change the font used to display text in the message ledger";

    public ChangeTableFont() {
        super(NAME);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.SMALL_ICON, Icons.FONT);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final FontChooser chooser = new FontChooser();
        chooser.setSelectedFont(UserPreferences.getInstance().getLedgerFont());
        if (chooser.showDialog(MainFrame.INSTANCE) == FontChooser.OK_OPTION) {
            final Font font = chooser.getSelectedFont();
            UserPreferences.getInstance().setLedgerFont(font);
        }
    }

}