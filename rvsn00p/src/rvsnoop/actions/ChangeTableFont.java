//:File:    ChangeTableFont.java
//:Created: Dec 27, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsnoop.ui.FontChooser;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

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
        final UIManager ui = UIManager.INSTANCE;
        final FontChooser chooser = new FontChooser();
        chooser.setSelectedFont(ui.getMessageLedgerFont());
        if (chooser.showDialog(UIManager.INSTANCE.getFrame()) == FontChooser.OK_OPTION)
            ui.setMessageLedgerFont(chooser.getSelectedFont());
    }

}
