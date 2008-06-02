/*
 * Class:     FilteredLedgerViewTest
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;


/**
 * Unit tests for the {@link FilteredLedgerView} class.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class FilteredLedgerViewTest extends RecordLedgerTest {

    /* (non-Javadoc)
     * @see org.rvsnoop.RecordLedgerTest#createRecordLedger()
     */
    @Override
    protected RecordLedger createRecordLedger() {
        return FilteredLedgerView.newInstance(new InMemoryLedger(), false);
    }

}
