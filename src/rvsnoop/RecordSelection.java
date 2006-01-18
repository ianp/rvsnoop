//:File:    RecordSelection.java
//:Created: Dec 28, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * A Transferable which implements the capability required to transfer a {@link TibrvMsg}.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class RecordSelection implements ClipboardOwner, Transferable {

    public static final DataFlavor BYTES_FLAVOUR = new DataFlavor(ByteBuffer.class, "Rendezvous Message (as raw bytes)");

    /**
     * A convenience method to unpack an array of messages from a transferrable.
     * 
     * @param selection The transferable to read from.
     * @return The array of messages.
     * @throws UnsupportedFlavorException If the transferrable does not support {@link #BYTES_FLAVOUR}.
     * @throws IOException If there was an exception thrown when reading from the transferable.
     * @throws TibrvException If any of the chunks in the stream could not be converted into messages.
     * @throws java.nio.BufferUnderflowException If there are not enough bytes in the stream to reconstruct all of the messages described in the header.
     * @see Record#BIND_RECORD_SET_MAGIC For details about the stream format.
     */
    public static TibrvMsg[] unmarshal(Transferable selection) throws UnsupportedFlavorException, IOException, TibrvException {
        final ByteBuffer data = (ByteBuffer) selection.getTransferData(BYTES_FLAVOUR);
        byte[] bytes = null;
        if (data.hasArray()) {
            bytes = data.array();
        } else {
            bytes = new byte[data.remaining()];
            data.get(bytes);
        }
        final TupleInput input = new TupleInput(bytes);
        final byte[] magic = new byte[Record.BIND_RECORD_SET_MAGIC.length];
        input.readFast(magic);
        if (!Arrays.equals(Record.BIND_RECORD_SET_MAGIC, magic))
            throw new IllegalArgumentException("Selection does not contain a valid records stream.");
        final int numMessages = input.readInt();
        final TibrvMsg[] messages = new TibrvMsg[numMessages];
        for (int i = 0; i < numMessages; ++i)
            messages[i] = (TibrvMsg) Record.BINDING.entryToObject(input);
        return messages;
    }
    
    /**
     * The messages as a stream of bytes.
     * 
     * @see Record#BIND_RECORD_SET_MAGIC For details about the stream format.
     */
    private ByteBuffer data;
    
    /**
     * Creates an instance that transfers a multiple messages.
     * 
     * @param messages The list of messages to transfer.
     * @throws TibrvException If the data could not be extracted from the message.
     * @throws IOException If the messages could not be serialized to the clipboard.
     * @throws ClassCastExcption if any of the items in the list are noe records.
     */
    public RecordSelection(List messages) throws TibrvException, IOException {
        super();
        final int numMessages = messages.size();
        final TupleOutput output = new TupleOutput();
        output.writeFast(Record.BIND_RECORD_SET_MAGIC);
        output.writeInt(numMessages);
        for (final Iterator i = messages.iterator(); i.hasNext();)
            Record.BINDING.objectToEntry(i.next(), output);
        data = ByteBuffer.wrap(output.getBufferBytes());
        data.rewind();
    }

    /**
     * Creates an instance that transfers a single message.
     * 
     * @param message The message to transfer.
     * @throws TibrvException If the data could not be extracted from the message.
     * @throws IOException If the messages could not be serialized to the clipboard.
     */
    public RecordSelection(Record message) throws TibrvException, IOException {
        this(Arrays.asList(new Record[] { message }));
    }
    
    /**
     * The returned object may be cast to <code>byte[]</code>.
     * 
     * @see Transferable#getTransferData(DataFlavor)
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);
        return data;
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
        data = null;
    }

}