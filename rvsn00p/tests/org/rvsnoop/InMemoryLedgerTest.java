//:File:    InMemoryLedgerTest.java
//:Legal:   Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package org.rvsnoop;

/**
 * Unit tests for the {@link InMemoryLedger} class.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class InMemoryLedgerTest extends RecordLedgerTest {

    /* (non-Javadoc)
     * @see org.rvsnoop.RecordLedgerTest#createRecordLedger()
     */
    protected RecordLedger createRecordLedger() {
        return new InMemoryLedger();
    }

}
