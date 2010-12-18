/*
 * Class:     RecordSelection
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.rvsnoop.CausedIOException;
import org.rvsnoop.Connections;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * A class to handle reading and writing records to and from various sources.
 * <p>
 * This class has several static methods to handle reading and writing records
 * to byte streams in the RvSnoop Record Byte Stream Format. A single record
 * represented in the format consists of:
 * <ol>
 * <li>
 *   The magic number {@linkplain #BIND_RECORD_MAGIC}; followed by
 * </li>
 * <li>
 *   A boolean denoting the existence of connection information. If the boolean
 *   is false then the next 4 fields are omitted from the stream.
 * </li>
 * <li>
 *   The connection description, as a Java-modified UTF encoded string.
 * </li>
 * <li>
 *   The connection service parameter, as a Java-modified UTF encoded string.
 * </li>
 * <li>
 *   The connection network parameter, as a Java-modified UTF encoded string.
 * </li>
 * <li>
 *   The connection daemon parameter, as a Java-modified UTF encoded string.
 * </li>
 * <li>
 *   The send subject, as a Java-modified UTF encoded string.
 * </li>
 * <li>
 *   The reply subject, as a Java-modified UTF encoded string.
 * </li>
 * <li>
 *   A long, representing the record timestamp in seconds since epoch.
 * </li>
 * <li>
 *   An integer, represent the size of the message in Rendezvous wire-format.
 * </li>
 * <li>
 *   A byte array containing the message in Rendezvous wire format.
 * </li>
 * </ol>
 * Certified messaging information is <em>not</em> stored when using this format.
 * <p>
 * A series of messages in the format begins with the magic number
 * {@linkplain #BIND_RECORD_SET_MAGIC} followed by an integer representing the
 * number of messages in the stream, followed by that many records in the format
 * described above.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class RecordSelection implements ClipboardOwner, Transferable {

    /**
     * Magic number that represents a single record entry in a byte stream.
     * <p>
     * The format of a single record entry is
     * <pre>MAGIC_NUMBER CONNECTION? CONNECTION_INFO TIMESTAMP MESSAGE_LENGTH MESSAGE</pre>
     * where connection is a boolean, if it is true then connection info is
     * stored as four Java-modified UTF strings representing description, service,
     * network, and daemon, respectively. If connection is false then connection
     * info is not stored.
     * <p>
     * Note that sequence number is <em>not</em> stored.
     *
     * @see DataOutput#writeUTF(String)
     */
    public static final byte[] BIND_RECORD_MAGIC;

    /**
     * Magic number that represents a set of record entries in a byte stream.
     * <p>
     * The format of a record set is <pre>MAGIC_NUMBER LENGTH RECORD*</pre>.
     */
    public static final byte[] BIND_RECORD_SET_MAGIC;

    public static final DataFlavor BYTES_FLAVOUR = new DataFlavor("application/x-rvsnoop-record-byte-stream", "RvSnoop Records Byte Stream");

    static {
        try {
            // Keep these to 4 characters then they can be used as Type codes
            // on Mac OS X.
            BIND_RECORD_MAGIC = "RS02".getBytes("UTF-8");
            BIND_RECORD_SET_MAGIC = "RS01".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * A convenience method to unpack an array of messages from a transferrable.
     * <p>
     * See <code>BIND_RECORD_SET_MAGIC</code> For details about the stream
     * format.
     *
     * @param selection The transferable to read from.
     * @return The array of messages.
     * @throws UnsupportedFlavorException If the transferrable does not support
     *     {@linkplain #BYTES_FLAVOUR} or
     *     {@linkplain DataFlavor#javaFileListFlavor}.
     * @throws IOException If there was an exception thrown when reading from
     *     the transferable.
     */
    public static Record[] read(Transferable selection, Connections connections) throws UnsupportedFlavorException, IOException, TibrvException {
        if (selection.isDataFlavorSupported(BYTES_FLAVOUR)) {
            return read(new DataInputStream((ByteArrayInputStream) selection.getTransferData(BYTES_FLAVOUR)), connections);
        } else if (selection.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            final List files = (List) selection.getTransferData(DataFlavor.javaFileListFlavor);
            final Record[] records = new Record[files.size()];
            for (int i = 0, imax = records.length; i < imax; ++i) {
                final File file = (File) files.get(i);
                final FileInputStream fis = new FileInputStream(file);
                final BufferedInputStream bis = new BufferedInputStream(fis, (int) file.length());
                records[i] = readRecord(new DataInputStream(bis), connections, true);
                IOUtils.closeQuietly(fis);
            }
            return records;
        } else {
            throw new UnsupportedFlavorException(selection.getTransferDataFlavors()[0]);
        }
    }

    /**
     * Read data from an input.
     *
     * @param input The input to read from.
     * @return The array of records that were unpacked from the input.
     * @throws IOException
     */
    public static Record[] read(DataInput input, Connections connections) throws IOException {
        final byte[] magic = new byte[BIND_RECORD_MAGIC.length];
        input.readFully(magic);
        Record[] records;
        if (Arrays.equals(BIND_RECORD_SET_MAGIC, magic)) {
            records = new Record[input.readInt()];
            for (int i = 0, imax = records.length; i < imax; ++i)
                records[i] = readRecord(input, connections, true);
            return records;
        } else if (Arrays.equals(BIND_RECORD_MAGIC, magic)) {
            return new Record[] { readRecord(input, connections, false) };
        } else {
            throw new IOException("Input does not contain a valid record stream.");
        }
    }

    private static Record readRecord(DataInput input, Connections connections, boolean checkMagic) throws IOException {
        if (checkMagic) {
            final byte[] magic = new byte[BIND_RECORD_MAGIC.length];
            input.readFully(magic);
            if (!Arrays.equals(BIND_RECORD_MAGIC, magic))
                throw new IOException("Input does not contain a valid record stream.");
        }
        final boolean connFlag = input.readBoolean();
        final String description = connFlag ? input.readUTF() : null;
        final String service = connFlag ? input.readUTF() : null;
        final String network = connFlag ? input.readUTF() : null;
        final String daemon = connFlag ? input.readUTF() : null;
        RvConnection connection = connections != null ? connections.get(service, network, daemon) : null;
        if (connection == null) {
            connection = new RvConnection(service, network, daemon);
            connection.setDescription(description);
            connection.addSubject(">");
            connections.add(connection);
        }
        final String send = input.readUTF();
        final String reply = input.readUTF();
        final long timestamp = input.readLong();
        final int length = input.readInt();
        final byte[] bytes = new byte[length];
        input.readFully(bytes);
        try {
            return new Record(connection, new TibrvMsg(bytes), send, reply, timestamp);
        } catch (TibrvException e) {
            throw new CausedIOException("Could not convert bytes to record.", e);
        }
    }

    /**
     * Write a series of records to a byte stream.
     *
     * @param records The records to write.
     * @param output The stream to write to.
     * @throws IOException If the records could not be written.
     */
    public static void write(Record[] records, DataOutput output) throws IOException {
        output.write(BIND_RECORD_SET_MAGIC);
        output.writeInt(records.length);
        for (int i = 0, imax = records.length; i < imax; ++i)
            write(records[i], output);
    }

    /**
     * Write a record to a byte stream.
     *
     * @param record The record to write.
     * @param output The stream to write to.
     * @throws IOException If the records could not be written.
     */
    public static void write(Record record, DataOutput output) throws IOException {
        output.write(BIND_RECORD_MAGIC);
        final RvConnection connection = record.getConnection();
        if (connection != null) {
            output.writeBoolean(true);
            output.writeUTF(connection.getDescription());
            output.writeUTF(connection.getService());
            output.writeUTF(connection.getNetwork());
            output.writeUTF(connection.getDaemon());
        } else {
            output.writeBoolean(false);
        }
        final String ss = record.getSendSubject();
        output.writeUTF(SubjectHierarchy.NO_SUBJECT_LABEL.equals(ss) ? "" : ss);
        final String rs = record.getReplySubject();
        output.writeUTF(SubjectHierarchy.NO_SUBJECT_LABEL.equals(rs) ? "" : rs);
        output.writeLong(record.getTimestamp());
        byte[] bytes;
        try {
            bytes = record.getMessage().getAsBytes();
            output.writeInt(bytes.length);
            output.write(bytes);
        } catch (TibrvException e) {
            throw new CausedIOException("Could not convert record to bytes: " + record, e);
        }
    }

    private final Record[] records;

    /**
     * Creates an instance that transfers a multiple records.
     *
     * @param records The list of records to transfer.
     * @throws IllegalArgumentException if any of the items in the list are not
     *     instances of {@linkplain Record}.
     */
    public RecordSelection(Record[] records) {
        this.records = records;
    }

    /**
     * Creates an instance that transfers a single message.
     *
     * @param message The message to transfer.
     * @throws TibrvException If the data could not be extracted from the message.
     * @throws IOException If the messages could not be serialized to the clipboard.
     */
    public RecordSelection(Record message) throws TibrvException, IOException {
        this.records = new Record[] { message };
    }

    /**
     * The returned object may be cast to <code>byte[]</code>.
     *
     * @see Transferable#getTransferData(DataFlavor)
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (BYTES_FLAVOUR.isMimeTypeEqual(flavor)) {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream(2048 * records.length);
            write(records, new DataOutputStream(stream));
            return new ByteArrayInputStream(stream.toByteArray());
        }
        throw new UnsupportedFlavorException(flavor);
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { BYTES_FLAVOUR };
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return BYTES_FLAVOUR.equals(flavor);
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // Do nothing.
    }

}