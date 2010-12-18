/*
 * Class:     FilterBySelection
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;
import rvsnoop.Record;

import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 * Filter the messages visible in the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class FilterBySelection extends AbstractSearchAction implements RecordLedgerSelectionListener {

    static { NLSUtils.internationalize(FilterBySelection.class); }

    private static final long serialVersionUID = -5949027962663244889L;

    public static final String COMMAND = "filterBySelection";
    static String DESCRIPTION, NAME, TITLE, TOOLTIP;

    private transient Record[] currentSelection;

    public FilterBySelection(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getDescription()
     */
    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected ImageIcon getIcon() {
        return new ImageIcon("/resources/banners/filterBySelection.png");
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getLedger()
     */
    @Override
    protected FilteredLedgerView getLedger() {
        return application.getFilteredLedger();
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getMatcherEditors()
     */
    @Override
    protected EventList getMatcherEditors() {
        final EventList matchers = new BasicEventList();
        if (currentSelection.length == 1) {
            configureMatchers(matchers, currentSelection[0]);
        } else {
            configureMatchers(matchers, currentSelection);
        }
        return matchers;
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getTitle()
     */
    @Override
    protected String getTitle() {
        return TITLE;
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.event.RecordLedgerSelectionListener#valueChanged(org.rvsnoop.event.RecordLedgerSelectionEvent)
     */
    public void valueChanged(RecordLedgerSelectionEvent event) {
        currentSelection = event.getSelectedRecords();
        setEnabled(currentSelection.length > 0);
    }

}
