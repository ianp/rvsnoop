/*
 * Class:     RecordLedgerTable
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import javax.swing.JTable;

import org.rvsnoop.RecordLedgerFormat;

import ca.odell.glazedlists.swing.EventTableModel;

/**
 * A custom <code>JTable</code> that is used to draw the record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class RecordLedgerTable extends JTable {

    static final long serialVersionUID = 7601999759173563496L;

    public RecordLedgerTable(EventTableModel model) {
        super(model);
    }

    /**
     * Convenience method to access the table format used by this table.
     *
     * @return The format.
     */
    public RecordLedgerFormat getTableFormat() {
        final EventTableModel model = ((EventTableModel) getModel());
        return (RecordLedgerFormat) model.getTableFormat();
    }

}
