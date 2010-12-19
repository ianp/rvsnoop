// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.ui.SearchResultsDialog;

import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 * Start a new live search in the ledger contents.
 */
public final class Search extends AbstractSearchAction {

    static { NLSUtils.internationalize(Search.class); }

    private static final long serialVersionUID = 1861453185869256202L;

    static String ACCELERATOR, DESCRIPTION, MNEMONIC, NAME, TITLE, TOOLTIP;

    public static final String COMMAND = "search";

    public Search(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    @Override
    protected void displayResults(RecordLedger ledger) {
        new SearchResultsDialog(application.getFrame(), ledger,
                application.getConnections(), application.getRecordTypes()).setVisible(true);
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
        return new ImageIcon("/resources/banners/search.png");
    }

    @Override
    protected FilteredLedgerView getLedger() {
        return FilteredLedgerView.newInstance(application.getLedger(), application.getRecordTypes(), true);
    }

    @Override
    protected EventList getMatcherEditors() {
        return new BasicEventList();
    }

    @Override
    protected String getTitle() {
        return TITLE;
    }

}
