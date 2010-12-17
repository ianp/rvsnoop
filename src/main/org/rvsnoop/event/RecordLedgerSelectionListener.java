/*
 * Class:     RecordLedgerSelectionListener
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.event;

import java.util.EventListener;

/**
 * The listener that is notified when the record ledger selection changes.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public interface RecordLedgerSelectionListener extends EventListener {

    /**
     * Messaged whenever the record ledger selection has changed.
     * <p>
     * Note that unlike normal list selection events this is only messaged when
     * all changes are complete (i.e. when
     * {@link javax.swing.event.ListSelectionEvent#getValueIsAdjusting()}
     * is <code>false</code>).
     *
     * @param event The event that described the current selection state.
     */
    public void valueChanged(RecordLedgerSelectionEvent event);

}
