/*
 * Class:     InMemoryLedger
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import ca.odell.glazedlists.BasicEventList;

/**
 * A ledger that uses a simple in-memory collection to hold the records.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class InMemoryLedger extends RecordLedger {

    /**
     * Create a new in memory ledger instance.
     */
    public InMemoryLedger() {
        super(new BasicEventList());
    }

}
