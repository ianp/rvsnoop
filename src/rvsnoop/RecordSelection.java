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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

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
     */
    public static TibrvMsg[] unmarshal(Transferable selection) throws UnsupportedFlavorException, IOException, TibrvException {
        final ByteBuffer data = (ByteBuffer) selection.getTransferData(BYTES_FLAVOUR);
        final int numMessages = data.getInt();
        final TibrvMsg[] messages = new TibrvMsg[numMessages];
        final int[] lengths = new int[numMessages];
        for (int i = 0; i < numMessages; ++i)
            lengths[i] = data.getInt();
        byte[] message = new byte[lengths[0]];
        for (int i = 0; i < numMessages; ++i) {
            if (message.length != lengths[i])
                message = new byte[lengths[i]];
            data.get(message);
            messages[i] = new TibrvMsg(message);
        }
        return messages;
    }
    
    /**
     * The messages as a stream of bytes.
     * <p>
     * The format for the stream is a single integer, <code>n</code> detailing
     * the number of messages in the stream, followed by <code>n</code>
     * integers giving the size in bytes of each message, followed by
     * <code>n</code> chunks of data which can be used to create messages.
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
        final int[] lengths = new int[numMessages];
        final ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        final DataOutputStream dataStream = new DataOutputStream(stream);
        // Write the space for the stream header.
        dataStream.writeInt(numMessages);
        for (int i = 0; i < numMessages; ++i) {
            // Start by seting the sizes to 0, we will go back and set them later.
            dataStream.writeInt(0);
        }
        // Now write the messages.
        for (int i = 0; i < numMessages; ++i) {
            final byte[] message = ((Record) messages.get(i)).getMessage().getAsBytes();
            lengths[i] = message.length;
            stream.write(message);
        }
        data = ByteBuffer.wrap(stream.toByteArray());
        data.rewind();
        // Finally go back and fill in the correct sizes in the header.
        for (int i = 0, offset = 0; i < numMessages; ++i)
            data.putInt(offset += 4, lengths[i]);
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
     * <p>
     * The format for the stream is a single integer, <code>n</code> detailing
     * the number of messages in the stream, followed by <code>n</code>
     * integers giving the size in bytes of each message, followed by
     * <code>n</code> chunks of data which can be used to create messages.
     * 
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
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