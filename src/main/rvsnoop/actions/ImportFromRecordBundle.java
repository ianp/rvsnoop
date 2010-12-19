// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.bushe.swing.event.EventBus;
import org.rvsnoop.Application;
import org.rvsnoop.event.MessageReceivedEvent;
import rvsnoop.Record;
import rvsnoop.RecordSelection;

/**
 * Import the contents of a ‘snoop record bundle’ to the ledger.
 * <p>
 * A record bundle is just a zip file containing one entry per record, the
 * records use the RvSnoop byte stream format (i.e. the native message format
 * and a bit of additional metadata).
 */
public final class ImportFromRecordBundle extends ImportFromFile {

    public static final String COMMAND = "importFromRecordBundle";

    public ImportFromRecordBundle(Application application) {
        super(application, COMMAND, new ExportToRecordBundle.FileFilter());
    }

    @Override
    protected void importRecords(InputStream stream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] bytes = new byte[1024];
        ZipInputStream zip = new ZipInputStream(stream);
        while (zip.getNextEntry() != null) {
            int count;
            buffer.reset();
            while ((count = zip.read(bytes, 0, 1024)) != -1)
                buffer.write(bytes, 0, count);
            final DataInput in = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            final Record[] records = RecordSelection.read(in, null); // FIXME
            for (int i = 0, imax = records.length; i < imax; ++i) {
                EventBus.publish(new MessageReceivedEvent(records[i]));
            }
        }
    }

}
