/*
 * Class:     InMemoryLedgerTest
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
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
    @Override
    protected RecordLedger createRecordLedger() {
        return new InMemoryLedger();
    }

}
