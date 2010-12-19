// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop;

import org.jdesktop.application.ApplicationContext;
import rvsnoop.RecordTypes;

/**
 * Unit tests for the {@link FilteredLedgerView} class.
 */
public class FilteredLedgerViewTest extends RecordLedgerTest {

    @Override
    protected RecordLedger createRecordLedger() {
        ApplicationContext context = new ApplicationContext() {};
        RecordTypes recordTypes = new RecordTypes(context);
        return FilteredLedgerView.newInstance(new InMemoryLedger(context, recordTypes), recordTypes, false);
    }

}
