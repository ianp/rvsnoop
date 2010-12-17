/*
 * Class:     Search
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Image;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.ui.ImageFactory;
import org.rvsnoop.ui.SearchResultsDialog;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Start a new live search in the ledger contents.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
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
        return ImageFactory.getInstance().getBannerImage(COMMAND);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getLedger()
     */
    @Override
    protected FilteredLedgerView getLedger() {
        return FilteredLedgerView.newInstance(application.getLedger(), true);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getMatcherEditors()
     */
    @Override
    protected EventList getMatcherEditors() {
        return new BasicEventList();
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getTitle()
     */
    @Override
    protected String getTitle() {
        return TITLE;
    }

}
