//:File:    InMemoryLedger.java
//:Legal:   Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package org.rvsnoop;

import ca.odell.glazedlists.BasicEventList;

/**
 * A ledger that uses a simple in-memory collection to hold the records.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class InMemoryLedger extends RecordLedger {

    /**
     * Create a new in memory ledger instance.
     */
    public InMemoryLedger() {
        super(new BasicEventList());
    }

}
