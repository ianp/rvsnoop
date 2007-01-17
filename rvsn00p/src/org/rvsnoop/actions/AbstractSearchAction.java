/*
 * Class:     AbstractSearchAction
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.ui.MatcherEditorListDialog;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * Base class for actions which act on filtered ledger views.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class AbstractSearchAction extends RvSnoopAction {

    /**
     * @param name
     * @param application
     */
    protected AbstractSearchAction(String name, Application application) {
        super(name, application);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.RvSnoopAction#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent e) {
        final EventList oldMatchers = getMatcherEditors();
        final JFrame frame = application.getFrame().getFrame();
        final MatcherEditorListDialog dialog = new MatcherEditorListDialog(
                frame, getTitle(), getDescription(), getImage(), oldMatchers);

        dialog.setVisible(true);
        final EventList newMatchers = dialog.getCopyOfEditors();
        if (newMatchers == null) { return; } // User cancelled dialog.

        // TODO add equals methods to the matcher editors so that this works as
        // expected. The equals methods will need to use non-final fields.
        final List added = new ArrayList(newMatchers);
        added.removeAll(oldMatchers);
        final List removed = new ArrayList(oldMatchers);
        removed.removeAll(newMatchers);

        final FilteredLedgerView ledger = getLedger();
        for (Iterator i = added.iterator(); i.hasNext(); ) {
            ledger.addFilter((MatcherEditor) i.next());
        }
        for (Iterator i = removed.iterator(); i.hasNext(); ) {
            ledger.removeFilter((MatcherEditor) i.next());
        }
    }

    protected EventList getMatcherEditors() {
        return new BasicEventList();
    }

    protected abstract FilteredLedgerView getLedger();

    protected abstract String getTitle();
    protected abstract String getDescription();
    protected abstract Image getImage();
}
