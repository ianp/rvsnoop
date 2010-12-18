/*
 * Class:     Filter
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import ca.odell.glazedlists.EventList;
import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.NLSUtils;

import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 * Filter the messages visible in the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class Filter extends AbstractSearchAction {

    static { NLSUtils.internationalize(Filter.class); }

    private static final long serialVersionUID = 7395491526593830048L;

    public static final String COMMAND = "filter";
    static String ACCELERATOR, DESCRIPTION, MNEMONIC, NAME, TITLE, TOOLTIP;

    public Filter(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
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
        return new ImageIcon("/resources/banners/filter.png");
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
        return application.getFilteredLedger().getMatchers();
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getTitle()
     */
    @Override
    protected String getTitle() {
        return TITLE;
    }

}
