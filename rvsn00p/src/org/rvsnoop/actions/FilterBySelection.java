/*
 * Class:     FilterBySelection
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
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;
import org.rvsnoop.ui.ImageFactory;

import rvsnoop.Record;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

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
    static String ACCELERATOR, DESCRIPTION, MNEMONIC, NAME, TITLE, TOOLTIP;

    private transient Record[] currentSelection;

    public FilterBySelection(Application application) {
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
    protected String getDescription() {
        return DESCRIPTION;
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getImage()
     */
    protected Image getImage() {
        return ImageFactory.getInstance().getBannerImage(COMMAND);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getLedger()
     */
    protected FilteredLedgerView getLedger() {
        return application.getFilteredLedger();
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.AbstractSearchAction#getMatcherEditors()
     */
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
