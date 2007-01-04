/*
 * Class:     MessageLedger
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.InMemoryLedger;
import org.rvsnoop.RecordLedger;

/**
 * The message ledger is the main store for received messages.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class MessageLedger {

    // FIXME:
    // These are nasty hacks to allow this class to be removed while the new
    // ledger implementations are used instead.
    public static final RecordLedger RECORD_LEDGER = new InMemoryLedger();
    public static final FilteredLedgerView FILTERED_VIEW = new FilteredLedgerView(RECORD_LEDGER);

}
