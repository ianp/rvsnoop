// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;
import org.rvsnoop.ui.SearchResultsDialog;
import rvsnoop.Record;

import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 * Start a new live search based on the current selection.
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

    @Override
    protected void displayResults(RecordLedger ledger) {
        new SearchResultsDialog(application.getFrame(), ledger,
                application.getConnections(), application.getRecordTypes()).setVisible(true);
    }

    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected ImageIcon getIcon() {
        return new ImageIcon("/resources/banners/searchBySelection.png");
    }

    @Override
    protected FilteredLedgerView getLedger() {
        return application.getFilteredLedger();
    }

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

    @Override
    protected String getTitle() {
        return TITLE;
    }

    public void valueChanged(RecordLedgerSelectionEvent event) {
        currentSelection = event.getSelectedRecords();
        setEnabled(currentSelection.length > 0);
    }

}
