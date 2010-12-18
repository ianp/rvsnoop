/*
 * Class:     SearchBySelection
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Image;

import javax.swing.*;

import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;
import org.rvsnoop.ui.SearchResultsDialog;

import rvsnoop.Record;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Start a new live search based on the current selection.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class SearchBySelection extends AbstractSearchAction implements RecordLedgerSelectionListener {

    static { NLSUtils.internationalize(SearchBySelection.class); }

    private static final long serialVersionUID = -4624032296697618690L;

    public static final String COMMAND = "searchBySelection";
    static String DESCRIPTION, NAME, TITLE, TOOLTIP;

    private transient Record[] currentSelection;

    public SearchBySelection(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#displayResults(org.rvsnoop.RecordLedger)
     */
    @Override
    protected void displayResults(RecordLedger ledger) {
        new SearchResultsDialog(application.getFrame(), ledger,
                application.getConnections()).setVisible(true);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getDescription()
     */
    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getImage()
     */
    @Override
    protected Image getImage() {
        return new ImageIcon("/resources/banners/searchBySelection.png").getImage();
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
