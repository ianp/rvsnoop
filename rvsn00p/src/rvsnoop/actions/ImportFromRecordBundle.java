/*
 * Class:     ImportFromRecordBundle
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.apache.commons.io.output.ByteArrayOutputStream;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.RvConnection;

/**
 * Import the contents of a ‘snoop record bundle’ to the ledger.
 * <p>
 * A record bundle is just a zip file containing one entry per record, the
 * records use the RvSnoop byte stream format (i.e. the native message format
 * and a bit of additional metadata).
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public final class ImportFromRecordBundle extends ImportFromFile {

    private static final String ID = "importFromRecordBundle";

    private static String NAME = "Record Bundle";

    static final long serialVersionUID = -6034884975434400478L;

    private static String TOOLTIP = "Import the contents of a record bundle";

    private ZipInputStream zip;

    public ImportFromRecordBundle() {
        super(ID, NAME, null, new ExportToRecordBundle.FileFilter());
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.ImportFromFile#importFromFile(java.io.InputStream)
     */
    protected void importRecords(InputStream stream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] bytes = new byte[1024];
        zip = new ZipInputStream(stream);
        while (zip.getNextEntry() != null) {
            int count;
            buffer.reset();
            while ((count = zip.read(bytes, 0, 1024)) != -1)
                buffer.write(bytes, 0, count);
            final DataInput in = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            final Record[] records = RecordSelection.read(in, null); // FIXME
            for (int i = 0, imax = records.length; i < imax; ++i)
                SwingUtilities.invokeLater(new RvConnection.AddRecordTask(records[i]));
        }
    }

}
