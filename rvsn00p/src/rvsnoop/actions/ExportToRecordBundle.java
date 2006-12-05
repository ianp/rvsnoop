//:File:    ExportToRecordBundle.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.Action;

import org.apache.commons.io.output.ByteArrayOutputStream;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.Version;

/**
 * Export the current ledger selction to a ‘snoop record bundle’.
 * <p>
 * A record bundle is just a zip file containing one entry per record, the
 * records use the RvSnoop byte stream format (i.e. the native message format
 * and a bit of additional metadata).
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
final class ExportToRecordBundle extends ExportToFile {

    static class FileFilter extends javax.swing.filechooser.FileFilter {
        FileFilter() {
            super();
        }
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".srb");
        }

        public String getDescription() {
            return "Record Bundles";
        }
    }

    static final long serialVersionUID = 9036056933732494645L;

    private static final String ID = "exportToRecordBundle";

    private static String NAME = "Record Bundle";

    private static String TOOLTIP = "Export the current ledger selction to a record bundle";

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private final DataOutput dataBuffer = new DataOutputStream(buffer);

    private ZipOutputStream zip;

    public ExportToRecordBundle() {
        super(ID, NAME, null, new FileFilter());
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.ExportToFile#writeFooter()
     */
    protected void writeFooter() throws IOException {
        zip.close();
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.ExportToFile#writeHeader(int)
     */
    protected void writeHeader(int numberOfRecords) throws IOException {
        zip = new ZipOutputStream(stream);
        zip.setComment(Version.getAsStringWithName() + " Record Bundle");
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.ExportToFile#writeRecord(rvsnoop.Record, int)
     */
    protected void writeRecord(Record record, int index) throws IOException {
        buffer.reset();
        RecordSelection.write(record, dataBuffer);
        final byte[] bytes = buffer.toByteArray();
        final ZipEntry entry = new ZipEntry(Integer.toString(index));
        entry.setSize(bytes.length);
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }

}
